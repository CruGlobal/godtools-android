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
import org.cru.godtools.user.activity.UserActivityManager
import org.greenrobot.eventbus.EventBus

private const val COUNTER_NAME = "counter"

@OptIn(ExperimentalCoroutinesApi::class)
class UserAnalyticsServiceTest {
    private val eventBus: EventBus = mockk(relaxUnitFun = true)
    private val userActivityManager: UserActivityManager = mockk {
        coEvery { updateCounter(any()) } just Runs
    }
    private val testScope = TestScope()

    private val analyticsService = UserAnalyticsService(eventBus, userActivityManager, testScope.backgroundScope)

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
        verify { userActivityManager wasNot Called }
    }

    @Test
    fun `onAnalyticsEvent() - ignore events without a counter name`() = testScope.runTest {
        val event = mockk<AnalyticsBaseEvent> {
            every { isForSystem(AnalyticsSystem.USER) } returns true
            every { userCounterName } returns null
        }

        analyticsService.onAnalyticsEvent(event)
        verify { userActivityManager wasNot Called }
    }

    @Test
    fun `onAnalyticsEvent() - ignore events with an invalid counter name`() = testScope.runTest {
        val event = mockk<AnalyticsBaseEvent> {
            every { isForSystem(AnalyticsSystem.USER) } returns true
            every { userCounterName } returns COUNTER_NAME
        }
        every { userActivityManager.isValidCounterName(COUNTER_NAME) } returns false

        analyticsService.onAnalyticsEvent(event)
        runCurrent()
        verifyAll {
            userActivityManager.isValidCounterName(COUNTER_NAME)
        }
    }

    @Test
    fun `onAnalyticsEvent()`() = testScope.runTest {
        val event = mockk<AnalyticsBaseEvent> {
            every { isForSystem(AnalyticsSystem.USER) } returns true
            every { userCounterName } returns COUNTER_NAME
        }
        every { userActivityManager.isValidCounterName(COUNTER_NAME) } returns true

        analyticsService.onAnalyticsEvent(event)
        runCurrent()
        coVerifyAll {
            userActivityManager.isValidCounterName(COUNTER_NAME)
            userActivityManager.updateCounter(COUNTER_NAME)
        }
    }
    // endregion onAnalyticsEvent()
}
