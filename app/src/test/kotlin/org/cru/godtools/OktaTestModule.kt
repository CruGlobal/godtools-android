package org.cru.godtools

import com.okta.authfoundationbootstrap.CredentialBootstrap
import com.okta.webauthenticationui.WebAuthenticationClient
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import io.mockk.mockk
import org.cru.godtools.dagger.OktaModule

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [OktaModule::class]
)
class OktaTestModule {
    @get:Provides
    val oktaCredentials by lazy { mockk<CredentialBootstrap>() }
    @get:Provides
    val webAuthenticationClient by lazy { mockk<WebAuthenticationClient>() }
}
