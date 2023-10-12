package org.cru.godtools.account.provider.facebook

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.annotation.VisibleForTesting
import androidx.core.content.edit
import com.facebook.AccessToken
import com.facebook.AccessTokenManager
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import org.ccci.gto.android.common.facebook.login.currentAccessTokenFlow
import org.ccci.gto.android.common.facebook.login.isAuthenticatedFlow
import org.ccci.gto.android.common.facebook.login.refreshCurrentAccessToken
import org.ccci.gto.android.common.kotlin.coroutines.getStringFlow
import org.cru.godtools.account.AccountType
import org.cru.godtools.account.provider.AccountProvider
import org.cru.godtools.api.AuthApi
import org.cru.godtools.api.model.AuthToken

private val FACEBOOK_SCOPE = setOf("email", "public_profile")

private const val PREFS_FACEBOOK_ACCOUNT_PROVIDER = "org.godtools.account.facebook"
private const val PREF_USER_ID_PREFIX = "user_id_"

@Singleton
@OptIn(ExperimentalCoroutinesApi::class)
internal class FacebookAccountProvider @Inject constructor(
    private val accessTokenManager: AccessTokenManager,
    private val authApi: AuthApi,
    @ApplicationContext
    context: Context,
    private val loginManager: LoginManager,
) : AccountProvider {
    @VisibleForTesting
    internal companion object {
        fun PREF_USER_ID(accessToken: AccessToken) = "$PREF_USER_ID_PREFIX${accessToken.userId}"
    }

    override val type = AccountType.FACEBOOK
    @VisibleForTesting
    internal val prefs by lazy { context.getSharedPreferences(PREFS_FACEBOOK_ACCOUNT_PROVIDER, Context.MODE_PRIVATE) }

    override fun userId() = accessTokenManager.currentAccessToken?.let { prefs.getString(PREF_USER_ID(it), null) }
    override fun isAuthenticated() = accessTokenManager.currentAccessToken?.isExpired == false
    override fun userIdFlow() = accessTokenManager.currentAccessTokenFlow()
        .flatMapLatest { it?.let { prefs.getStringFlow(PREF_USER_ID(it), null) } ?: flowOf(null) }
    override fun isAuthenticatedFlow() = accessTokenManager.isAuthenticatedFlow()

    // region Login/Logout
    inner class FacebookLoginState(activity: ComponentActivity) : AccountProvider.LoginState(activity) {
        val launcher = activity.registerForActivityResult(loginManager.createLogInActivityResultContract()) {}
    }

    override fun prepareForLogin(activity: ComponentActivity) = FacebookLoginState(activity)
    override suspend fun login(state: AccountProvider.LoginState) {
        if (state !is FacebookLoginState) return
        state.launcher.launch(FACEBOOK_SCOPE)
    }
    override suspend fun logout() = loginManager.logOut()
    // endregion Login/Logout

    override suspend fun authenticateWithMobileContentApi(): AuthToken? {
        var accessToken = accessTokenManager.currentAccessToken ?: return null
        var resp = accessToken.authenticateWithMobileContentApi()

        // try refreshing the access token if the API rejected it
        if (!resp.isSuccessful) {
            accessToken = try {
                accessTokenManager.refreshCurrentAccessToken()
            } catch (e: FacebookException) {
                null
            } ?: return null
            resp = accessToken.authenticateWithMobileContentApi()
        }

        return resp.takeIf { it.isSuccessful }
            ?.body()?.takeUnless { it.hasErrors }?.dataSingle
            ?.also { prefs.edit { putString(PREF_USER_ID(accessToken), it.userId) } }
    }

    private suspend fun AccessToken.authenticateWithMobileContentApi() =
        authApi.authenticate(AuthToken.Request(fbAccessToken = token))
}
