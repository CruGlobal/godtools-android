package org.cru.godtools.shortcuts

import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.Called
import io.mockk.clearMocks
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.Locale
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.cru.godtools.base.Settings
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.keynote.godtools.android.db.GodToolsDao
import org.robolectric.annotation.Config
import org.robolectric.annotation.Config.NEWEST_SDK
import org.robolectric.annotation.Config.OLDEST_SDK

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalCoroutinesApi::class)
class GodToolsShortcutManagerDispatcherTest {
    // various flows
    private val primaryLanguageFlow = MutableSharedFlow<Locale>(replay = 1, extraBufferCapacity = 20)
    private val parallelLanguageFlow = MutableSharedFlow<Locale?>(replay = 1, extraBufferCapacity = 20)
    private val invalidationFlow = MutableSharedFlow<Unit>(extraBufferCapacity = 20)

    @Before
    fun setupFlows() {
        assertTrue(primaryLanguageFlow.tryEmit(Settings.defaultLanguage))
        assertTrue(parallelLanguageFlow.tryEmit(null))
    }

    private val dao: GodToolsDao = mockk {
        every { invalidationFlow(*anyVararg(), emitOnStart = false) } returns invalidationFlow
        every { invalidationFlow(*anyVararg(), emitOnStart = true) } returns invalidationFlow.onStart { emit(Unit) }
    }
    private val settings: Settings = mockk {
        every { primaryLanguageFlow } returns this@GodToolsShortcutManagerDispatcherTest.primaryLanguageFlow
        every { parallelLanguageFlow } returns this@GodToolsShortcutManagerDispatcherTest.parallelLanguageFlow
    }
    private val shortcutManager: GodToolsShortcutManager = mockk(relaxUnitFun = true)

    private fun TestScope.testDispatcher(block: (GodToolsShortcutManager.Dispatcher) -> Unit) {
        val dispatcher = GodToolsShortcutManager.Dispatcher(shortcutManager, dao, settings, this)
        block(dispatcher)
        dispatcher.shutdown()
    }

    @Test
    fun verifyUpdatePendingToolShortcuts() = runTest {
        testDispatcher { dispatcher ->
            dispatcher.updateShortcutsActor.close()

            // trigger update
            assertTrue(invalidationFlow.tryEmit(Unit))
            verify { shortcutManager wasNot Called }
            advanceTimeBy(DELAY_UPDATE_PENDING_SHORTCUTS)
            runCurrent()
            coVerify(exactly = 1) { shortcutManager.updatePendingShortcuts() }
            confirmVerified(shortcutManager)
            clearMocks(shortcutManager)

            // trigger multiple updates simultaneously, it should conflate to a single update
            assertTrue(invalidationFlow.tryEmit(Unit))
            advanceTimeBy(1)
            runCurrent()
            verify { shortcutManager wasNot Called }
            assertTrue(invalidationFlow.tryEmit(Unit))
            advanceTimeBy(DELAY_UPDATE_PENDING_SHORTCUTS)
            runCurrent()
            coVerify(exactly = 1) { shortcutManager.updatePendingShortcuts() }
            confirmVerified(shortcutManager)
            advanceUntilIdle()
            confirmVerified(shortcutManager)
        }
    }

    // region updateShortcutsActor
    @Test
    @Config(sdk = [Build.VERSION_CODES.N_MR1, NEWEST_SDK])
    fun `updateShortcutsActor - Triggers once on startup`() = runTest {
        testDispatcher { dispatcher ->
            dispatcher.updatePendingShortcutsJob.cancel()
            verify { shortcutManager wasNot Called }
            advanceTimeBy(DELAY_UPDATE_SHORTCUTS)
            verify { shortcutManager wasNot Called }
            runCurrent()
            coVerify(exactly = 1) { shortcutManager.updateShortcuts() }
            advanceUntilIdle()
            confirmVerified(shortcutManager)
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N_MR1, NEWEST_SDK])
    fun `updateShortcutsActor - Trigger on primaryLanguage Update`() = runTest {
        testDispatcher { dispatcher ->
            dispatcher.updatePendingShortcutsJob.cancel()
            advanceUntilIdle()
            clearMocks(shortcutManager)

            // trigger a primary language update
            assertTrue(primaryLanguageFlow.tryEmit(Locale.ENGLISH))
            verify { shortcutManager wasNot Called }
            advanceUntilIdle()
            coVerify(exactly = 1) { shortcutManager.updateShortcuts() }
            confirmVerified(shortcutManager)
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N_MR1, NEWEST_SDK])
    fun `updateShortcutsActor - Trigger on parallelLanguage Update`() = runTest {
        testDispatcher { dispatcher ->
            dispatcher.updatePendingShortcutsJob.cancel()
            advanceUntilIdle()
            clearMocks(shortcutManager)

            // trigger a parallel language update
            assertTrue(parallelLanguageFlow.tryEmit(null))
            verify { shortcutManager wasNot Called }
            advanceUntilIdle()
            coVerify(exactly = 1) { shortcutManager.updateShortcuts() }
            confirmVerified(shortcutManager)
        }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N_MR1, NEWEST_SDK])
    fun `updateShortcutsActor - Aggregates multiple events`() = runTest {
        testDispatcher { dispatcher ->
            dispatcher.updatePendingShortcutsJob.cancel()

            // trigger multiple updates simultaneously, it should aggregate to a single update
            assertTrue(dispatcher.updateShortcutsActor.trySend(Unit).isSuccess)
            assertTrue(primaryLanguageFlow.tryEmit(Locale.ENGLISH))
            assertTrue(parallelLanguageFlow.tryEmit(null))
            advanceTimeBy(DELAY_UPDATE_SHORTCUTS - 1)
            verify { shortcutManager wasNot Called }
            assertTrue(dispatcher.updateShortcutsActor.trySend(Unit).isSuccess)
            assertTrue(dispatcher.updateShortcutsActor.trySend(Unit).isSuccess)
            assertTrue(primaryLanguageFlow.tryEmit(Locale.ENGLISH))
            assertTrue(parallelLanguageFlow.tryEmit(null))
            advanceTimeBy(DELAY_UPDATE_SHORTCUTS)
            verify { shortcutManager wasNot Called }
            runCurrent()
            coVerify(exactly = 1) { shortcutManager.updateShortcuts() }
            advanceUntilIdle()
            confirmVerified(shortcutManager)
        }
    }

    @Test
    @Config(sdk = [OLDEST_SDK, Build.VERSION_CODES.N])
    fun `updateShortcutsActor - Not Available For Old Sdks`() = runTest {
        testDispatcher { dispatcher ->
            dispatcher.updatePendingShortcutsJob.cancel()
            advanceUntilIdle()
            assertTrue(
                "Ensure actor can still accept requests, even though they are no-ops",
                dispatcher.updateShortcutsActor.trySend(Unit).isSuccess
            )
            advanceUntilIdle()
            verify { shortcutManager wasNot Called }
        }
    }
    // endregion updateShortcutsActor
}
