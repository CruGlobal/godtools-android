package org.cru.godtools.account.compose

import androidx.compose.runtime.Composable
import org.cru.godtools.account.LoginResponse

@Composable
fun rememberLoginLauncher(createAccount: Boolean, onResponse: (LoginResponse) -> Unit) =
    LocalGodToolsAccountManager.current.rememberLauncherForLogin(createAccount, onResponse)
