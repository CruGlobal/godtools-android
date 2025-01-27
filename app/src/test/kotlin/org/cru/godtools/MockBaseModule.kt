package org.cru.godtools

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import io.mockk.every
import io.mockk.mockk
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

    @get:Provides
    val remoteConfig: FirebaseRemoteConfig = mockk {
        every { getBoolean(any()) } returns false
        every { getLong(any()) } returns 0
    }
}
