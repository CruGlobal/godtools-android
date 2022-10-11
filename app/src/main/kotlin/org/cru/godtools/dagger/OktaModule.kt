package org.cru.godtools.dagger

import android.content.Context
import com.okta.authfoundation.client.OidcClient
import com.okta.authfoundation.credential.CredentialDataSource
import com.okta.authfoundationbootstrap.CredentialBootstrap
import com.okta.legacytokenmigration.LegacyTokenMigration
import com.okta.oidc.OIDCConfig
import com.okta.oidc.Okta
import com.okta.oidc.clients.sessions.SessionClient
import com.okta.oidc.storage.SharedPreferenceStorage
import com.okta.webauthenticationui.WebAuthenticationClient.Companion.createWebAuthenticationClient
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.ccci.gto.android.common.okta.oidc.storage.security.NoopEncryptionManager
import org.ccci.gto.android.common.okta.oidc.storage.security.createDefaultEncryptionManager
import org.cru.godtools.BuildConfig
import org.cru.godtools.BuildConfig.OKTA_AUTH_SCHEME
import org.cru.godtools.BuildConfig.OKTA_CLIENT_ID
import org.cru.godtools.BuildConfig.OKTA_DISCOVERY_URI
import org.cru.godtools.account.provider.okta.OktaBuildConfig
import timber.log.Timber

private const val OKTA_SCOPE = "openid profile email offline_access"

@Module
@InstallIn(SingletonComponent::class)
object OktaModule {
    private const val TAG = "OktaModule"

    @Provides
    @Reusable
    fun oktaBuildConfig() = OktaBuildConfig(
        clientId = BuildConfig.OKTA_CLIENT_ID,
        discoveryUrl = "${BuildConfig.OKTA_DISCOVERY_URI}/.well-known/openid-configuration".toHttpUrl(),
        appUriScheme = BuildConfig.OKTA_AUTH_SCHEME
    )

    @Provides
    @Singleton
    fun oktaCredentials(
        credentialDataSource: CredentialDataSource,
        coroutineScope: CoroutineScope,
        @ApplicationContext context: Context,
        sessionClient: SessionClient,
    ) = CredentialBootstrap.apply {
        initialize(credentialDataSource)
        coroutineScope.launch { LegacyTokenMigration.migrate(context, sessionClient, defaultCredential()) }
    }

    @Provides
    @Singleton
    fun OidcClient.oktaWebAuthenticationClient() = createWebAuthenticationClient()

    @Provides
    @Singleton
    fun oktaLegacySessionClient(@ApplicationContext context: Context): SessionClient {
        val config = OIDCConfig.Builder()
            .discoveryUri(OKTA_DISCOVERY_URI)
            .clientId(OKTA_CLIENT_ID)
            .redirectUri("$OKTA_AUTH_SCHEME:/auth")
            .endSessionRedirectUri("$OKTA_AUTH_SCHEME:/auth/logout")
            .scopes(*OKTA_SCOPE.split(" ").toTypedArray())
            .create()
        val encryptionManager = try {
            createDefaultEncryptionManager(context)
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error loading the DefaultEncryptionManager, disabling optional encryption")
            NoopEncryptionManager
        }

        return Okta.WebAuthBuilder()
            .withConfig(config)
            .withContext(context)
            .withStorage(SharedPreferenceStorage(context))
            .withEncryptionManager(encryptionManager)
            .setRequireHardwareBackedKeyStore(false)
            .create()
            .sessionClient
    }
}
