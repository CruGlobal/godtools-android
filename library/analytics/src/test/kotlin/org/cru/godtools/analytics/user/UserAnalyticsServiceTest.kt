package org.cru.godtools.analytics.user

import io.mockk.Called
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerifyAll
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyAll
import kotlin.test.Test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.cru.godtools.analytics.model.AnalyticsBaseEvent
import org.cru.godtools.analytics.model.AnalyticsSystem
import org.cru.godtools.user.data.Counters
import org.greenrobot.eventbus.EventBus

private const val COUNTER_NAME = "counter"

@OptIn(ExperimentalCoroutinesApi::class)
class UserAnalyticsServiceTest {
    private val eventBus: EventBus = mockk(relaxUnitFun = true)
    private val userCounters: Counters = mockk {
        coEvery { updateCounter(any()) } just Runs
    }
    private val testScope = TestScope()

    private val analyticsService = UserAnalyticsService(eventBus, userCounters, testScope.backgroundScope)

    @Test
    fun verifyRegisteredWithEventBus() = testScope.runTest {
        verifyAll {
            eventBus.register(analyticsService)
        }
    }

    // region onAnalyticsEvent()
    @Test
    fun `onAnalyticsEvent() - ignore non-User events`() = testScope.runTest {
        val event = mockk<AnalyticsBaseEvent> {
            every { isForSystem(AnalyticsSystem.USER) } returns false
        }

        analyticsService.onAnalyticsEvent(event)
        verify { userCounters wasNot Called }
    }

    @Test
    fun `onAnalyticsEvent() - ignore events without a counter name`() = testScope.runTest {
        val event = mockk<AnalyticsBaseEvent> {
            every { isForSystem(AnalyticsSystem.USER) } returns true
            every { userCounterName } returns null
        }

        analyticsService.onAnalyticsEvent(event)
        verify { userCounters wasNot Called }
    }

    @Test
    fun `onAnalyticsEvent() - ignore events with an invalid counter name`() = testScope.runTest {
        val event = mockk<AnalyticsBaseEvent> {
            every { isForSystem(AnalyticsSystem.USER) } returns true
            every { userCounterName } returns COUNTER_NAME
        }
        every { userCounters.isValidCounterName(COUNTER_NAME) } returns false

        analyticsService.onAnalyticsEvent(event)
        runCurrent()
        verifyAll {
            userCounters.isValidCounterName(COUNTER_NAME)
        }
    }

    @Test
    fun `onAnalyticsEvent()`() = testScope.runTest {
        val event = mockk<AnalyticsBaseEvent> {
            every { isForSystem(AnalyticsSystem.USER) } returns true
            every { userCounterName } returns COUNTER_NAME
        }
        every { userCounters.isValidCounterName(COUNTER_NAME) } returns true

        analyticsService.onAnalyticsEvent(event)
        runCurrent()
        coVerifyAll {
            userCounters.isValidCounterName(COUNTER_NAME)
            userCounters.updateCounter(COUNTER_NAME)
        }
    }
    // endregion onAnalyticsEvent()
}
