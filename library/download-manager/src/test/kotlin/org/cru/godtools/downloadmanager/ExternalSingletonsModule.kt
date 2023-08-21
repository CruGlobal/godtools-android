package org.cru.godtools.downloadmanager

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Named
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.cru.godtools.base.BaseModule

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [BaseModule::class]
)
abstract class ExternalSingletonsModule {
    companion object {
        @get:Provides
        @get:Singleton
        @get:Named(BaseModule.IS_CONNECTED_STATE_FLOW)
        val isConnected = MutableStateFlow(true)
    }

    @Binds
    @Named(BaseModule.IS_CONNECTED_STATE_FLOW)
    abstract fun isConnected(
        @Named(BaseModule.IS_CONNECTED_STATE_FLOW) flow: MutableStateFlow<Boolean>,
    ): StateFlow<Boolean>
}
