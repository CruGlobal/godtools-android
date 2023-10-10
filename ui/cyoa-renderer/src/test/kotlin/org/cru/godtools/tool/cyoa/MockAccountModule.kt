package org.cru.godtools.tool.cyoa

import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import io.mockk.mockk
import org.cru.godtools.account.AccountModule
import org.cru.godtools.account.GodToolsAccountManager

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [AccountModule::class]
)
class MockAccountModule {
    @get:Provides
    val accountManager: GodToolsAccountManager by lazy { mockk() }
}
