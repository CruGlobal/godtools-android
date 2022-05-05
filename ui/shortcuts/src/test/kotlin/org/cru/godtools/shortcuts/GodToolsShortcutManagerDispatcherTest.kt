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
import org.keynote.godtools.android.db.GodToolsDao
import org.robolectric.annotation.Config
import org.robolectric.annotation.Config.NEWEST_SDK
import org.robolectric.annotation.Config.OLDEST_SDK

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalCoroutinesApi::class)
class GodToolsShortcutManagerDispatcherTest {
    // various flows
    private val primaryLanguageFlow = MutableSharedFlow<Locale>(extraBufferCapacity = 20)
    private val parallelLanguageFlow = MutableSharedFlow<Locale?>(extraBufferCapacity = 20)
    private val invalidationFlow = MutableSharedFlow<Unit>(extraBufferCapacity = 20)

    private val dao: GodToolsDao = mockk {
        every { invalidationFlow(*anyVararg()) } returns invalidationFlow
    }
    private val settings: Settings = mockk {
        every { primaryLanguageFlow } returns this@GodToolsShortcutManagerDispatcherTest.primaryLanguageFlow
        every { parallelLanguageFlow } returns this@GodToolsShortcutManagerDispatcherTest.parallelLanguageFlow
    }
    private val shortcutManager: GodToolsShortcutManager = mockk(relaxUnitFun = true)
    private val coroutineScope = TestScope()

    private lateinit var dispatcher: GodToolsShortcutManager.Dispatcher

    @Before
    fun setup() {
        dispatcher = GodToolsShortcutManager.Dispatcher(shortcutManager, dao, settings, coroutineScope)
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
        verify { shortcutManager wasNot Called }

        // trigger update
        assertTrue(invalidationFlow.tryEmit(Unit))
        verify { shortcutManager wasNot Called }
        coroutineScope.advanceTimeBy(DELAY_UPDATE_PENDING_SHORTCUTS)
        coroutineScope.runCurrent()
        coVerify { shortcutManager.updatePendingShortcuts() }
        confirmVerified(shortcutManager)
        clearMocks(shortcutManager)

        // trigger multiple updates simultaneously, it should conflate to a single update
        assertTrue(invalidationFlow.tryEmit(Unit))
        coroutineScope.advanceTimeBy(1)
        coroutineScope.runCurrent()
        verify { shortcutManager wasNot Called }
        assertTrue(invalidationFlow.tryEmit(Unit))
        coroutineScope.advanceTimeBy(DELAY_UPDATE_PENDING_SHORTCUTS)
        coroutineScope.runCurrent()
        coVerify { shortcutManager.updatePendingShortcuts() }
        confirmVerified(shortcutManager)
        coroutineScope.advanceUntilIdle()
        confirmVerified(shortcutManager)
    }

    // region updateShortcutsActor
    @Test
    @Config(sdk = [Build.VERSION_CODES.N_MR1, NEWEST_SDK])
    fun verifyUpdateExistingShortcutsOnPrimaryLanguageUpdate() {
        dispatcher.updatePendingShortcutsJob.cancel()
        assertUpdateExistingShortcutsInitialUpdate()

        // trigger a primary language update
        assertTrue(primaryLanguageFlow.tryEmit(Locale.ENGLISH))
        verify { shortcutManager wasNot Called }
        coroutineScope.advanceUntilIdle()
        coVerify { shortcutManager.updateShortcuts() }
        confirmVerified(shortcutManager)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N_MR1, NEWEST_SDK])
    fun verifyUpdateExistingShortcutsOnParallelLanguageUpdate() {
        dispatcher.updatePendingShortcutsJob.cancel()
        assertUpdateExistingShortcutsInitialUpdate()

        // trigger a primary language update
        assertTrue(parallelLanguageFlow.tryEmit(null))
        verify { shortcutManager wasNot Called }
        coroutineScope.advanceUntilIdle()
        coVerify { shortcutManager.updateShortcuts() }
        confirmVerified(shortcutManager)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N_MR1, NEWEST_SDK])
    fun verifyUpdateExistingShortcutsAggregateMultiple() = runTest {
        dispatcher.updatePendingShortcutsJob.cancel()
        assertUpdateExistingShortcutsInitialUpdate()

        // trigger multiple updates simultaneously, it should aggregate to a single update
        assertTrue(dispatcher.updateShortcutsActor.trySend(Unit).isSuccess)
        assertTrue(primaryLanguageFlow.tryEmit(Locale.ENGLISH))
        assertTrue(parallelLanguageFlow.tryEmit(null))
        coroutineScope.advanceTimeBy(DELAY_UPDATE_SHORTCUTS - 4000)
        verify { shortcutManager wasNot Called }
        assertTrue(dispatcher.updateShortcutsActor.trySend(Unit).isSuccess)
        assertTrue(dispatcher.updateShortcutsActor.trySend(Unit).isSuccess)
        assertTrue(primaryLanguageFlow.tryEmit(Locale.ENGLISH))
        assertTrue(parallelLanguageFlow.tryEmit(null))
        coroutineScope.advanceTimeBy(DELAY_UPDATE_SHORTCUTS - 1)
        verify { shortcutManager wasNot Called }
        coroutineScope.advanceUntilIdle()
        coVerify { shortcutManager.updateShortcuts() }
    }

    @Test
    @Config(sdk = [OLDEST_SDK, Build.VERSION_CODES.N])
    fun verifyUpdateExistingShortcutsNotAvailableForOldSdks() {
        coroutineScope.advanceUntilIdle()
        assertTrue(
            "Ensure actor can still accept requests, even though they are no-ops",
            dispatcher.updateShortcutsActor.trySend(Unit).isSuccess
        )
        coroutineScope.advanceUntilIdle()
        verify { shortcutManager wasNot Called }
    }

    private fun assertUpdateExistingShortcutsInitialUpdate() {
        // ensure update shortcuts is initially delayed
        coroutineScope.advanceTimeBy(DELAY_UPDATE_SHORTCUTS)
        verify { shortcutManager wasNot Called }
        coroutineScope.runCurrent()
        coVerify { shortcutManager.updateShortcuts() }
        coroutineScope.advanceUntilIdle()
        confirmVerified(shortcutManager)
        clearMocks(shortcutManager)
    }
    // endregion updateShortcutsActor
}
