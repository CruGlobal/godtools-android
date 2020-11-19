package org.cru.godtools.shortcuts

import android.content.Context
import android.content.pm.ShortcutManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.clearInvocations
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import java.util.EnumSet
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScope
import org.ccci.gto.android.common.db.find
import org.cru.godtools.base.Settings
import org.cru.godtools.model.Tool
import org.greenrobot.eventbus.EventBus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.keynote.godtools.android.db.GodToolsDao

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalCoroutinesApi::class)
class GodToolsShortcutManagerTest {
    private lateinit var context: Context
    private lateinit var systemShortcutManager: ShortcutManager
    private lateinit var dao: GodToolsDao
    private lateinit var eventBus: EventBus
    private lateinit var settings: Settings
    private lateinit var coroutineScope: TestCoroutineScope

    private lateinit var shortcutManager: GodToolsShortcutManager

    @Before
    fun setup() {
        systemShortcutManager = mock {
            on { it.isRequestPinShortcutSupported } doReturn true
        }
        context = mock {
            on { getSystemService(ShortcutManager::class.java) } doReturn systemShortcutManager
        }
        dao = mock()
        eventBus = mock()
        settings = mock()
        coroutineScope = TestCoroutineScope()
        coroutineScope.pauseDispatcher()

        shortcutManager =
            GodToolsShortcutManager(context, dao, eventBus, settings, coroutineScope, coroutineScope.coroutineContext)
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
        val shortcut = shortcutManager.getPendingToolShortcut("kgp")!!
        coroutineScope.runCurrent()
        clearInvocations(dao)

        // update doesn't trigger before requested
        coroutineScope.advanceTimeBy(DELAY_PENDING_SHORTCUT_UPDATE)
        verifyZeroInteractions(dao)

        // trigger update
        shortcutManager.updatePendingShortcutsActor.offer(Unit)
        verifyZeroInteractions(dao)
        coroutineScope.advanceTimeBy(DELAY_PENDING_SHORTCUT_UPDATE)
        verify(dao).find<Tool>("kgp")
        verifyNoMoreInteractions(dao)
        clearInvocations(dao)

        // trigger multiple updates simultaneously, it should conflate to a single update
        shortcutManager.updatePendingShortcutsActor.offer(Unit)
        coroutineScope.advanceTimeBy(1)
        verifyZeroInteractions(dao)
        shortcutManager.updatePendingShortcutsActor.offer(Unit)
        coroutineScope.advanceTimeBy(DELAY_PENDING_SHORTCUT_UPDATE)
        verify(dao).find<Tool>("kgp")
        verifyNoMoreInteractions(dao)
        coroutineScope.advanceUntilIdle()
        verifyZeroInteractions(dao)
    }
    // endregion Pending Shortcuts
}
