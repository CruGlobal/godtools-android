package org.cru.godtools.tract.ui.controller

import android.view.LayoutInflater
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.testing.TestLifecycleOwner
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.Called
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.ccci.gto.android.common.androidx.lifecycle.ConstrainedStateLifecycleOwner
import org.cru.godtools.base.tool.analytics.model.ContentAnalyticsEventAnalyticsActionEvent
import org.cru.godtools.shared.tool.parser.model.AnalyticsEvent
import org.cru.godtools.shared.tool.parser.model.Manifest
import org.cru.godtools.tool.tract.databinding.TractPageHeroBinding
import org.greenrobot.eventbus.EventBus
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalCoroutinesApi::class)
class HeroControllerTest {
    private val baseLifecycleOwner = TestLifecycleOwner(Lifecycle.State.CREATED)
    private val eventBus: EventBus = mockk(relaxUnitFun = true)
    private lateinit var scheduler: TestCoroutineScheduler

    private lateinit var controller: HeroController

    @Before
    fun setup() {
        val dispatcher = StandardTestDispatcher()
        scheduler = dispatcher.scheduler
        Dispatchers.setMain(dispatcher)

        val binding = TractPageHeroBinding.inflate(LayoutInflater.from(ApplicationProvider.getApplicationContext()))
        val pageController = mockk<PageController> {
            every { lifecycleOwner } returns ConstrainedStateLifecycleOwner(baseLifecycleOwner)
            every { eventBus } returns this@HeroControllerTest.eventBus
        }

        controller = HeroController(binding, pageController, mockk())
    }

    @After
    fun cleanup() {
        Dispatchers.resetMain()
    }

    @Test
    fun verifyAnalyticsEventsTriggeredOnResume() {
        val event1 = AnalyticsEvent()
        val event2 = AnalyticsEvent(delay = 1)
        val event3 = AnalyticsEvent(delay = 2)
        controller.model = mockk {
            every { manifest } returns Manifest()
            every { getAnalyticsEvents(any()) } returns listOf(event1, event2, event3)
            every { content } returns emptyList()
        }
        verify { eventBus wasNot Called }

        // event1 with no delay
        baseLifecycleOwner.currentState = Lifecycle.State.RESUMED
        scheduler.runCurrent()
        verify(exactly = 1) { eventBus.post(match<ContentAnalyticsEventAnalyticsActionEvent> { it.event == event1 }) }
        confirmVerified(eventBus)

        // event2 with 1 second delay
        scheduler.advanceTimeBy(1000)
        confirmVerified(eventBus)
        scheduler.runCurrent()
        verify(exactly = 1) { eventBus.post(match<ContentAnalyticsEventAnalyticsActionEvent> { it.event == event2 }) }
        confirmVerified(eventBus)

        // event3 with 2 second delay, lifecycle is paused before event can fire
        scheduler.advanceTimeBy(1000)
        confirmVerified(eventBus)
        baseLifecycleOwner.currentState = Lifecycle.State.STARTED
        scheduler.advanceUntilIdle()
        verify(exactly = 0) { eventBus.post(match<ContentAnalyticsEventAnalyticsActionEvent> { it.event == event3 }) }
        confirmVerified(eventBus)
    }
}
