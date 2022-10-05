package org.cru.godtools.account.provider.okta

import com.okta.authfoundation.credential.Credential
import com.okta.authfoundationbootstrap.CredentialBootstrap
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import org.ccci.gto.android.common.okta.authfoundation.credential.isAuthenticated
import org.ccci.gto.android.common.okta.authfoundation.credential.isAuthenticatedFlow
import org.ccci.gto.android.common.okta.authfoundationbootstrap.defaultCredentialFlow
import org.cru.godtools.account.provider.AccountProvider

private const val TAG_USER_ID = "mobileContentApiUserId"

@Singleton
@OptIn(ExperimentalCoroutinesApi::class)
internal class OktaAccountProvider @Inject constructor(
    private val credentials: CredentialBootstrap
) : AccountProvider {
    override val type = AccountProvider.Type.OKTA

    override suspend fun isAuthenticated() = credentials.defaultCredential().isAuthenticated
    override suspend fun userId() = credentials.defaultCredential().userId
    override fun isAuthenticatedFlow() = credentials.defaultCredentialFlow().flatMapLatest { it.isAuthenticatedFlow() }

    override suspend fun logout() {
        with(credentials.defaultCredential()) {
            try {
                revokeAllTokens()
            } finally {
                delete()
            }
        }
    }
}

private val Credential.userId get() = tags[TAG_USER_ID]
