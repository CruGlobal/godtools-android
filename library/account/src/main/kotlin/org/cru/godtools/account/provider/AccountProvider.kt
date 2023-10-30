package org.cru.godtools.account.provider

import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.Composable
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
    @Composable
    fun rememberLauncherForLogin(): ActivityResultLauncher<AccountType>

    suspend fun logout()
    // endregion Login/Logout

    suspend fun authenticateWithMobileContentApi(): Result<AuthToken>
}
