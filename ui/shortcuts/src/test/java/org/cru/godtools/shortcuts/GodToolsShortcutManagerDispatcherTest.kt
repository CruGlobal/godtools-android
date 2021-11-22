package org.cru.godtools.shortcuts

import java.util.Locale
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.TestCoroutineScope
import org.cru.godtools.base.Settings
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions

@OptIn(ExperimentalCoroutinesApi::class)
class GodToolsShortcutManagerDispatcherTest {
    private lateinit var settings: Settings
    private lateinit var shortcutManager: GodToolsShortcutManager
    private val coroutineScope = TestCoroutineScope(SupervisorJob()).apply { pauseDispatcher() }

    private val primaryLanguageFlow = MutableSharedFlow<Locale>(extraBufferCapacity = 20)
    private val parallelLanguageFlow = MutableSharedFlow<Locale?>(extraBufferCapacity = 20)

    private lateinit var dispatcher: GodToolsShortcutManager.Dispatcher

    @Before
    fun setup() {
        shortcutManager = mock()
        settings = mock {
            on { primaryLanguageFlow } doReturn primaryLanguageFlow
            on { parallelLanguageFlow } doReturn parallelLanguageFlow
        }

        dispatcher = GodToolsShortcutManager.Dispatcher(shortcutManager, mock(), settings, coroutineScope)
    }

    @After
    fun cleanup() {
        dispatcher.shutdown()
        coroutineScope.cleanupTestCoroutines()
    }

    @Test
    fun verifyUpdatePendingToolShortcuts() {
        // update doesn't trigger before requested
        coroutineScope.advanceUntilIdle()
        coroutineScope.advanceTimeBy(DELAY_UPDATE_PENDING_SHORTCUTS)
        verifyNoInteractions(shortcutManager)

        // trigger update
        assertTrue(dispatcher.updatePendingShortcutsActor.trySend(Unit).isSuccess)
        verifyNoInteractions(shortcutManager)
        coroutineScope.advanceTimeBy(DELAY_UPDATE_PENDING_SHORTCUTS)
        verifyBlocking(shortcutManager) { updatePendingShortcuts() }
        verifyNoMoreInteractions(shortcutManager)
        clearInvocations(shortcutManager)

        // trigger multiple updates simultaneously, it should conflate to a single update
        assertTrue(dispatcher.updatePendingShortcutsActor.trySend(Unit).isSuccess)
        coroutineScope.advanceTimeBy(1)
        verifyNoInteractions(shortcutManager)
        assertTrue(dispatcher.updatePendingShortcutsActor.trySend(Unit).isSuccess)
        coroutineScope.advanceTimeBy(DELAY_UPDATE_PENDING_SHORTCUTS)
        verifyBlocking(shortcutManager) { updatePendingShortcuts() }
        verifyNoMoreInteractions(shortcutManager)
        coroutineScope.advanceUntilIdle()
        verifyNoMoreInteractions(shortcutManager)
    }
}
