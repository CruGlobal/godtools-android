package org.cru.godtools.shortcuts

import android.app.Application
import android.content.pm.ActivityInfo
import android.content.pm.ResolveInfo
import android.content.pm.ShortcutManager
import android.os.Build
import androidx.core.content.getSystemService
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.squareup.picasso.Picasso
import java.util.EnumSet
import java.util.Locale
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.keynote.godtools.android.db.GodToolsDao
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.stub
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.annotation.Config.NEWEST_SDK
import org.robolectric.annotation.Config.OLDEST_SDK

private const val ACTION_INSTALL_SHORTCUT = "com.android.launcher.action.INSTALL_SHORTCUT"
private const val INSTALL_SHORTCUT_PERMISSION = "com.android.launcher.permission.INSTALL_SHORTCUT"

@RunWith(AndroidJUnit4::class)
@Config(sdk = [OLDEST_SDK, Build.VERSION_CODES.N, Build.VERSION_CODES.N_MR1, NEWEST_SDK])
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

        // trigger update
        runBlocking { shortcutManager.updatePendingShortcuts() }
        verify(dao).find<Tool>("kgp")
        verifyNoMoreInteractions(dao)
    }
    // endregion Pending Shortcuts

    // region Update Existing Shortcuts
    @Test
    @Config(sdk = [Build.VERSION_CODES.N_MR1, NEWEST_SDK])
    fun verifyUpdateExistingShortcutsOnPrimaryLanguageUpdate() {
        whenever(dao.get(Tool::class.java)).thenReturn(emptyList())
        assertUpdateExistingShortcutsInitialUpdate()

        // trigger a primary language update
        assertTrue(primaryLanguageFlow.tryEmit(Locale.ENGLISH))
        verifyNoInteractions(dao)
        coroutineScope.advanceUntilIdle()
        verify(dao).get(Tool::class.java)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N_MR1, NEWEST_SDK])
    fun verifyUpdateExistingShortcutsOnParallelLanguageUpdate() {
        whenever(dao.get(Tool::class.java)).thenReturn(emptyList())
        assertUpdateExistingShortcutsInitialUpdate()

        // trigger a primary language update
        assertTrue(parallelLanguageFlow.tryEmit(null))
        verifyNoInteractions(dao)
        coroutineScope.advanceUntilIdle()
        verify(dao).get(Tool::class.java)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N_MR1, NEWEST_SDK])
    fun verifyUpdateExistingShortcutsAggregateMultiple() = runBlockingTest {
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
        verifyNoInteractions(dao)
        coroutineScope.advanceUntilIdle()
        verify(dao).get(Tool::class.java)
    }

    @Test
    @Config(sdk = [OLDEST_SDK, Build.VERSION_CODES.N])
    fun verifyUpdateExistingShortcutsNotAvailableForOldSdks() {
        coroutineScope.advanceUntilIdle()
        assertTrue(
            "Ensure actor can still accept requests, even though they are no-ops",
            shortcutManager.updateShortcutsActor.trySend(Unit).isSuccess
        )
        coroutineScope.advanceUntilIdle()
        verifyNoInteractions(dao)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N_MR1, NEWEST_SDK])
    fun testUpdateDynamicShortcutsDoesntInterceptChildCancelledException() {
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
        verifyNoInteractions(dao)
        coroutineScope.advanceTimeBy(1)
        verify(dao).get(Tool::class.java)
        clearInvocations(dao)
    }
    // endregion Update Existing Shortcuts

    // region Instant App
    @Test
    @Config(sdk = [Build.VERSION_CODES.N_MR1, NEWEST_SDK])
    fun verifyUpdateDynamicShortcutsOnInstantAppIsANoop() {
        // Instant Apps don't have access to the system ShortcutManager
        whenever(app.getSystemService<ShortcutManager>()).thenReturn(null)
        coroutineScope.resumeDispatcher()
        clearInvocations(dao)

        coroutineScope.launch { shortcutManager.updateDynamicShortcuts(emptyMap()) }
        verifyNoInteractions(dao)
    }
    // endregion Instant App
}
