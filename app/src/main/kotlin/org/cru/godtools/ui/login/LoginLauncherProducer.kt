package org.cru.godtools.ui.login

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import javax.inject.Inject
import org.cru.godtools.account.AccountType
import org.cru.godtools.account.LoginResponse
import org.cru.godtools.account.compose.rememberLoginLauncher

interface LoginLauncherProducer {
    @Composable
    fun produce(createAccount: Boolean, onResponse: (LoginResponse) -> Unit): (AccountType) -> Unit
}

internal class DefaultLoginLauncherProducer @Inject constructor() : LoginLauncherProducer {
    @Composable
    override fun produce(createAccount: Boolean, onResponse: (LoginResponse) -> Unit): (AccountType) -> Unit {
        val launcher = rememberLoginLauncher(createAccount, onResponse)
        return remember(launcher) { { launcher.launch(it) } }
    }
}
