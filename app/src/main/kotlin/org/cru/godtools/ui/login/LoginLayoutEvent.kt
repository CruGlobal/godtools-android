package org.cru.godtools.ui.login

import org.cru.godtools.account.AccountType

sealed class LoginLayoutEvent {
    class Login(val type: AccountType) : LoginLayoutEvent()
    object Close : LoginLayoutEvent()
}
