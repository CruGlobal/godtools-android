package org.cru.godtools.shortcuts

import android.app.Application
import android.content.pm.ActivityInfo
import android.content.pm.ResolveInfo
import android.content.pm.ShortcutManager
import android.os.Build
import androidx.core.content.getSystemService
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argThat
import com.nhaarman.mockitokotlin2.clearInvocations
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.stub
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import com.squareup.picasso.Picasso
import java.util.EnumSet
import java.util.Locale
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.ccci.gto.android.common.db.Query
import org.ccci.gto.android.common.db.find
import org.ccci.gto.android.common.testing.timber.ExceptionRaisingTree
import org.cru.godtools.base.Settings
import org.cru.godtools.base.ToolFileManager
import org.cru.godtools.model.Tool
import org.greenrobot.eventbus.EventBus
import org.hamcrest.Matchers.greaterThanOrEqualTo
import org.hamcrest.Matchers.lessThan
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.keynote.godtools.android.db.GodToolsDao
import org.robolectric.Shadows
import org.robolectric.annotation.Config

private const val ACTION_INSTALL_SHORTCUT = "com.android.launcher.action.INSTALL_SHORTCUT"
private const val INSTALL_SHORTCUT_PERMISSION = "com.android.launcher.permission.INSTALL_SHORTCUT"

@RunWith(AndroidJUnit4::class)
@Config(sdk = [24, 25, 28])
@OptIn(ExperimentalCoroutinesApi::class)
class GodToolsShortcutManagerTest {
    private lateinit var app: Application
    private lateinit var shortcutManagerService: ShortcutManager

    private lateinit var dao: GodToolsDao
    private lateinit var eventBus: EventBus
    private lateinit var fileManager: ToolFileManager
    private lateinit var picasso: Picasso
    private lateinit var settings: Settings
    private val coroutineScope = TestCoroutineScope(SupervisorJob()).apply { pauseDispatcher() }
    private val ioDispatcher = TestCoroutineDispatcher()

    private val primaryLanguageFlow = MutableSharedFlow<Locale>(extraBufferCapacity = 20)
    private val parallelLanguageFlow = MutableSharedFlow<Locale?>(extraBufferCapacity = 20)

    private lateinit var shortcutManager: GodToolsShortcutManager

    @Before
    fun setup() {
        val rawApp = ApplicationProvider.getApplicationContext<Application>()
        Shadows.shadowOf(rawApp).grantPermissions(INSTALL_SHORTCUT_PERMISSION)
        app = spy(rawApp) {
            val pm = spy(it.packageManager) { pm ->
                val shortcutReceiver = ResolveInfo().apply {
                    activityInfo = ActivityInfo().apply { permission = INSTALL_SHORTCUT_PERMISSION }
                }
                doReturn(listOf(shortcutReceiver))
                    .whenever(pm).queryBroadcastReceivers(argThat { action == ACTION_INSTALL_SHORTCUT }, eq(0))
            }
            on { packageManager } doReturn pm
            it.getSystemService<ShortcutManager>()?.let { sm ->
                shortcutManagerService = spy(sm)
                on { getSystemService(ShortcutManager::class.java) } doReturn shortcutManagerService
            }
        }
        dao = mock()
        eventBus = mock()
        fileManager = mock()
        picasso = mock()
        settings = mock {
            on { primaryLanguageFlow } doReturn primaryLanguageFlow
            on { parallelLanguageFlow } doReturn parallelLanguageFlow
        }

        shortcutManager =
            GodToolsShortcutManager(app, dao, eventBus, fileManager, picasso, settings, coroutineScope, ioDispatcher)
    }

    @After
    fun cleanup() {
        shortcutManager.shutdown()
        ioDispatcher.cleanupTestCoroutines()
        coroutineScope.cleanupTestCoroutines()
    }

    // region Pending Shortcuts
    // region canPinShortcut(tool)
    @Test
    fun verifyCanPinToolShortcut() {
        val supportedTypes = EnumSet.of(Tool.Type.TRACT, Tool.Type.ARTICLE)
        Tool.Type.values().forEach {
            assertEquals(supportedTypes.contains(it), shortcutManager.canPinToolShortcut(Tool().apply { type = it }))
        }
    }

    @Test
    fun verifyCanPinToolShortcutNull() {
        assertFalse(shortcutManager.canPinToolShortcut(null))
    }
    // endregion canPinShortcut(tool)

    @Test
    fun verifyGetPendingToolShortcutInvalidTool() {
        val shortcut = shortcutManager.getPendingToolShortcut("invalid")!!
        coroutineScope.runCurrent()
        verify(dao).find<Tool>("invalid")
        assertNull(shortcut.shortcut)
    }

    @Test
    fun verifyUpdatePendingToolShortcuts() {
        shortcutManager.updateShortcutsActor.close()
        val shortcut = shortcutManager.getPendingToolShortcut("kgp")!!
        coroutineScope.advanceUntilIdle()
        clearInvocations(dao)

        // update doesn't trigger before requested
        coroutineScope.advanceUntilIdle()
        coroutineScope.advanceTimeBy(DELAY_UPDATE_PENDING_SHORTCUTS)
        verifyZeroInteractions(dao)

        // trigger update
        assertTrue(shortcutManager.updatePendingShortcutsActor.trySend(Unit).isSuccess)
        verifyZeroInteractions(dao)
        coroutineScope.advanceTimeBy(DELAY_UPDATE_PENDING_SHORTCUTS)
        verify(dao).find<Tool>("kgp")
        verifyNoMoreInteractions(dao)
        clearInvocations(dao)

        // trigger multiple updates simultaneously, it should conflate to a single update
        assertTrue(shortcutManager.updatePendingShortcutsActor.trySend(Unit).isSuccess)
        coroutineScope.advanceTimeBy(1)
        verifyZeroInteractions(dao)
        assertTrue(shortcutManager.updatePendingShortcutsActor.trySend(Unit).isSuccess)
        coroutineScope.advanceTimeBy(DELAY_UPDATE_PENDING_SHORTCUTS)
        verify(dao).find<Tool>("kgp")
        verifyNoMoreInteractions(dao)
        coroutineScope.advanceUntilIdle()
        verifyZeroInteractions(dao)
    }
    // endregion Pending Shortcuts

    // region Update Existing Shortcuts
    @Test
    fun verifyUpdateExistingShortcutsOnPrimaryLanguageUpdate() {
        assumeThat(Build.VERSION.SDK_INT, greaterThanOrEqualTo(Build.VERSION_CODES.N_MR1))

        whenever(dao.get(Tool::class.java)).thenReturn(emptyList())
        assertUpdateExistingShortcutsInitialUpdate()

        // trigger a primary language update
        assertTrue(primaryLanguageFlow.tryEmit(Locale.ENGLISH))
        verifyZeroInteractions(dao)
        coroutineScope.advanceUntilIdle()
        verify(dao).get(Tool::class.java)
    }

    @Test
    fun verifyUpdateExistingShortcutsOnParallelLanguageUpdate() {
        assumeThat(Build.VERSION.SDK_INT, greaterThanOrEqualTo(Build.VERSION_CODES.N_MR1))

        whenever(dao.get(Tool::class.java)).thenReturn(emptyList())
        assertUpdateExistingShortcutsInitialUpdate()

        // trigger a primary language update
        assertTrue(parallelLanguageFlow.tryEmit(null))
        verifyZeroInteractions(dao)
        coroutineScope.advanceUntilIdle()
        verify(dao).get(Tool::class.java)
    }

    @Test
    fun verifyUpdateExistingShortcutsAggregateMultiple() = runBlockingTest {
        assumeThat(Build.VERSION.SDK_INT, greaterThanOrEqualTo(Build.VERSION_CODES.N_MR1))

        whenever(dao.get(Tool::class.java)).thenReturn(emptyList())
        assertUpdateExistingShortcutsInitialUpdate()

        // trigger multiple updates simultaneously, it should aggregate to a single update
        assertTrue(shortcutManager.updateShortcutsActor.trySend(Unit).isSuccess)
        assertTrue(primaryLanguageFlow.tryEmit(Locale.ENGLISH))
        assertTrue(parallelLanguageFlow.tryEmit(null))
        coroutineScope.advanceTimeBy(DELAY_UPDATE_SHORTCUTS - 1)
        assertTrue(shortcutManager.updateShortcutsActor.trySend(Unit).isSuccess)
        assertTrue(shortcutManager.updateShortcutsActor.trySend(Unit).isSuccess)
        assertTrue(primaryLanguageFlow.tryEmit(Locale.ENGLISH))
        assertTrue(parallelLanguageFlow.tryEmit(null))
        coroutineScope.advanceTimeBy(DELAY_UPDATE_SHORTCUTS - 1)
        verifyZeroInteractions(dao)
        coroutineScope.advanceUntilIdle()
        verify(dao).get(Tool::class.java)
    }

    @Test
    fun verifyUpdateExistingShortcutsNotAvailableForOldSdks() {
        assumeThat(Build.VERSION.SDK_INT, lessThan(Build.VERSION_CODES.N_MR1))
        coroutineScope.advanceUntilIdle()
        assertTrue(
            "Ensure actor can still accept requests, even though they are no-ops",
            shortcutManager.updateShortcutsActor.trySend(Unit).isSuccess
        )
        coroutineScope.advanceUntilIdle()
        verifyZeroInteractions(dao)
    }

    @Test
    fun testUpdateDynamicShortcutsDoesntInterceptChildCancelledException() {
        assumeThat(Build.VERSION.SDK_INT, greaterThanOrEqualTo(Build.VERSION_CODES.N_MR1))

        dao.stub { on { get(any<Query<Tool>>()) } doReturn emptyList() }
        ioDispatcher.pauseDispatcher()
        coroutineScope.resumeDispatcher()

        ExceptionRaisingTree.plant().use {
            coroutineScope.launch { shortcutManager.updateDynamicShortcuts(emptyMap()) }.cancel()
            ioDispatcher.resumeDispatcher()
        }
    }

    private fun assertUpdateExistingShortcutsInitialUpdate() {
        // ensure update shortcuts is initially delayed
        coroutineScope.advanceTimeBy(DELAY_UPDATE_SHORTCUTS - 1)
        verifyZeroInteractions(dao)
        coroutineScope.advanceTimeBy(1)
        verify(dao).get(Tool::class.java)
        clearInvocations(dao)
    }
    // endregion Update Existing Shortcuts

    // region Instant App
    @Test
    fun verifyUpdateDynamicShortcutsOnInstantAppIsANoop() {
        assumeThat(Build.VERSION.SDK_INT, greaterThanOrEqualTo(Build.VERSION_CODES.N_MR1))

        // Instant Apps don't have access to the system ShortcutManager
        whenever(app.getSystemService<ShortcutManager>()).thenReturn(null)
        coroutineScope.resumeDispatcher()
        clearInvocations(dao)

        coroutineScope.launch { shortcutManager.updateDynamicShortcuts(emptyMap()) }
        verifyZeroInteractions(dao)
    }
    // endregion Instant App
}
