package org.cru.godtools.tract.liveshare

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import java.util.Locale
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.ccci.gto.android.common.scarlet.actioncable.model.ConfirmSubscription
import org.ccci.gto.android.common.scarlet.actioncable.model.Identifier
import org.cru.godtools.api.TractShareService
import org.cru.godtools.api.TractShareService.Companion.CHANNEL_PUBLISHER
import org.cru.godtools.api.TractShareService.Companion.PARAM_CHANNEL_ID
import org.cru.godtools.api.model.NavigationEvent
import org.junit.Rule

private const val CHANNEL_ID = "channel"

@OptIn(ExperimentalCoroutinesApi::class)
class TractPublisherControllerTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val subscriptionConfirmations = Channel<ConfirmSubscription>(Channel.UNLIMITED)
    private val service: TractShareService = mockk {
        every { subscribe(any()) } just Runs
        every { unsubscribe(any()) } just Runs
        every { sendEvent(any()) } just Runs
        every { webSocketEvents() } returns Channel()
        every { subscriptionConfirmation() } returns subscriptionConfirmations
        every { publisherInfo() } returns Channel()
    }
    private val testScope = TestScope()

    private lateinit var controller: TractPublisherController

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScope.testScheduler))

        controller = TractPublisherController(
            service,
            mockk(relaxed = true),
            SavedStateHandle(mapOf(PARAM_CHANNEL_ID to CHANNEL_ID))
        )
        controller.started = true
    }

    @AfterTest
    fun cleanup() {
        controller.started = false
        Dispatchers.resetMain()
    }

    // region sendNavigationEvent()
    @Test
    fun `sendNavigationEvent() - Repeated event is only sent once`() = testScope.runTest {
        val event = NavigationEvent("kgp", Locale.ENGLISH, 3)
        controller.sendNavigationEvent(event)
        controller.sendNavigationEvent(NavigationEvent("kgp", Locale.ENGLISH, 3))

        verify(exactly = 1) { service.sendEvent(match { it.data == event }) }
    }

    @Test
    fun `sendNavigationEvent() - Subscription confirmed - Resends last event`() = testScope.runTest {
        val event = NavigationEvent("kgp", Locale.ENGLISH, 3)
        controller.sendNavigationEvent(event)

        subscriptionConfirmations.send(
            mockk { every { identifier } returns Identifier(CHANNEL_PUBLISHER, mapOf(PARAM_CHANNEL_ID to CHANNEL_ID)) }
        )
        verify(exactly = 2) { service.sendEvent(match { it.data == event }) }
    }
    // endregion sendNavigationEvent()
}
