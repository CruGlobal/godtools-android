package org.cru.godtools.shortcuts

import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.util.Locale
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.cru.godtools.base.Settings
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalCoroutinesApi::class)
class GodToolsShortcutManagerDispatcherTest {
    private lateinit var settings: Settings
    private lateinit var shortcutManager: GodToolsShortcutManager
    private val coroutineScope = TestScope()

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
    }

    @Test
    fun verifyUpdatePendingToolShortcuts() {
        dispatcher.updateShortcutsActor.close()

        // update doesn't trigger before requested
        coroutineScope.advanceUntilIdle()
        coroutineScope.advanceTimeBy(DELAY_UPDATE_PENDING_SHORTCUTS)
        verifyNoInteractions(shortcutManager)

        // trigger update
        assertTrue(dispatcher.updatePendingShortcutsActor.trySend(Unit).isSuccess)
        verifyNoInteractions(shortcutManager)
        coroutineScope.advanceTimeBy(DELAY_UPDATE_PENDING_SHORTCUTS)
        coroutineScope.runCurrent()
        verifyBlocking(shortcutManager) { updatePendingShortcuts() }
        verifyNoMoreInteractions(shortcutManager)
        clearInvocations(shortcutManager)

        // trigger multiple updates simultaneously, it should conflate to a single update
        assertTrue(dispatcher.updatePendingShortcutsActor.trySend(Unit).isSuccess)
        coroutineScope.advanceTimeBy(1)
        coroutineScope.runCurrent()
        verifyNoInteractions(shortcutManager)
        assertTrue(dispatcher.updatePendingShortcutsActor.trySend(Unit).isSuccess)
        coroutineScope.advanceTimeBy(DELAY_UPDATE_PENDING_SHORTCUTS)
        coroutineScope.runCurrent()
        verifyBlocking(shortcutManager) { updatePendingShortcuts() }
        verifyNoMoreInteractions(shortcutManager)
        coroutineScope.advanceUntilIdle()
        verifyNoMoreInteractions(shortcutManager)
    }

    // region updateShortcutsActor
    @Test
    @Config(sdk = [Build.VERSION_CODES.N_MR1, Config.NEWEST_SDK])
    fun verifyUpdateExistingShortcutsOnPrimaryLanguageUpdate() {
        dispatcher.updatePendingShortcutsActor.close()
        assertUpdateExistingShortcutsInitialUpdate()

        // trigger a primary language update
        assertTrue(primaryLanguageFlow.tryEmit(Locale.ENGLISH))
        verifyNoInteractions(shortcutManager)
        coroutineScope.advanceUntilIdle()
        verifyBlocking(shortcutManager) { updateShortcuts() }
        verifyNoMoreInteractions(shortcutManager)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N_MR1, Config.NEWEST_SDK])
    fun verifyUpdateExistingShortcutsOnParallelLanguageUpdate() {
        dispatcher.updatePendingShortcutsActor.close()
        assertUpdateExistingShortcutsInitialUpdate()

        // trigger a primary language update
        assertTrue(parallelLanguageFlow.tryEmit(null))
        verifyNoInteractions(shortcutManager)
        coroutineScope.advanceUntilIdle()
        verifyBlocking(shortcutManager) { updateShortcuts() }
        verifyNoMoreInteractions(shortcutManager)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N_MR1, Config.NEWEST_SDK])
    fun verifyUpdateExistingShortcutsAggregateMultiple() = runTest {
        dispatcher.updatePendingShortcutsActor.close()
        assertUpdateExistingShortcutsInitialUpdate()

        // trigger multiple updates simultaneously, it should aggregate to a single update
        assertTrue(dispatcher.updateShortcutsActor.trySend(Unit).isSuccess)
        assertTrue(primaryLanguageFlow.tryEmit(Locale.ENGLISH))
        assertTrue(parallelLanguageFlow.tryEmit(null))
        coroutineScope.advanceTimeBy(DELAY_UPDATE_SHORTCUTS - 4000)
        verifyNoInteractions(shortcutManager)
        assertTrue(dispatcher.updateShortcutsActor.trySend(Unit).isSuccess)
        assertTrue(dispatcher.updateShortcutsActor.trySend(Unit).isSuccess)
        assertTrue(primaryLanguageFlow.tryEmit(Locale.ENGLISH))
        assertTrue(parallelLanguageFlow.tryEmit(null))
        coroutineScope.advanceTimeBy(DELAY_UPDATE_SHORTCUTS - 1)
        verifyNoInteractions(shortcutManager)
        coroutineScope.advanceUntilIdle()
        verifyBlocking(shortcutManager) { updateShortcuts() }
    }

    @Test
    @Config(sdk = [Config.OLDEST_SDK, Build.VERSION_CODES.N])
    fun verifyUpdateExistingShortcutsNotAvailableForOldSdks() {
        coroutineScope.advanceUntilIdle()
        assertTrue(
            "Ensure actor can still accept requests, even though they are no-ops",
            dispatcher.updateShortcutsActor.trySend(Unit).isSuccess
        )
        coroutineScope.advanceUntilIdle()
        verifyNoInteractions(shortcutManager)
    }

    private fun assertUpdateExistingShortcutsInitialUpdate() {
        // ensure update shortcuts is initially delayed
        coroutineScope.advanceTimeBy(DELAY_UPDATE_SHORTCUTS)
        verifyNoInteractions(shortcutManager)
        coroutineScope.runCurrent()
        verifyBlocking(shortcutManager) { updateShortcuts() }
        coroutineScope.advanceUntilIdle()
        verifyNoMoreInteractions(shortcutManager)
        clearInvocations(shortcutManager)
    }
    // endregion updateShortcutsActor
}
