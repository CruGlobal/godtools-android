package org.cru.godtools.account.provider.google

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.Composable
import androidx.core.content.edit
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import org.ccci.gto.android.common.androidx.activity.result.contract.transformInput
import org.ccci.gto.android.common.kotlin.coroutines.getStringFlow
import org.ccci.gto.android.common.play.auth.signin.GoogleSignInKtx
import org.cru.godtools.account.AccountType
import org.cru.godtools.account.provider.AccountProvider
import org.cru.godtools.account.provider.AuthenticationException
import org.cru.godtools.account.provider.extractAuthToken
import org.cru.godtools.api.AuthApi
import org.cru.godtools.api.model.AuthToken
import timber.log.Timber

private const val TAG = "GoogleAccountProvider"

private const val PREFS_GOOGLE_ACCOUNT_PROVIDER = "org.godtools.account.google"
private const val PREF_USER_ID_PREFIX = "user_id_"

@Singleton
@OptIn(ExperimentalCoroutinesApi::class)
internal class GoogleAccountProvider @Inject constructor(
    private val authApi: AuthApi,
    @ApplicationContext private val context: Context,
    private val googleSignInClient: GoogleSignInClient,
) : AccountProvider {
    companion object {
        @VisibleForTesting
        internal val GoogleSignInAccount.PREF_USER_ID get() = "$PREF_USER_ID_PREFIX$id"
    }

    @VisibleForTesting
    internal val prefs by lazy { context.getSharedPreferences(PREFS_GOOGLE_ACCOUNT_PROVIDER, Context.MODE_PRIVATE) }
    override val type = AccountType.GOOGLE

    override val isAuthenticated get() = GoogleSignIn.getLastSignedInAccount(context) != null
    override val userId get() = GoogleSignIn.getLastSignedInAccount(context)
        ?.let { prefs.getString(it.PREF_USER_ID, null) }
    override fun isAuthenticatedFlow() = GoogleSignInKtx.getLastSignedInAccountFlow(context).map { it != null }
    override fun userIdFlow() = GoogleSignInKtx.getLastSignedInAccountFlow(context)
        .flatMapLatest { it?.let { prefs.getStringFlow(it.PREF_USER_ID, null) } ?: flowOf(null) }

    // region Login/Logout
    @Composable
    override fun rememberLauncherForLogin() = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
            .transformInput { _: AccountType -> googleSignInClient.signInIntent },
        onResult = {},
    )

    private suspend fun refreshSignIn() = try {
        googleSignInClient.silentSignIn().await()
    } catch (e: ApiException) {
        Timber.tag(TAG).d(e, "Error refreshing google account authentication")
        null
    }

    override suspend fun logout() {
        googleSignInClient.signOut().await()
        prefs.edit { clear() }
    }
    // endregion Login/Logout

    override suspend fun authenticateWithMobileContentApi(createUser: Boolean): Result<AuthToken> {
        var account = GoogleSignIn.getLastSignedInAccount(context)
            ?: return Result.failure(AuthenticationException.MissingCredentials)
        var resp = account.authenticateWithMobileContentApi(createUser)

        if (account.idToken == null || resp?.isSuccessful != true) {
            account = refreshSignIn() ?: return Result.failure(AuthenticationException.UnableToRefreshCredentials)
            resp = account.authenticateWithMobileContentApi(createUser)
                ?: return Result.failure(AuthenticationException.MissingCredentials)
        }

        return resp.extractAuthToken()
            .onSuccess { prefs.edit { putString(account.PREF_USER_ID, it.userId) } }
    }

    private suspend fun GoogleSignInAccount.authenticateWithMobileContentApi(createUser: Boolean) =
        idToken?.let { authApi.authenticate(AuthToken.Request(googleIdToken = it, createUser = createUser)) }
}
