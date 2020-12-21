package org.cru.godtools.dagger

import android.content.Context
import com.okta.oidc.OIDCConfig
import com.okta.oidc.Okta
import com.okta.oidc.clients.sessions.SessionClient
import com.okta.oidc.clients.web.WebAuthClient
import com.okta.oidc.storage.SharedPreferenceStorage
import com.okta.oidc.storage.security.DefaultEncryptionManager
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.lang.Exception
import javax.inject.Named
import javax.inject.Singleton
import okhttp3.OkHttpClient
import org.ccci.gto.android.common.okta.oidc.OktaUserProfileProvider
import org.ccci.gto.android.common.okta.oidc.clients.sessions.isAuthenticatedLiveData
import org.ccci.gto.android.common.okta.oidc.net.OkHttpOktaHttpClient
import org.ccci.gto.android.common.okta.oidc.storage.makeChangeAware
import org.ccci.gto.android.common.okta.oidc.storage.security.NoopEncryptionManager
import org.cru.godtools.BuildConfig.OKTA_AUTH_SCHEME
import org.cru.godtools.BuildConfig.OKTA_CLIENT_ID
import org.cru.godtools.BuildConfig.OKTA_DISCOVERY_URI
import timber.log.Timber

@Module
@InstallIn(SingletonComponent::class)
object OktaModule {
    private const val TAG = "OktaModule"

    const val IS_AUTHENTICATED_LIVE_DATA = "LIVE_DATA_IS_AUTHENTICATED"

    @Provides
    @Reusable
    fun oktaConfig() = OIDCConfig.Builder()
        .discoveryUri(OKTA_DISCOVERY_URI)
        .clientId(OKTA_CLIENT_ID)
        .redirectUri("$OKTA_AUTH_SCHEME:/auth")
        .endSessionRedirectUri("$OKTA_AUTH_SCHEME:/auth/logout")
        .scopes("openid", "profile", "email", "offline_access")
        .create()

    @Provides
    @Singleton
    fun oktaClient(@ApplicationContext context: Context, config: OIDCConfig, okhttp: OkHttpClient): WebAuthClient {
        val encryptionManager = try {
            DefaultEncryptionManager(context)
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error loading the DefaultEncryptionManager, disabling optional encryption")
            NoopEncryptionManager
        }

        return Okta.WebAuthBuilder()
            .withConfig(config)
            .withContext(context)
            .withOktaHttpClient(OkHttpOktaHttpClient(okhttp))
            .withStorage(SharedPreferenceStorage(context).makeChangeAware())
            .withEncryptionManager(encryptionManager)
            .setRequireHardwareBackedKeyStore(false)
            .create()
    }

    @Provides
    @Reusable
    @Named(IS_AUTHENTICATED_LIVE_DATA)
    fun SessionClient.isAuthenticatedLiveData() = isAuthenticatedLiveData

    @Provides
    @Reusable
    fun WebAuthClient.oktaSession() = sessionClient

    @Provides
    @Singleton
    fun SessionClient.oktaUserProfileProvider() = OktaUserProfileProvider(this)
}
