package org.cru.godtools.tool.cyoa

import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Named
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.cru.godtools.base.BaseModule

@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [BaseModule::class])
object MockBaseModule {
    @get:Provides
    @get:Named(BaseModule.IS_CONNECTED_STATE_FLOW)
    val isConnected = MutableStateFlow(true).asStateFlow()
}
