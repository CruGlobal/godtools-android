package org.cru.godtools.tract.ui.controller

import android.view.LayoutInflater
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.testing.TestLifecycleOwner
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.argThat
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.ccci.gto.android.common.androidx.lifecycle.ConstrainedStateLifecycleOwner
import org.cru.godtools.base.tool.analytics.model.ContentAnalyticsActionEvent
import org.cru.godtools.tool.model.AnalyticsEvent
import org.cru.godtools.tract.databinding.TractPageHeroBinding
import org.greenrobot.eventbus.EventBus
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.verifyNoInteractions

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalCoroutinesApi::class)
class HeroControllerTest {
    private val baseLifecycleOwner = TestLifecycleOwner(Lifecycle.State.CREATED)
    private lateinit var eventBus: EventBus
    private val mainDispatcher = TestCoroutineDispatcher()

    private lateinit var controller: HeroController

    @Before
    fun setup() {
        Dispatchers.setMain(mainDispatcher)

        eventBus = mock()
        val binding = TractPageHeroBinding.inflate(LayoutInflater.from(ApplicationProvider.getApplicationContext()))
        val pageController: PageController = mock {
            on { lifecycleOwner } doReturn ConstrainedStateLifecycleOwner(baseLifecycleOwner)
            on { eventBus } doReturn eventBus
        }

        controller = HeroController(binding, pageController, mock())
    }

    @After
    fun cleanup() {
        mainDispatcher.cleanupTestCoroutines()
        Dispatchers.resetMain()
    }

    @Test
    fun verifyAnalyticsEventsTriggeredOnResume() {
        val event1 = AnalyticsEvent()
        val event2 = AnalyticsEvent(delay = 1)
        val event3 = AnalyticsEvent(delay = 2)
        controller.model = mock { on { analyticsEvents } doReturn listOf(event1, event2, event3) }
        verifyNoInteractions(eventBus)

        // event1 with no delay
        mainDispatcher.pauseDispatcher()
        baseLifecycleOwner.currentState = Lifecycle.State.RESUMED
        mainDispatcher.runCurrent()
        verify(eventBus).post(argThat<ContentAnalyticsActionEvent> { event == event1 })
        verifyNoMoreInteractions(eventBus)

        // event2 with 1 second delay
        mainDispatcher.advanceTimeBy(999)
        verifyNoMoreInteractions(eventBus)
        mainDispatcher.advanceTimeBy(1)
        verify(eventBus).post(argThat<ContentAnalyticsActionEvent> { event == event2 })
        verifyNoMoreInteractions(eventBus)

        // event3 with 2 second delay, lifecycle is paused before event can fire
        mainDispatcher.advanceTimeBy(999)
        verifyNoMoreInteractions(eventBus)
        baseLifecycleOwner.currentState = Lifecycle.State.STARTED
        mainDispatcher.advanceUntilIdle()
        verify(eventBus, never()).post(argThat<ContentAnalyticsActionEvent> { event == event3 })
        verifyNoMoreInteractions(eventBus)
    }
}
