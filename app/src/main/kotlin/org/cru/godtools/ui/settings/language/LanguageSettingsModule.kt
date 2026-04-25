package org.cru.godtools.ui.settings.language

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import org.cru.godtools.base.ui.circuit.CircuitDeepLinkParser

@Module
@InstallIn(SingletonComponent::class)
object LanguageSettingsModule {
    @get:Provides
    @get:IntoSet
    val languagesDeepLinkParser: CircuitDeepLinkParser get() = LanguageSettingsDeepLinkParser
}
