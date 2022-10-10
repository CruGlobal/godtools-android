package org.cru.godtools.account.provider.okta

import android.content.Context
import android.os.Build
import com.okta.authfoundation.AuthFoundationDefaults
import com.okta.authfoundation.client.OidcClient
import com.okta.authfoundation.client.OidcConfiguration
import com.okta.authfoundation.client.SharedPreferencesCache
import com.okta.authfoundation.credential.CredentialDataSource.Companion.createCredentialDataSource
import com.okta.authfoundation.credential.TokenStorage
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.ccci.gto.android.common.okta.authfoundation.credential.ChangeAwareTokenStorage.Companion.makeChangeAware
import org.ccci.gto.android.common.okta.authfoundation.credential.SharedPreferencesTokenStorage
import org.ccci.gto.android.common.okta.authfoundation.credential.migrateTo
import org.ccci.gto.android.common.okta.authfoundation.enableClockCompat
import org.ccci.gto.android.common.okta.datastore.DataStoreTokenStorage
import org.cru.godtools.account.provider.AccountProvider

private const val OKTA_SCOPE = "openid profile email offline_access"

@Module
@InstallIn(SingletonComponent::class)
internal abstract class OktaModule {
    @Binds
    @IntoSet
    abstract fun oktaAccountProvider(okta: OktaAccountProvider): AccountProvider

    companion object {
        @Provides
        @Singleton
        fun oidcClient(
            @ApplicationContext context: Context,
            buildConfig: OktaBuildConfig,
            okhttp: OkHttpClient
        ): OidcClient {
            AuthFoundationDefaults.enableClockCompat()
            AuthFoundationDefaults.cache = SharedPreferencesCache.create(context)
            AuthFoundationDefaults.okHttpClientFactory = { okhttp }
            val config = OidcConfiguration(buildConfig.clientId, OKTA_SCOPE)
            return OidcClient.createFromDiscoveryUrl(config, buildConfig.discoveryUrl)
        }

        @Provides
        @Singleton
        fun OidcClient.oktaTokenStorage(
            @ApplicationContext context: Context,
            coroutineScope: CoroutineScope
        ): TokenStorage = DataStoreTokenStorage(context).let { dataStore ->
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
    }
}
