package org.cru.godtools.account.provider

import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.Flow
import org.ccci.gto.android.common.Ordered
import org.ccci.gto.android.common.jsonapi.JsonApiConverter
import org.ccci.gto.android.common.jsonapi.model.JsonApiObject
import org.cru.godtools.account.AccountType
import org.cru.godtools.api.model.AuthToken
import org.json.JSONException
import retrofit2.Response

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

    suspend fun authenticateWithMobileContentApi(createUser: Boolean): Result<AuthToken>
}

internal fun Response<JsonApiObject<AuthToken>>.extractAuthToken() = when {
    isSuccessful -> {
        val obj = body()
        val data = obj?.dataSingle

        when {
            obj == null -> Result.failure(AuthenticationException.UnknownError)
            obj.hasErrors -> obj.parseErrors()
            data == null -> Result.failure(AuthenticationException.UnknownError)
            else -> Result.success(data)
        }
    }
    else -> errorBody()?.string()
        ?.let {
            try {
                JsonApiConverter.Builder().build().fromJson(it, AuthToken::class.java)
            } catch (_: JSONException) {
                null
            }
        }
        ?.parseErrors()
        ?: Result.failure(AuthenticationException.UnknownError)
}

private fun JsonApiObject<AuthToken>.parseErrors(): Result<AuthToken> {
    errors.forEach {
        when (it.code) {
            AuthToken.ERROR_USER_ALREADY_EXISTS -> return Result.failure(AuthenticationException.UserAlreadyExists)
            AuthToken.ERROR_USER_NOT_FOUND -> return Result.failure(AuthenticationException.UserNotFound)
        }
    }
    return Result.failure(AuthenticationException.UnknownError)
}
