package org.cru.godtools.ui.login

import androidx.compose.runtime.Composable
import app.cash.turbine.Turbine
import org.cru.godtools.account.AccountType
import org.cru.godtools.account.LoginResponse

class FakeLoginLauncherProducer : LoginLauncherProducer {
    val launches = Turbine<AccountType>()
    var lastCreateAccount: Boolean? = null
    private var onResponse: ((LoginResponse) -> Unit)? = null

    fun respond(response: LoginResponse) = checkNotNull(onResponse).invoke(response)

    @Composable
    override fun produce(createAccount: Boolean, onResponse: (LoginResponse) -> Unit): (AccountType) -> Unit {
        lastCreateAccount = createAccount
        this.onResponse = onResponse
        return { launches.add(it) }
    }
}
