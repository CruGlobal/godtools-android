package org.cru.godtools.ui.login

import android.app.Application
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.slack.circuit.test.FakeNavigator
import com.slack.circuit.test.test
import io.mockk.every
import io.mockk.mockk
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.ccci.gto.android.common.androidx.compose.ui.platform.AndroidUiDispatcherUtil
import org.cru.godtools.account.AccountType
import org.cru.godtools.account.GodToolsAccountManager
import org.cru.godtools.account.LoginResponse
import org.cru.godtools.ui.login.LoginPresenter.UiEvent
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
class LoginPresenterTest {
    private val isAuthenticatedFlow = MutableStateFlow(false)

    private val accountManager: GodToolsAccountManager = mockk {
        every { isAuthenticatedFlow } returns this@LoginPresenterTest.isAuthenticatedFlow
    }
    private val loginLauncherProducer = FakeLoginLauncherProducer()
    private val navigator = FakeNavigator(LoginScreen())

    private fun createPresenter(screen: LoginScreen = LoginScreen()) = LoginPresenter(
        accountManager = accountManager,
        loginLauncherProducer = loginLauncherProducer,
        screen = screen,
        navigator = navigator,
    )

    @AfterTest
    fun cleanup() {
        AndroidUiDispatcherUtil.runScheduledDispatches()
        loginLauncherProducer.launches.ensureAllEventsConsumed()
        navigator.assertGoToIsEmpty()
        navigator.assertPopIsEmpty()
        navigator.assertResetRootIsEmpty()
    }

    // region State.createAccount
    @Test
    fun `State - createAccount - login`() = runTest {
        createPresenter(LoginScreen(createAccount = false)).test {
            assertFalse(awaitItem().createAccount)
            assertEquals(false, loginLauncherProducer.lastCreateAccount)
        }
    }

    @Test
    fun `State - createAccount - create account`() = runTest {
        createPresenter(LoginScreen(createAccount = true)).test {
            assertTrue(awaitItem().createAccount)
            assertEquals(true, loginLauncherProducer.lastCreateAccount)
        }
    }
    // endregion State.createAccount

    // region State.loginError
    @Test
    fun `State - loginError - login fails`() = runTest {
        createPresenter().test {
            assertNull(awaitItem().loginError)

            loginLauncherProducer.respond(LoginResponse.Error.UserNotFound)
            assertEquals(LoginResponse.Error.UserNotFound, awaitItem().loginError)
        }
    }

    @Test
    fun `State - loginError - login succeeds`() = runTest {
        createPresenter().test {
            assertNull(awaitItem().loginError)

            loginLauncherProducer.respond(LoginResponse.Success)
            expectNoEvents()
        }
    }
    // endregion State.loginError

    // region SideEffect - Close When Authenticated
    @Test
    fun `SideEffect - Close When Authenticated - login succeeds`() = runTest {
        createPresenter().test {
            awaitItem()

            loginLauncherProducer.respond(LoginResponse.Success)
            isAuthenticatedFlow.value = true
            navigator.awaitPop()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `SideEffect - Close When Authenticated - already authenticated`() = runTest {
        isAuthenticatedFlow.value = true

        createPresenter().test {
            navigator.awaitPop()
            cancelAndIgnoreRemainingEvents()
        }
    }
    // endregion SideEffect - Close When Authenticated

    // region UiEvent.Login
    @Test
    fun `UiEvent - Login - Google`() = runTest {
        createPresenter().test {
            awaitItem().eventSink(UiEvent.Login(AccountType.GOOGLE))
            assertEquals(AccountType.GOOGLE, loginLauncherProducer.launches.awaitItem())
        }
    }

    @Test
    fun `UiEvent - Login - Facebook`() = runTest {
        createPresenter().test {
            awaitItem().eventSink(UiEvent.Login(AccountType.FACEBOOK))
            assertEquals(AccountType.FACEBOOK, loginLauncherProducer.launches.awaitItem())
        }
    }
    // endregion UiEvent.Login

    // region UiEvent.ClearError
    @Test
    fun `UiEvent - ClearError`() = runTest {
        createPresenter().test {
            awaitItem()
            loginLauncherProducer.respond(LoginResponse.Error.NotConnected)

            val state = awaitItem()
            assertEquals(LoginResponse.Error.NotConnected, state.loginError)
            state.eventSink(UiEvent.ClearError)
            assertNull(awaitItem().loginError)
        }
    }
    // endregion UiEvent.ClearError

    // region UiEvent.Close
    @Test
    fun `UiEvent - Close`() = runTest {
        createPresenter().test {
            awaitItem().eventSink(UiEvent.Close)
            navigator.awaitPop()
        }
    }
    // endregion UiEvent.Close
}
