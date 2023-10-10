package org.cru.godtools.account.compose

import androidx.compose.runtime.Composable

@Composable
fun rememberLoginLauncher() = LocalGodToolsAccountManager.current.rememberLauncherForLogin()
