package org.cru.godtools.account.provider

import androidx.activity.ComponentActivity
import kotlinx.coroutines.flow.Flow
import org.ccci.gto.android.common.Ordered
import org.cru.godtools.account.AccountType
import org.cru.godtools.account.model.AccountInfo
import org.cru.godtools.api.model.AuthToken

internal interface AccountProvider : Ordered {
    val type: AccountType

    suspend fun isAuthenticated(): Boolean
    suspend fun userId(): String?
    fun isAuthenticatedFlow(): Flow<Boolean>
    fun userIdFlow(): Flow<String?>
    fun accountInfoFlow(): Flow<AccountInfo?>

    // region Login/Logout
    open class LoginState internal constructor(val activity: ComponentActivity)

    fun prepareForLogin(activity: ComponentActivity) = LoginState(activity)
    suspend fun login(state: LoginState)
    suspend fun logout()
    // endregion Login/Logout

    suspend fun authenticateWithMobileContentApi(): AuthToken?
}
