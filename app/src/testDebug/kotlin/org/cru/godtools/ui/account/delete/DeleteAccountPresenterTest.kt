package org.cru.godtools.ui.account.delete

import android.app.Application
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.Turbine
import com.slack.circuit.test.FakeNavigator
import com.slack.circuit.test.test
import io.mockk.Called
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest
import org.cru.godtools.TestUtils.clearAndroidUiDispatcher
import org.cru.godtools.account.GodToolsAccountManager
import org.cru.godtools.ui.account.delete.DeleteAccountScreen.Event
import org.cru.godtools.ui.account.delete.DeleteAccountScreen.State
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
class DeleteAccountPresenterTest {
    private val deleteAccountResponse = Turbine<Boolean>()

    private val accountManager: GodToolsAccountManager = mockk {
        coEvery { deleteAccount() } coAnswers { deleteAccountResponse.awaitItem() }
    }
    private val navigator = FakeNavigator()

    private val presenter = DeleteAccountPresenter(navigator, accountManager)

    @AfterTest
    fun cleanup() = clearAndroidUiDispatcher()

    @Test
    fun `Delete Account - succeeds`() = runTest {
        presenter.test {
            assertIs<State.Display>(awaitItem())
                .eventSink(Event.DeleteAccount)

            assertIs<State.Deleting>(awaitItem())
            deleteAccountResponse.add(true)
            coVerify { accountManager.deleteAccount() }
            navigator.awaitPop()

            cancelAndIgnoreRemainingEvents()
        }

        confirmVerified(accountManager)
    }

    @Test
    fun `Delete Account - fails`() = runTest {
        presenter.test {
            assertIs<State.Display>(awaitItem())
                .eventSink(Event.DeleteAccount)

            assertIs<State.Deleting>(awaitItem())
            deleteAccountResponse.add(false)
            coVerify { accountManager.deleteAccount() }

            assertIs<State.Error>(expectMostRecentItem())
                .eventSink(Event.ClearError)

            assertIs<State.Display>(awaitItem())
        }

        confirmVerified(accountManager)
    }

    @Test
    fun `Cancel Delete Account`() = runTest {
        coEvery { accountManager.deleteAccount() } returns true

        presenter.test {
            assertIs<State.Display>(awaitItem())
                .eventSink(Event.Close)
            navigator.awaitPop()
            coVerify { accountManager wasNot Called }
        }

        confirmVerified(accountManager)
    }

    @Test
    fun `Cancel Delete Account - While Deleting`() = runTest {
        presenter.test {
            assertIs<State.Display>(awaitItem())
                .eventSink(Event.DeleteAccount)

            assertIs<State.Deleting>(awaitItem())
                .eventSink(Event.Close)
            coVerify { accountManager.deleteAccount() }
            navigator.awaitPop()

            cancelAndIgnoreRemainingEvents()
        }

        confirmVerified(accountManager)
    }
}
