package org.cru.godtools.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.ccci.gto.android.common.androidx.compose.foundation.layout.padding
import org.cru.godtools.R
import org.cru.godtools.account.AccountType
import org.cru.godtools.account.LoginResponse
import org.cru.godtools.account.compose.rememberLoginLauncher
import org.cru.godtools.base.ui.theme.GodToolsTheme

private val MARGIN_HORIZONTAL = 32.dp
private val FACEBOOK_BLUE = Color(red = 0x18, green = 0x77, blue = 0xf2)

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun LoginLayout(createAccount: Boolean = false, onEvent: (event: LoginLayoutEvent) -> Unit) {
    var loginError: LoginResponse.Error? by rememberSaveable { mutableStateOf(null) }
    val loginLauncher = rememberLoginLauncher(createAccount) {
        when (it) {
            LoginResponse.Success -> onEvent(LoginLayoutEvent.Close)
            is LoginResponse.Error -> loginError = it
        }
    }

    LoginError(loginError, onDismiss = { loginError = null })

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(GodToolsTheme.GT_BLUE)
            .verticalScroll(rememberScrollState())
    ) {
        CompositionLocalProvider(LocalContentColor provides Color.White) {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { onEvent(LoginLayoutEvent.Close) }) {
                        Icon(Icons.Filled.Close, null)
                    }
                },
                title = {},
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GodToolsTheme.GT_BLUE,
                    navigationIconContentColor = Color.White
                ),
            )
            Spacer(Modifier.weight(1f))
            Image(
                painterResource(R.drawable.banner_account_login),
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.weight(1f))

            Text(
                stringResource(if (createAccount) R.string.account_create_heading else R.string.account_login_heading),
                style = MaterialTheme.typography.displayMedium,
                modifier = Modifier
                    .padding(horizontal = MARGIN_HORIZONTAL)
                    .align(Alignment.Start)
            )
            Text(
                stringResource(
                    if (createAccount) R.string.account_create_description else R.string.account_login_description
                ),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(horizontal = MARGIN_HORIZONTAL, top = 8.dp, bottom = 32.dp)
            )

            Button(
                onClick = { loginLauncher.launch(AccountType.GOOGLE) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black.copy(alpha = 0.54f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MARGIN_HORIZONTAL)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_account_logo_google),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(stringResource(com.google.android.gms.base.R.string.common_signin_button_text_long))
            }
            Button(
                onClick = { loginLauncher.launch(AccountType.FACEBOOK) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = FACEBOOK_BLUE,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MARGIN_HORIZONTAL)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_account_logo_facebook),
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(stringResource(com.facebook.common.R.string.com_facebook_loginview_log_in_button_long))
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun LoginError(error: LoginResponse.Error?, onDismiss: () -> Unit) {
    if (error != null) {
        AlertDialog(
            text = {
                Text(
                    stringResource(
                        when (error) {
                            LoginResponse.Error.UserAlreadyExists -> R.string.account_error_user_already_exists
                            LoginResponse.Error.UserNotFound -> R.string.account_error_user_not_found
                            else -> R.string.account_error_unknown
                        }
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = { onDismiss() }) {
                    Text(stringResource(R.string.account_error_dialog_dismiss))
                }
            },
            onDismissRequest = { onDismiss() },
        )
    }
}
