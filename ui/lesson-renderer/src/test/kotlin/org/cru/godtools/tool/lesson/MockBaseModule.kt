package org.cru.godtools.tool.lesson

import androidx.lifecycle.MutableLiveData
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
import org.cru.godtools.base.Settings

@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [BaseModule::class])
object MockBaseModule {
    @get:Provides
    val firebaseRemoteConfig: FirebaseRemoteConfig by lazy {
        mockk {
            every { getBoolean(any()) } returns true
        }
    }

    @get:Provides
    @get:Named(BaseModule.IS_CONNECTED_STATE_FLOW)
    val isConnected = MutableStateFlow(true).asStateFlow()

    @get:Provides
    val settings: Settings by lazy {
        mockk {
            every { isFeatureDiscovered(any()) } returns true
            every { isFeatureDiscoveredFlow(any()) } returns MutableStateFlow(true).asStateFlow()
            every { isFeatureDiscoveredLiveData(any()) } returns MutableLiveData(true)
        }
    }
}
