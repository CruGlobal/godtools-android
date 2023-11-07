package org.cru.godtools.account

sealed interface LoginResponse {
    data object Success : LoginResponse
    open class Error : LoginResponse {
        data object UserNotFound : Error()
        data object UserAlreadyExists : Error()
    }
}
