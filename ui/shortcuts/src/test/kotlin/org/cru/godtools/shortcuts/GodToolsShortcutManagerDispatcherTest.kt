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
import org.cru.godtools.db.repository.AttachmentsRepository
import org.cru.godtools.db.repository.ToolsRepository
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
    private val attachmentsChangeFlow = MutableSharedFlow<Any?>(extraBufferCapacity = 20)
    private val toolsChangeFlow = MutableSharedFlow<Any?>(extraBufferCapacity = 20)
    private val invalidationFlow = MutableSharedFlow<Unit>(extraBufferCapacity = 20)

    @Before
    fun setupFlows() {
        assertTrue(primaryLanguageFlow.tryEmit(Settings.defaultLanguage))
        assertTrue(parallelLanguageFlow.tryEmit(null))
    }

    private val attachmentsRepository: AttachmentsRepository = mockk {
        every { attachmentsChangeFlow(false) } returns attachmentsChangeFlow
        every { attachmentsChangeFlow(true) } returns attachmentsChangeFlow.onStart { emit(Unit) }
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
    private val testScope = TestScope()
    private val toolsRepository: ToolsRepository = mockk {
        every { toolsChangeFlow(false) } returns toolsChangeFlow
        every { toolsChangeFlow(true) } returns toolsChangeFlow.onStart { emit(Unit) }
    }

    private val dispatcher by lazy {
        GodToolsShortcutManager.Dispatcher(
            shortcutManager,
            attachmentsRepository = attachmentsRepository,
            dao = dao,
            settings = settings,
            toolsRepository = toolsRepository,
            coroutineScope = testScope.backgroundScope
        )
    }

    @Test
    fun verifyUpdatePendingToolShortcuts() = testScope.runTest {
        dispatcher.updateShortcutsJob.cancel()

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
        advanceTimeBy(10 * DELAY_UPDATE_PENDING_SHORTCUTS)
        confirmVerified(shortcutManager)
    }

    // region updateShortcutsActor
    @Test
    @Config(sdk = [Build.VERSION_CODES.N_MR1, NEWEST_SDK])
    fun `updateShortcutsJob - Triggers once on startup`() = testScope.runTest {
        dispatcher.updatePendingShortcutsJob.cancel()
        verify { shortcutManager wasNot Called }
        advanceTimeBy(DELAY_UPDATE_SHORTCUTS)
        verify { shortcutManager wasNot Called }
        runCurrent()
        coVerify(exactly = 1) { shortcutManager.updateShortcuts() }
        advanceUntilIdle()
        confirmVerified(shortcutManager)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N_MR1, NEWEST_SDK])
    fun `updateShortcutsJob - Trigger on primaryLanguage Update`() = testScope.runTest {
        dispatcher.updatePendingShortcutsJob.cancel()
        runCurrent()
        clearMocks(shortcutManager)

        // trigger a primary language update
        assertTrue(primaryLanguageFlow.tryEmit(Locale.ENGLISH))
        verify { shortcutManager wasNot Called }
        advanceTimeBy(10 * DELAY_UPDATE_SHORTCUTS)
        coVerify(exactly = 1) { shortcutManager.updateShortcuts() }
        confirmVerified(shortcutManager)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N_MR1, NEWEST_SDK])
    fun `updateShortcutsJob - Trigger on parallelLanguage Update`() = testScope.runTest {
        dispatcher.updatePendingShortcutsJob.cancel()
        runCurrent()
        clearMocks(shortcutManager)

        // trigger a parallel language update
        assertTrue(parallelLanguageFlow.tryEmit(null))
        verify { shortcutManager wasNot Called }
        advanceTimeBy(10 * DELAY_UPDATE_SHORTCUTS)
        coVerify(exactly = 1) { shortcutManager.updateShortcuts() }
        confirmVerified(shortcutManager)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N_MR1, NEWEST_SDK])
    fun `updateShortcutsJob - Trigger on attachments Update`() = testScope.runTest {
        dispatcher.updatePendingShortcutsJob.cancel()
        runCurrent()
        clearMocks(shortcutManager)

        // trigger an Attachments update
        assertTrue(attachmentsChangeFlow.tryEmit(Unit))
        verify { shortcutManager wasNot Called }
        advanceTimeBy(10 * DELAY_UPDATE_SHORTCUTS)
        coVerify(exactly = 1) { shortcutManager.updateShortcuts() }
        confirmVerified(shortcutManager)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N_MR1, NEWEST_SDK])
    fun `updateShortcutsJob - Trigger on tools Update`() = testScope.runTest {
        dispatcher.updatePendingShortcutsJob.cancel()
        runCurrent()
        clearMocks(shortcutManager)

        // trigger a Tools update
        assertTrue(toolsChangeFlow.tryEmit(Unit))
        verify { shortcutManager wasNot Called }
        advanceTimeBy(10 * DELAY_UPDATE_SHORTCUTS)
        coVerify(exactly = 1) { shortcutManager.updateShortcuts() }
        confirmVerified(shortcutManager)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N_MR1, NEWEST_SDK])
    fun `updateShortcutsJob - Aggregates multiple events`() = testScope.runTest {
        dispatcher.updatePendingShortcutsJob.cancel()

        // trigger multiple updates simultaneously, it should aggregate to a single update
        assertTrue(primaryLanguageFlow.tryEmit(Locale.ENGLISH))
        assertTrue(parallelLanguageFlow.tryEmit(null))
        assertTrue(attachmentsChangeFlow.tryEmit(Unit))
        assertTrue(toolsChangeFlow.tryEmit(Unit))
        assertTrue(invalidationFlow.tryEmit(Unit))
        advanceTimeBy(DELAY_UPDATE_SHORTCUTS - 1)
        verify { shortcutManager wasNot Called }
        assertTrue(primaryLanguageFlow.tryEmit(Locale.ENGLISH))
        assertTrue(parallelLanguageFlow.tryEmit(null))
        assertTrue(attachmentsChangeFlow.tryEmit(Unit))
        assertTrue(attachmentsChangeFlow.tryEmit(Unit))
        assertTrue(toolsChangeFlow.tryEmit(Unit))
        assertTrue(toolsChangeFlow.tryEmit(Unit))
        assertTrue(invalidationFlow.tryEmit(Unit))
        assertTrue(invalidationFlow.tryEmit(Unit))
        advanceTimeBy(DELAY_UPDATE_SHORTCUTS)
        verify { shortcutManager wasNot Called }
        runCurrent()
        coVerify(exactly = 1) { shortcutManager.updateShortcuts() }
        advanceTimeBy(10 * DELAY_UPDATE_SHORTCUTS)
        confirmVerified(shortcutManager)
    }

    @Test
    @Config(sdk = [OLDEST_SDK, Build.VERSION_CODES.N])
    fun `updateShortcutsJob - Not Available For Old Sdks`() = testScope.runTest {
        dispatcher.updatePendingShortcutsJob.cancel()
        runCurrent()
        assertTrue(dispatcher.updateShortcutsJob.isCompleted)
        verify { shortcutManager wasNot Called }
    }
    // endregion updateShortcutsActor
}
