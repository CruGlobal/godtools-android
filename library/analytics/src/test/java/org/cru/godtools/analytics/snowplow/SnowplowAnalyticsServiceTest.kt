package org.cru.godtools.analytics.snowplow

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.okta.oidc.net.response.UserInfo
import kotlinx.coroutines.flow.MutableSharedFlow
import okhttp3.OkHttpClient
import org.ccci.gto.android.common.okta.oidc.OktaUserProfileProvider
import org.cru.godtools.analytics.model.AnalyticsActionEvent
import org.cru.godtools.analytics.model.AnalyticsSystem
import org.greenrobot.eventbus.EventBus
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub

@RunWith(AndroidJUnit4::class)
class SnowplowAnalyticsServiceTest {
    private lateinit var eventBus: EventBus
    private lateinit var okhttp: OkHttpClient
    private lateinit var oktaUserProfileProvider: OktaUserProfileProvider
    private val userInfoFlow = MutableSharedFlow<UserInfo?>(replay = 1)

    @Before
    fun setupMocks() {
        userInfoFlow.tryEmit(null)
        eventBus = mock()
        okhttp = mock()
        oktaUserProfileProvider = mock {
            on { userInfoFlow(refreshIfStale = false) } doReturn userInfoFlow
        }
    }

    @Test
    fun testInitializationEventbusRaceCondition() {
        eventBus.stub {
            on { register(any<SnowplowAnalyticsService>()) } doAnswer {
                // simulate triggering an analytics event immediately after registering with eventbus before the service
                // can finish initializing.
                it.getArgument<SnowplowAnalyticsService>(0)!!
                    .onAnalyticsEvent(AnalyticsActionEvent("test", system = AnalyticsSystem.SNOWPLOW))
            }
        }
        SnowplowAnalyticsService(
            ApplicationProvider.getApplicationContext(),
            eventBus,
            okhttp,
            oktaUserProfileProvider
        )
    }
}
