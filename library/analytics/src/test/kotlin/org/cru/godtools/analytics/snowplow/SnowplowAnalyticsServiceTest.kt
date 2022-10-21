package org.cru.godtools.analytics.snowplow

import android.content.Context
import android.net.ConnectivityManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.cru.godtools.analytics.model.AnalyticsActionEvent
import org.cru.godtools.analytics.model.AnalyticsSystem
import org.cru.godtools.user.data.UserManager
import org.greenrobot.eventbus.EventBus
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows

@RunWith(AndroidJUnit4::class)
class SnowplowAnalyticsServiceTest {
    private val userManager: UserManager = mockk {
        every { userFlow } returns flowOf(null)
    }

    @Before
    fun setupShadows() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        Shadows.shadowOf(context.getSystemService(ConnectivityManager::class.java)).setActiveNetworkInfo(null)
    }

    @Test
    fun testInitializationEventbusRaceCondition() {
        val eventBus = mockk<EventBus> {
            every { register(any()) } answers {
                // simulate triggering an analytics event immediately after registering with eventbus before the service
                // can finish initializing.
                (it.invocation.args[0] as SnowplowAnalyticsService)
                    .onAnalyticsEvent(AnalyticsActionEvent("test", system = AnalyticsSystem.SNOWPLOW))
            }
        }
        SnowplowAnalyticsService(
            ApplicationProvider.getApplicationContext(),
            eventBus,
            mockk(),
            userManager,
        )
    }
}
