package org.cru.godtools.ui.login

sealed class LoginLayoutEvent {
    data object Close : LoginLayoutEvent()
}
