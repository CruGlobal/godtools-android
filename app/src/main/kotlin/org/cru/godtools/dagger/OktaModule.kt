package org.cru.godtools.dagger

import android.content.Context
import android.os.Build
import com.okta.authfoundation.AuthFoundationDefaults
import com.okta.authfoundation.client.OidcClient
import com.okta.authfoundation.client.OidcConfiguration
import com.okta.authfoundation.client.SharedPreferencesCache
import com.okta.authfoundation.credential.CredentialDataSource
import com.okta.authfoundation.credential.CredentialDataSource.Companion.createCredentialDataSource
import com.okta.authfoundation.credential.TokenStorage
import com.okta.authfoundationbootstrap.CredentialBootstrap
import com.okta.legacytokenmigration.LegacyTokenMigration
import com.okta.oidc.OIDCConfig
import com.okta.oidc.Okta
import com.okta.oidc.clients.sessions.SessionClient
import com.okta.oidc.storage.SharedPreferenceStorage
import com.okta.webauthenticationui.WebAuthenticationClient.Companion.createWebAuthenticationClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import org.ccci.gto.android.common.okta.authfoundation.credential.ChangeAwareTokenStorage.Companion.makeChangeAware
import org.ccci.gto.android.common.okta.authfoundation.credential.SharedPreferencesTokenStorage
import org.ccci.gto.android.common.okta.authfoundation.credential.migrateTo
import org.ccci.gto.android.common.okta.authfoundation.credential.userInfoFlow
import org.ccci.gto.android.common.okta.authfoundation.enableClockCompat
import org.ccci.gto.android.common.okta.authfoundationbootstrap.defaultCredentialFlow
import org.ccci.gto.android.common.okta.datastore.DataStoreTokenStorage
import org.ccci.gto.android.common.okta.oidc.storage.security.NoopEncryptionManager
import org.ccci.gto.android.common.okta.oidc.storage.security.createDefaultEncryptionManager
import org.cru.godtools.BuildConfig.OKTA_AUTH_SCHEME
import org.cru.godtools.BuildConfig.OKTA_CLIENT_ID
import org.cru.godtools.BuildConfig.OKTA_DISCOVERY_URI
import org.cru.godtools.base.DAGGER_OKTA_USER_INFO_FLOW
import timber.log.Timber

private const val OKTA_SCOPE = "openid profile email offline_access"

@Module
@InstallIn(SingletonComponent::class)
object OktaModule {
    private const val TAG = "OktaModule"

    @Provides
    @Singleton
    fun oidcClient(@ApplicationContext context: Context, okhttp: OkHttpClient): OidcClient {
        AuthFoundationDefaults.enableClockCompat()
        AuthFoundationDefaults.cache = SharedPreferencesCache.create(context)
        AuthFoundationDefaults.okHttpClientFactory = { okhttp }
        val config = OidcConfiguration(OKTA_CLIENT_ID, OKTA_SCOPE)
        return OidcClient.createFromDiscoveryUrl(
            config,
            "$OKTA_DISCOVERY_URI/.well-known/openid-configuration".toHttpUrl()
        )
    }

    @Provides
    @Singleton
    fun OidcClient.oktaTokenStorage(
        @ApplicationContext context: Context,
        coroutineScope: CoroutineScope
    ): TokenStorage = DataStoreTokenStorage(context)
        .let { dataStore ->
            when {
                Build.VERSION.SDK_INT < Build.VERSION_CODES.M -> dataStore
                else -> SharedPreferencesTokenStorage(this, context)
                    .makeChangeAware()
                    // migrate tokens if the user had previously used the DataStoreTokenStorage
                    // TODO: this may race with loading tokens in the CredentialDataSource.
                    //       We may want to create a "MergedTokenStorage" class instead to facilitate token migrations.
                    .also { prefs -> coroutineScope.launch { dataStore.migrateTo(prefs) } }
            }
        }

    @Provides
    @Singleton
    fun OidcClient.oktaCredentialDataSource(storage: TokenStorage) = createCredentialDataSource(storage)

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
    @Named(DAGGER_OKTA_USER_INFO_FLOW)
    fun oktaUserInfoFlow(
        credentials: CredentialBootstrap,
        coroutineScope: CoroutineScope,
    ) = credentials.defaultCredentialFlow()
        .userInfoFlow()
        .shareIn(coroutineScope, SharingStarted.WhileSubscribed(), replay = 1)

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
