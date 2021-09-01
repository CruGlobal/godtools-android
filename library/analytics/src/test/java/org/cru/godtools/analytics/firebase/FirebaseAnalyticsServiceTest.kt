package org.cru.godtools.analytics.firebase

import android.app.Application
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.analytics.FirebaseAnalytics
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.clearInvocations
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.okta.oidc.net.response.UserInfo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineScope
import org.ccci.gto.android.common.okta.oidc.OktaUserProfileProvider
import org.cru.godtools.base.Settings
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.RETURNS_DEEP_STUBS

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalCoroutinesApi::class)
class FirebaseAnalyticsServiceTest {
    private lateinit var application: Application
    private lateinit var firebase: FirebaseAnalytics
    private lateinit var eventBus: EventBus
    private lateinit var oktaUserProfileProvider: OktaUserProfileProvider
    private lateinit var settings: Settings
    private val userInfoChannel = Channel<UserInfo?>()
    private lateinit var coroutineScope: TestCoroutineScope

    private lateinit var analyticsService: FirebaseAnalyticsService

    @Before
    fun setupMocks() {
        application = mock(defaultAnswer = RETURNS_DEEP_STUBS)
        coroutineScope = TestCoroutineScope()
        eventBus = mock()
        firebase = mock()
        oktaUserProfileProvider = mock {
            on { userInfoFlow(refreshIfStale = any()) } doReturn userInfoChannel.consumeAsFlow()
        }
        settings = mock { on { launchesFlow } doAnswer { flowOf(0) } }

        analyticsService =
            FirebaseAnalyticsService(application, eventBus, oktaUserProfileProvider, settings, firebase, coroutineScope)
    }

    @After
    fun cleanup() {
        coroutineScope.cleanupTestCoroutines()
    }

    @Test(timeout = 10000)
    fun verifySetUser() = runBlocking {
        // initial state
        verify(firebase, never()).setUserId(any())
        clearInvocations(firebase)

        // no active user
        userInfoChannel.send(null)
        verify(firebase).setUserId(null)
        clearInvocations(firebase)

        // active user
        userInfoChannel.send(userInfo("GUID"))
        verify(firebase).setUserId("GUID")
        clearInvocations(firebase)

        // user logs out
        userInfoChannel.send(null)
        verify(firebase).setUserId(null)
    }

    private fun userInfo(guid: String?): UserInfo = UserInfo(JSONObject(mapOf("ssoGuid" to guid)))
}
