package org.cru.godtools.ui.login

import android.app.Application
import android.content.Context
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.slack.circuit.test.TestEventSink
import kotlin.test.Test
import org.cru.godtools.R
import org.cru.godtools.account.AccountType
import org.cru.godtools.account.LoginResponse
import org.cru.godtools.ui.login.LoginPresenter.UiEvent
import org.cru.godtools.ui.login.LoginPresenter.UiState
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
@OptIn(ExperimentalTestApi::class)
class LoginLayoutTest {
    private val context: Context get() = ApplicationProvider.getApplicationContext()
    private val events = TestEventSink<UiEvent>()

    // region Heading
    @Test
    fun `Heading - login`() = runComposeUiTest {
        setContent { LoginLayout(UiState(createAccount = false, eventSink = events)) }

        onNodeWithText(context.getString(R.string.account_login_heading)).assertExists()
        onNodeWithText(context.getString(R.string.account_create_heading)).assertDoesNotExist()
        events.assertNoEvents()
    }

    @Test
    fun `Heading - create account`() = runComposeUiTest {
        setContent { LoginLayout(UiState(createAccount = true, eventSink = events)) }

        onNodeWithText(context.getString(R.string.account_create_heading)).assertExists()
        onNodeWithText(context.getString(R.string.account_login_heading)).assertDoesNotExist()
        events.assertNoEvents()
    }
    // endregion Heading

    // region Close Icon
    @Test
    fun `Close Icon - click fires Close`() = runComposeUiTest {
        setContent { LoginLayout(UiState(eventSink = events)) }

        onNodeWithTag(TEST_TAG_ICON_CLOSE).performClick()
        events.assertEvent(UiEvent.Close)
    }
    // endregion Close Icon

    // region Login Buttons
    @Test
    fun `Login Buttons - click Google fires Login(GOOGLE)`() = runComposeUiTest {
        setContent { LoginLayout(UiState(eventSink = events)) }

        onNodeWithTag(TEST_TAG_BUTTON_GOOGLE).performClick()
        events.assertEvent(UiEvent.Login(AccountType.GOOGLE))
    }

    @Test
    fun `Login Buttons - click Facebook fires Login(FACEBOOK)`() = runComposeUiTest {
        setContent { LoginLayout(UiState(eventSink = events)) }

        onNodeWithTag(TEST_TAG_BUTTON_FACEBOOK).performClick()
        events.assertEvent(UiEvent.Login(AccountType.FACEBOOK))
    }
    // endregion Login Buttons

    // region Error Dialog
    @Test
    fun `Error Dialog - not shown without an error`() = runComposeUiTest {
        setContent { LoginLayout(UiState(loginError = null, eventSink = events)) }

        onNode(isDialog()).assertDoesNotExist()
        events.assertNoEvents()
    }

    @Test
    fun `Error Dialog - shown for an error`() = runComposeUiTest {
        setContent { LoginLayout(UiState(loginError = LoginResponse.Error.NotConnected, eventSink = events)) }

        onNode(isDialog() and hasAnyDescendant(hasTestTag(TEST_TAG_ERROR_DIALOG))).assertExists()
        onNodeWithText(context.getString(R.string.account_error_not_connected_title)).assertExists()
        events.assertNoEvents()
    }

    @Test
    fun `Error Dialog - click Confirm fires ClearError`() = runComposeUiTest {
        setContent { LoginLayout(UiState(loginError = LoginResponse.Error.UserNotFound, eventSink = events)) }

        onNode(hasAnyAncestor(hasTestTag(TEST_TAG_ERROR_DIALOG)) and hasTestTag(TEST_TAG_ERROR_DIALOG_BUTTON_CONFIRM))
            .performClick()
        events.assertEvent(UiEvent.ClearError)
    }
    // endregion Error Dialog
}
