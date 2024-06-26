package org.cru.godtools.shortcuts

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.Called
import io.mockk.clearMocks
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.excludeRecords
import io.mockk.mockk
import io.mockk.verify
import java.util.Locale
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.cru.godtools.base.Settings
import org.cru.godtools.db.repository.AttachmentsRepository
import org.cru.godtools.db.repository.ToolsRepository
import org.cru.godtools.db.repository.TranslationsRepository
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalCoroutinesApi::class)
class GodToolsShortcutManagerDispatcherTest {
    // various flows
    private val appLanguageFlow = MutableStateFlow(Locale.ENGLISH)
    private val attachmentsChangeFlow = MutableSharedFlow<Any?>(extraBufferCapacity = 20)
    private val toolsChangeFlow = MutableSharedFlow<Any?>(extraBufferCapacity = 20)
    private val translationsChangeFlow = MutableSharedFlow<Any?>(extraBufferCapacity = 20)

    private val attachmentsRepository: AttachmentsRepository = mockk {
        every { attachmentsChangeFlow() } returns attachmentsChangeFlow.onStart { emit(Unit) }
    }
    private val settings: Settings = mockk {
        every { appLanguageFlow } returns this@GodToolsShortcutManagerDispatcherTest.appLanguageFlow
    }
    private val shortcutManager: GodToolsShortcutManager = mockk(relaxUnitFun = true) {
        every { isEnabled } returns true
        excludeRecords { isEnabled }
    }
    private val testScope = TestScope()
    private val toolsRepository: ToolsRepository = mockk {
        every { toolsChangeFlow() } returns toolsChangeFlow.onStart { emit(Unit) }
    }
    private val translationsRepository: TranslationsRepository = mockk {
        every { translationsChangeFlow() } returns translationsChangeFlow.onStart { emit(Unit) }
    }

    private val dispatcher by lazy {
        GodToolsShortcutManager.Dispatcher(
            shortcutManager,
            attachmentsRepository = attachmentsRepository,
            settings = settings,
            toolsRepository = toolsRepository,
            translationsRepository = translationsRepository,
            coroutineScope = testScope.backgroundScope
        )
    }

    @Test
    fun verifyUpdatePendingToolShortcuts() = testScope.runTest {
        dispatcher.updateShortcutsJob.cancel()

        // trigger update
        assertTrue(translationsChangeFlow.tryEmit(Unit))
        verify { shortcutManager wasNot Called }
        advanceTimeBy(DELAY_UPDATE_PENDING_SHORTCUTS)
        runCurrent()
        coVerify(exactly = 1) { shortcutManager.updatePendingShortcuts() }
        confirmVerified(shortcutManager)
        clearMocks(shortcutManager)

        // trigger multiple updates simultaneously, it should conflate to a single update
        assertTrue(translationsChangeFlow.tryEmit(Unit))
        advanceTimeBy(1)
        runCurrent()
        verify { shortcutManager wasNot Called }
        assertTrue(translationsChangeFlow.tryEmit(Unit))
        advanceTimeBy(DELAY_UPDATE_PENDING_SHORTCUTS)
        runCurrent()
        coVerify(exactly = 1) { shortcutManager.updatePendingShortcuts() }
        confirmVerified(shortcutManager)
        advanceTimeBy(10 * DELAY_UPDATE_PENDING_SHORTCUTS)
        confirmVerified(shortcutManager)
    }

    // region updateShortcutsJob
    @Test
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
    fun `updateShortcutsJob - Trigger on primaryLanguage Update`() = testScope.runTest {
        dispatcher.updatePendingShortcutsJob.cancel()
        runCurrent()
        clearMocks(shortcutManager)

        // trigger a primary language update
        assertTrue(appLanguageFlow.tryEmit(Locale.FRENCH))
        verify { shortcutManager wasNot Called }
        advanceTimeBy(10 * DELAY_UPDATE_SHORTCUTS)
        coVerify(exactly = 1) { shortcutManager.updateShortcuts() }
        confirmVerified(shortcutManager)
    }

    @Test
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
    fun `updateShortcutsJob - Trigger on translations Update`() = testScope.runTest {
        dispatcher.updatePendingShortcutsJob.cancel()
        runCurrent()
        clearMocks(shortcutManager)

        // trigger a Translations update
        assertTrue(translationsChangeFlow.tryEmit(Unit))
        verify { shortcutManager wasNot Called }
        advanceTimeBy(10 * DELAY_UPDATE_SHORTCUTS)
        coVerify(exactly = 1) { shortcutManager.updateShortcuts() }
        confirmVerified(shortcutManager)
    }

    @Test
    fun `updateShortcutsJob - Aggregates multiple events`() = testScope.runTest {
        dispatcher.updatePendingShortcutsJob.cancel()

        // trigger multiple updates simultaneously, it should aggregate to a single update
        assertTrue(appLanguageFlow.tryEmit(Locale.FRENCH))
        assertTrue(attachmentsChangeFlow.tryEmit(Unit))
        assertTrue(toolsChangeFlow.tryEmit(Unit))
        assertTrue(translationsChangeFlow.tryEmit(Unit))
        advanceTimeBy(DELAY_UPDATE_SHORTCUTS - 1)
        verify { shortcutManager wasNot Called }
        assertTrue(appLanguageFlow.tryEmit(Locale.GERMAN))
        assertTrue(attachmentsChangeFlow.tryEmit(Unit))
        assertTrue(attachmentsChangeFlow.tryEmit(Unit))
        assertTrue(toolsChangeFlow.tryEmit(Unit))
        assertTrue(toolsChangeFlow.tryEmit(Unit))
        assertTrue(translationsChangeFlow.tryEmit(Unit))
        assertTrue(translationsChangeFlow.tryEmit(Unit))
        advanceTimeBy(DELAY_UPDATE_SHORTCUTS)
        verify { shortcutManager wasNot Called }
        runCurrent()
        coVerify(exactly = 1) { shortcutManager.updateShortcuts() }
        advanceTimeBy(10 * DELAY_UPDATE_SHORTCUTS)
        confirmVerified(shortcutManager)
    }
    // endregion updateShortcutsJob
}
