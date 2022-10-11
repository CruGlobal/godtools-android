package org.cru.godtools.account.provider.okta

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.okta.authfoundation.claims.email
import com.okta.authfoundation.claims.familyName
import com.okta.authfoundation.claims.givenName
import com.okta.authfoundation.claims.name
import com.okta.authfoundation.claims.subject
import com.okta.authfoundation.client.OidcClientResult
import com.okta.authfoundation.credential.Credential
import com.okta.authfoundation.credential.Token
import com.okta.authfoundationbootstrap.CredentialBootstrap
import com.okta.webauthenticationui.WebAuthenticationClient
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.transformLatest
import org.ccci.gto.android.common.okta.authfoundation.client.dto.grMasterPersonId
import org.ccci.gto.android.common.okta.authfoundation.client.dto.oktaUserId
import org.ccci.gto.android.common.okta.authfoundation.client.dto.ssoGuid
import org.ccci.gto.android.common.okta.authfoundation.credential.idTokenFlow
import org.ccci.gto.android.common.okta.authfoundation.credential.isAuthenticated
import org.ccci.gto.android.common.okta.authfoundation.credential.isAuthenticatedFlow
import org.ccci.gto.android.common.okta.authfoundation.credential.userInfoFlow
import org.ccci.gto.android.common.okta.authfoundationbootstrap.defaultCredentialFlow
import org.cru.godtools.account.model.AccountInfo
import org.cru.godtools.account.provider.AccountProvider
import org.cru.godtools.api.AuthApi
import org.cru.godtools.api.model.AuthToken
import timber.log.Timber

private const val TAG = "OktaAccountProvider"

@Singleton
@OptIn(ExperimentalCoroutinesApi::class)
internal class OktaAccountProvider @Inject constructor(
    private val credentials: CredentialBootstrap,
    private val authApi: AuthApi,
    private val buildConfig: OktaBuildConfig,
    private val webAuthenticationClient: WebAuthenticationClient
) : AccountProvider {
    internal companion object {
        @VisibleForTesting
        internal const val TAG_USER_ID = "mobileContentApiUserId"

        private val Credential.userId get() = tags[TAG_USER_ID]
        private suspend fun Credential.setUserId(id: String) = storeToken(tags = tags + (TAG_USER_ID to id))
    }

    private val coroutineScope = CoroutineScope(SupervisorJob())
    override val type = AccountProvider.Type.OKTA

    private val credentialFlow = credentials.defaultCredentialFlow()
        .shareIn(coroutineScope, SharingStarted.WhileSubscribed(), replay = 1)
        .distinctUntilChanged()

    override suspend fun isAuthenticated() = credentials.defaultCredential().isAuthenticated
    override suspend fun userId() = credentials.defaultCredential().userId
    override fun isAuthenticatedFlow() = credentialFlow.flatMapLatest { it.isAuthenticatedFlow() }

    override fun accountInfoFlow() = credentialFlow
        .transformLatest { cred ->
            emitAll(
                combine(cred.idTokenFlow(), cred.userInfoFlow()) { idToken, userInfo ->
                    if (idToken != null || userInfo != null) {
                        AccountInfo(
                            userId = cred.userId,
                            oktaUserId = idToken?.subject ?: userInfo?.oktaUserId,
                            ssoGuid = userInfo?.ssoGuid,
                            grMasterPersonId = userInfo?.grMasterPersonId,
                            name = idToken?.name,
                            givenName = userInfo?.givenName,
                            familyName = userInfo?.familyName,
                            email = idToken?.email ?: userInfo?.email
                        )
                    } else null
                }
            )
        }
        .shareIn(coroutineScope, SharingStarted.WhileSubscribed(), replay = 1)
        .distinctUntilChanged()

    override suspend fun login(context: Context) {
        when (
            val result = webAuthenticationClient.login(
                context,
                "${buildConfig.appUriScheme}:/auth",
                extraRequestParameters = mapOf("prompt" to "login")
            )
        ) {
            is OidcClientResult.Success<Token> -> credentials.defaultCredential().storeToken(result.result)
            is OidcClientResult.Error -> {
                // log the login error
                Timber.tag(TAG).d(result.exception, "Error logging in to Okta.")
            }
        }
    }

    override suspend fun logout() {
        with(credentials.defaultCredential()) {
            try {
                revokeAllTokens()
            } finally {
                delete()
            }
        }
    }

    override suspend fun authenticateWithMobileContentApi(): AuthToken? {
        val credential = credentials.defaultCredential()
        val request = credential.getValidAccessToken()?.let { AuthToken.Request(oktaAccessToken = it) } ?: return null
        val token = authApi.authenticate(request).takeIf { it.isSuccessful }
            ?.body()?.takeUnless { it.hasErrors() }
            ?.dataSingle
        token?.userId?.let { credential.setUserId(it) }
        return token
    }
}
