package org.cru.godtools.account.provider.google

import android.content.Context
import androidx.core.content.edit
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import org.ccci.gto.android.common.play.auth.signin.GoogleSignInKtx
import org.cru.godtools.account.AccountType
import org.cru.godtools.account.provider.AccountProvider
import org.cru.godtools.api.AuthApi
import org.cru.godtools.api.model.AuthToken

private const val PREFS_GOOGLE_ACCOUNT_PROVIDER = "org.godtools.account.google"
private const val PREF_USER_ID_PREFIX = "user_id_"

@Singleton
internal class GoogleAccountProvider @Inject constructor(
    private val authApi: AuthApi,
    @ApplicationContext private val context: Context,
    private val googleSignInClient: GoogleSignInClient,
) : AccountProvider {
    private companion object {
        fun PREF_USER_ID(account: GoogleSignInAccount) = "${PREF_USER_ID_PREFIX}${account.id}"
    }

    private val prefs by lazy { context.getSharedPreferences(PREFS_GOOGLE_ACCOUNT_PROVIDER, Context.MODE_PRIVATE) }
    override val type = AccountType.GOOGLE

    override suspend fun isAuthenticated() = GoogleSignIn.getLastSignedInAccount(context) != null
    override suspend fun userId() = GoogleSignIn.getLastSignedInAccount(context)
        ?.let { prefs.getString(PREF_USER_ID(it), null) }
    override fun isAuthenticatedFlow() = GoogleSignInKtx.getLastSignedInAccountFlow(context).map { it != null }
    override fun userIdFlow() = GoogleSignInKtx.getLastSignedInAccountFlow(context)
        .map { it?.let { prefs.getString(PREF_USER_ID(it), null) } }

    // TODO: we need to implement this unless GT-1871 is implemented
    override fun accountInfoFlow() = flowOf(null)

    // region Login/Logout
    override suspend fun login(state: AccountProvider.LoginState) {
        state.activity.startActivity(googleSignInClient.signInIntent)
    }

    override suspend fun logout() {
        googleSignInClient.signOut().await()
    }
    // endregion Login/Logout

    override suspend fun authenticateWithMobileContentApi(): AuthToken? {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        val request = account?.idToken?.let { AuthToken.Request(googleIdToken = it) } ?: return null
        val token = authApi.authenticate(request).takeIf { it.isSuccessful }
            ?.body()?.takeUnless { it.hasErrors() }
            ?.dataSingle
        if (token != null) prefs.edit { putString(PREF_USER_ID(account), token.userId) }
        return token
    }
}
