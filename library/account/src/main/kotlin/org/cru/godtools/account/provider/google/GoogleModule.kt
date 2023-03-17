package org.cru.godtools.account.provider.google

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Singleton
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
        fun googleSignInOptions(
            config: GoogleBuildConfig
        ) = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(config.serverClientId)
            .requestEmail()
            .requestProfile()
            .build()

        @Provides
        @Singleton
        fun googleSignInClient(@ApplicationContext context: Context, options: GoogleSignInOptions) =
            GoogleSignIn.getClient(context, options)
    }
}
