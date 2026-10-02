package org.cru.godtools.account.provider.google

import android.content.Context
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.edit
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CredentialOption
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.ClearCredentialException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.ccci.gto.android.common.kotlin.coroutines.getStringFlow
import org.cru.godtools.account.AccountType
import org.cru.godtools.account.provider.AccountProvider
import org.cru.godtools.account.provider.AuthenticationException
import org.cru.godtools.account.provider.extractAuthToken
import org.cru.godtools.api.AuthApi
import org.cru.godtools.api.model.AuthToken
import timber.log.Timber

private const val TAG = "GoogleAccountProvider"

private const val PREFS_GOOGLE_ACCOUNT_PROVIDER = "org.godtools.account.google"
@VisibleForTesting
internal const val PREF_ACCOUNT_ID = "account_id"
@VisibleForTesting
internal const val PREF_ID_TOKEN = "id_token"
@VisibleForTesting
internal const val PREF_USER_ID = "user_id"

@Singleton
internal class GoogleAccountProvider @Inject constructor(
    private val authApi: AuthApi,
    @ApplicationContext private val context: Context,
    private val credentialManager: CredentialManager,
    private val signInWithGoogleOption: GetSignInWithGoogleOption,
    private val authorizedGoogleIdOption: GetGoogleIdOption,
) : AccountProvider {
    @VisibleForTesting
    internal val prefs by lazy { context.getSharedPreferences(PREFS_GOOGLE_ACCOUNT_PROVIDER, Context.MODE_PRIVATE) }
    override val type = AccountType.GOOGLE

    override val isAuthenticated get() = userId != null
    override val userId get() = prefs.getString(PREF_USER_ID, null)
    override fun isAuthenticatedFlow() = userIdFlow().map { it != null }
    override fun userIdFlow() = prefs.getStringFlow(PREF_USER_ID, null)

    // region Login/Logout
    @Composable
    override fun rememberLauncherForLogin(
        createUser: Boolean,
        onAuthResult: (Result<AuthToken>) -> Unit
    ): ActivityResultLauncher<AccountType> {
        val activityContext = LocalContext.current
        val coroutineScope = rememberCoroutineScope()
        val currentOnAuthResult by rememberUpdatedState(onAuthResult)

        return remember(activityContext, coroutineScope, createUser) {
            object : ActivityResultLauncher<AccountType>() {
                override fun launch(input: AccountType, options: ActivityOptionsCompat?) {
                    coroutineScope.launch {
                        currentOnAuthResult(login(activityContext, createUser).onFailure { logout() })
                    }
                }

                override val contract get() = TODO("Unsupported")
                override fun unregister() = TODO("Unsupported")
            }
        }
    }

    @VisibleForTesting
    internal suspend fun login(activityContext: Context, createUser: Boolean): Result<AuthToken> {
        return catchAuthenticationErrors {
            val credential = getGoogleIdTokenCredential(activityContext, signInWithGoogleOption)
                ?: return Result.failure(AuthenticationException.MissingCredentials)
            prefs.edit {
                putString(PREF_ACCOUNT_ID, credential.id)
                putString(PREF_ID_TOKEN, credential.idToken)
            }
            credential.authenticateWithMobileContentApi(createUser)
        }
    }

    override suspend fun logout() {
        try {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (e: ClearCredentialException) {
            Timber.tag(TAG).d(e, "Error clearing google credential state")
        }
        prefs.edit { clear() }
    }
    // endregion Login/Logout

    override suspend fun authenticateWithMobileContentApi(createUser: Boolean): Result<AuthToken> {
        return catchAuthenticationErrors {
            val accountId = prefs.getString(PREF_ACCOUNT_ID, null)
                ?: return Result.failure(AuthenticationException.MissingCredentials)
            val resp = prefs.getString(PREF_ID_TOKEN, null)
                ?.let { authApi.authenticate(AuthToken.Request(googleIdToken = it, createUser = createUser)) }
            if (resp?.isSuccessful == true) {
                return resp.extractAuthToken()
                    .onSuccess { prefs.edit { putString(PREF_USER_ID, it.userId) } }
            }

            // try refreshing the id token if we don't have one or the API rejected it
            val credential = getGoogleIdTokenCredential(context, authorizedGoogleIdOption)
                ?.takeIf { it.id == accountId }
                ?: return Result.failure(AuthenticationException.UnableToRefreshCredentials)
            prefs.edit { putString(PREF_ID_TOKEN, credential.idToken) }
            credential.authenticateWithMobileContentApi(createUser)
        }
    }

    private suspend fun getGoogleIdTokenCredential(context: Context, option: CredentialOption) = try {
        val request = GetCredentialRequest.Builder().addCredentialOption(option).build()
        val credential = credentialManager.getCredential(context, request).credential
        when {
            credential !is CustomCredential -> null
            credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL -> null
            else -> GoogleIdTokenCredential.createFrom(credential.data)
        }
    } catch (e: GetCredentialException) {
        Timber.tag(TAG).d(e, "Unable to retrieve google credential")
        null
    } catch (e: GoogleIdTokenParsingException) {
        Timber.tag(TAG).d(e, "Unable to parse google id token credential")
        null
    }

    private suspend fun GoogleIdTokenCredential.authenticateWithMobileContentApi(createUser: Boolean) =
        authApi.authenticate(AuthToken.Request(googleIdToken = idToken, createUser = createUser))
            .extractAuthToken()
            .onSuccess { prefs.edit { putString(PREF_USER_ID, it.userId) } }

    private inline fun catchAuthenticationErrors(block: () -> Result<AuthToken>) = try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        if (e !is IOException) Timber.tag(TAG).e(e, "Unexpected error authenticating with Google")
        Result.failure(e)
    }
}
