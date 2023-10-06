package org.cru.godtools.account.provider

import androidx.activity.ComponentActivity
import kotlinx.coroutines.flow.Flow
import org.ccci.gto.android.common.Ordered
import org.cru.godtools.account.AccountType
import org.cru.godtools.api.model.AuthToken

internal interface AccountProvider : Ordered {
    val type: AccountType

    val isAuthenticated: Boolean
    val userId: String?
    fun isAuthenticatedFlow(): Flow<Boolean>
    fun userIdFlow(): Flow<String?>

    // region Login/Logout
    abstract class LoginState

    fun prepareForLogin(activity: ComponentActivity): LoginState
    suspend fun login(state: LoginState)
    suspend fun logout()
    // endregion Login/Logout

    suspend fun authenticateWithMobileContentApi(): AuthToken?
}
