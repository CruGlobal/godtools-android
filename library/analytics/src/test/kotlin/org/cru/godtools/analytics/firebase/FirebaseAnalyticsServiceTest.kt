package org.cru.godtools.analytics.firebase

import com.google.android.gms.common.wrappers.InstantApps
import com.google.firebase.analytics.FirebaseAnalytics
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.excludeRecords
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import io.mockk.verifyOrder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.cru.godtools.account.GodToolsAccountManager
import org.cru.godtools.model.User
import org.cru.godtools.user.data.UserManager
import org.greenrobot.eventbus.EventBus
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FirebaseAnalyticsServiceTest {
    private val userFlow = MutableSharedFlow<User?>()

    private val accountManager: GodToolsAccountManager = mockk {
        every { isAuthenticatedFlow() } returns flowOf(false)
    }
    private val eventBus: EventBus = mockk(relaxUnitFun = true)
    private val firebase: FirebaseAnalytics = mockk(relaxUnitFun = true)
    private val userManager: UserManager = mockk {
        every { userFlow } returns this@FirebaseAnalyticsServiceTest.userFlow
    }
    private val testScope = TestScope()

    private lateinit var analyticsService: FirebaseAnalyticsService

    @Before
    fun setupMocks() {
        mockkStatic("com.google.android.gms.common.wrappers.InstantApps") {
            every { InstantApps.isInstantApp(any()) } returns false

            analyticsService = FirebaseAnalyticsService(
                mockk(),
                accountManager,
                eventBus,
                userManager,
                firebase,
                testScope.backgroundScope
            )
        }
    }

    @Test
    fun verifySetUser() = testScope.runTest {
        excludeRecords { firebase.setUserProperty(any(), any()) }

        // initial state
        runCurrent()
        verify(exactly = 0) { firebase.setUserId(any()) }
        confirmVerified(firebase)

        // no active user
        userFlow.emit(null)
        runCurrent()
        verify(exactly = 1) { firebase.setUserId(null) }
        confirmVerified(firebase)

        // active user
        userFlow.emit(User(ssoGuid = "GUID"))
        runCurrent()
        verify(exactly = 1) { firebase.setUserId("GUID") }
        confirmVerified(firebase)

        // user logs out
        userFlow.emit(null)
        runCurrent()
        verifyOrder {
            firebase.setUserId(null)
            firebase.setUserId("GUID")
            firebase.setUserId(null)
        }
    }
}
