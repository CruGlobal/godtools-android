package org.cru.godtools.account.provider.google

import android.content.Context
import androidx.credentials.CredentialManager
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import org.cru.godtools.account.provider.AccountProvider

@Module
@InstallIn(SingletonComponent::class)
internal abstract class GoogleModule {
    @Binds
    @IntoSet
    abstract fun googleAccountProvider(google: GoogleAccountProvider): AccountProvider

    companion object {
        @Provides
        @Reusable
        fun credentialManager(@ApplicationContext context: Context) = CredentialManager.create(context)

        @Provides
        @Reusable
        fun getSignInWithGoogleOption(config: GoogleBuildConfig) =
            GetSignInWithGoogleOption.Builder(config.serverClientId).build()

        @Provides
        @Reusable
        fun getGoogleIdOption(config: GoogleBuildConfig) = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true)
            .setServerClientId(config.serverClientId)
            .setAutoSelectEnabled(true)
            .build()
    }
}
