package org.cru.godtools.analytics.firebase

import com.google.android.gms.common.wrappers.InstantApps
import com.google.firebase.analytics.FirebaseAnalytics
import com.okta.authfoundation.client.dto.OidcUserInfo
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.excludeRecords
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import io.mockk.verifySequence
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.ccci.gto.android.common.okta.authfoundation.client.dto.ssoGuid
import org.greenrobot.eventbus.EventBus
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FirebaseAnalyticsServiceTest {
    private val firebase = mockk<FirebaseAnalytics>(relaxUnitFun = true)
    private val eventBus = mockk<EventBus>(relaxUnitFun = true)
    private val userInfoFlow = MutableSharedFlow<OidcUserInfo?>()

    private fun userInfo(guid: String?) = mockk<OidcUserInfo> {
        every { this@mockk.deserializeClaim<Any>(any(), any()) } returns null
        every { ssoGuid } returns guid
    }

    private suspend fun TestScope.useAnalyticsService(block: suspend (FirebaseAnalyticsService) -> Unit) {
        val service = FirebaseAnalyticsService(mockk(), eventBus, userInfoFlow, firebase, this)
        try {
            block(service)
        } finally {
            service.userInfoJob.cancel()
        }
    }

    @Before
    fun setupMocks() {
        mockkStatic("com.google.android.gms.common.wrappers.InstantApps")
        every { InstantApps.isInstantApp(any()) } returns false
    }

    @After
    fun cleanupMocks() {
        unmockkStatic("com.google.android.gms.common.wrappers.InstantApps")
    }

    @Test
    fun verifySetUser() = runTest(UnconfinedTestDispatcher()) {
        excludeRecords { firebase.setUserProperty(any(), any()) }

        useAnalyticsService {
            // initial state
            verify(inverse = true) { firebase.setUserId(any()) }
            confirmVerified(firebase)

            // no active user
            userInfoFlow.emit(null)
            verify(exactly = 1) { firebase.setUserId(null) }
            confirmVerified(firebase)

            // active user
            userInfoFlow.emit(userInfo("GUID"))
            verify(exactly = 1) { firebase.setUserId("GUID") }
            confirmVerified(firebase)

            // user logs out
            userInfoFlow.emit(null)
            verifySequence {
                firebase.setUserId(null)
                firebase.setUserId("GUID")
                firebase.setUserId(null)
            }
        }
    }
}
