package org.cru.godtools.shortcuts

import android.app.Application
import android.content.pm.ActivityInfo
import android.content.pm.ResolveInfo
import android.content.pm.ShortcutManager
import android.os.Build
import androidx.core.content.getSystemService
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.argThat
import com.nhaarman.mockitokotlin2.clearInvocations
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import java.util.EnumSet
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScope
import org.ccci.gto.android.common.db.find
import org.cru.godtools.base.Settings
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
    private lateinit var settings: Settings
    private val coroutineScope = TestCoroutineScope().apply { pauseDispatcher() }

    private lateinit var shortcutManager: GodToolsShortcutManager

    @Before
    fun setup() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        Shadows.shadowOf(app).grantPermissions(INSTALL_SHORTCUT_PERMISSION)
        this.app = spy(app) {
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
        settings = mock()

        shortcutManager =
            GodToolsShortcutManager(this.app, dao, eventBus, settings, coroutineScope, coroutineScope.coroutineContext)
    }

    @After
    fun cleanup() {
        shortcutManager.updateShortcutsActor.close()
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
        coroutineScope.runCurrent()
        clearInvocations(dao)

        // update doesn't trigger before requested
        coroutineScope.advanceUntilIdle()
        coroutineScope.advanceTimeBy(DELAY_UPDATE_PENDING_SHORTCUTS)
        verifyZeroInteractions(dao)

        // trigger update
        assertTrue(shortcutManager.updatePendingShortcutsActor.offer(Unit))
        verifyZeroInteractions(dao)
        coroutineScope.advanceTimeBy(DELAY_UPDATE_PENDING_SHORTCUTS)
        verify(dao).find<Tool>("kgp")
        verifyNoMoreInteractions(dao)
        clearInvocations(dao)

        // trigger multiple updates simultaneously, it should conflate to a single update
        assertTrue(shortcutManager.updatePendingShortcutsActor.offer(Unit))
        coroutineScope.advanceTimeBy(1)
        verifyZeroInteractions(dao)
        assertTrue(shortcutManager.updatePendingShortcutsActor.offer(Unit))
        coroutineScope.advanceTimeBy(DELAY_UPDATE_PENDING_SHORTCUTS)
        verify(dao).find<Tool>("kgp")
        verifyNoMoreInteractions(dao)
        coroutineScope.advanceUntilIdle()
        verifyZeroInteractions(dao)
    }
    // endregion Pending Shortcuts

    // region Update Existing Shortcuts
    @Test
    fun verifyUpdateExistingShortcuts() {
        assumeThat(Build.VERSION.SDK_INT, greaterThanOrEqualTo(Build.VERSION_CODES.N_MR1))

        whenever(dao.get(Tool::class.java)).thenReturn(emptyList())

        // ensure update shortcuts is initially delayed
        coroutineScope.advanceTimeBy(DELAY_UPDATE_SHORTCUTS - 1)
        verifyZeroInteractions(dao)
        coroutineScope.advanceTimeBy(1)
        verify(dao).get(Tool::class.java)

        // trigger multiple updates simultaneously, it should conflate to a single update
        assertTrue(shortcutManager.updateShortcutsActor.offer(Unit))
        coroutineScope.advanceTimeBy(1)
        verifyZeroInteractions(dao)
    }

    @Test
    fun verifyUpdateExistingShortcutsNotAvailableForOldSdks() {
        assumeThat(Build.VERSION.SDK_INT, lessThan(Build.VERSION_CODES.N_MR1))
        coroutineScope.advanceUntilIdle()
        verifyZeroInteractions(dao)
    }
    // endregion Update Existing Shortcuts
}
