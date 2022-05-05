package org.cru.godtools.shortcuts

import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.every
import io.mockk.mockk
import java.util.Locale
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.cru.godtools.base.Settings
import org.cru.godtools.model.event.AttachmentUpdateEvent
import org.cru.godtools.model.event.ToolUpdateEvent
import org.cru.godtools.model.event.TranslationUpdateEvent
import org.greenrobot.eventbus.EventBus
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.keynote.godtools.android.db.GodToolsDao
import org.mockito.kotlin.any
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions
import org.robolectric.annotation.Config
import org.robolectric.annotation.Config.NEWEST_SDK
import org.robolectric.annotation.Config.OLDEST_SDK

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalCoroutinesApi::class)
class GodToolsShortcutManagerDispatcherTest {
    private val invalidations = MutableSharedFlow<Unit>(extraBufferCapacity = 20)
    private val dao: GodToolsDao = mockk {
        every { invalidationFlow(*anyVararg()) } returns invalidations
    }
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

        dispatcher = GodToolsShortcutManager.Dispatcher(shortcutManager, dao, mock(), settings, coroutineScope)
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
        assertTrue(invalidations.tryEmit(Unit))
        verifyNoInteractions(shortcutManager)
        coroutineScope.advanceTimeBy(DELAY_UPDATE_PENDING_SHORTCUTS)
        coroutineScope.runCurrent()
        verifyBlocking(shortcutManager) { updatePendingShortcuts() }
        verifyNoMoreInteractions(shortcutManager)
        clearInvocations(shortcutManager)

        // trigger multiple updates simultaneously, it should conflate to a single update
        assertTrue(invalidations.tryEmit(Unit))
        coroutineScope.advanceTimeBy(1)
        coroutineScope.runCurrent()
        verifyNoInteractions(shortcutManager)
        assertTrue(invalidations.tryEmit(Unit))
        coroutineScope.advanceTimeBy(DELAY_UPDATE_PENDING_SHORTCUTS)
        coroutineScope.runCurrent()
        verifyBlocking(shortcutManager) { updatePendingShortcuts() }
        verifyNoMoreInteractions(shortcutManager)
        coroutineScope.advanceUntilIdle()
        verifyNoMoreInteractions(shortcutManager)
    }

    // region updateShortcutsActor
    @Test
    @Config(sdk = [Build.VERSION_CODES.N_MR1, NEWEST_SDK])
    fun verifyUpdateExistingShortcutsOnPrimaryLanguageUpdate() {
        dispatcher.updatePendingShortcutsJob.cancel()
        assertUpdateExistingShortcutsInitialUpdate()

        // trigger a primary language update
        assertTrue(primaryLanguageFlow.tryEmit(Locale.ENGLISH))
        verifyNoInteractions(shortcutManager)
        coroutineScope.advanceUntilIdle()
        verifyBlocking(shortcutManager) { updateShortcuts() }
        verifyNoMoreInteractions(shortcutManager)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N_MR1, NEWEST_SDK])
    fun verifyUpdateExistingShortcutsOnParallelLanguageUpdate() {
        dispatcher.updatePendingShortcutsJob.cancel()
        assertUpdateExistingShortcutsInitialUpdate()

        // trigger a primary language update
        assertTrue(parallelLanguageFlow.tryEmit(null))
        verifyNoInteractions(shortcutManager)
        coroutineScope.advanceUntilIdle()
        verifyBlocking(shortcutManager) { updateShortcuts() }
        verifyNoMoreInteractions(shortcutManager)
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
    @Config(sdk = [OLDEST_SDK, Build.VERSION_CODES.N])
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

    @Test
    fun `Initialization - EventBus events shouldn't race initialization`() {
        val eventBus = mock<EventBus> {
            on { register(any<GodToolsShortcutManager.Dispatcher>()) } doAnswer {
                // trigger events immediately when the Dispatcher is registered
                val dispatcher: GodToolsShortcutManager.Dispatcher = it.getArgument(0)
                dispatcher.onToolUpdate(ToolUpdateEvent)
                dispatcher.onAttachmentUpdate(AttachmentUpdateEvent)
                dispatcher.onTranslationUpdate(TranslationUpdateEvent)
            }
        }

        val dispatcher = GodToolsShortcutManager.Dispatcher(shortcutManager, dao, eventBus, settings, coroutineScope)
        verify(eventBus).register(dispatcher)
        verifyNoMoreInteractions(eventBus)
        dispatcher.shutdown()
    }
}
