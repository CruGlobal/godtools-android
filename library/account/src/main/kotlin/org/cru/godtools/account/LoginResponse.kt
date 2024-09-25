package org.cru.godtools.account

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed interface LoginResponse : Parcelable {
    @Parcelize
    data object Success : LoginResponse
    @Parcelize
    open class Error : LoginResponse {
        @Parcelize
        data object UserNotFound : Error()
        @Parcelize
        data object UserAlreadyExists : Error()
    }
}
