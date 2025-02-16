package org.cru.godtools.base

import android.content.Context
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.stateIn
import org.ccci.gto.android.common.kotlin.coroutines.flow.net.isConnectedFlow

@Module
@InstallIn(SingletonComponent::class)
object BaseModule {
    const val IS_CONNECTED_STATE_FLOW = "STATE_FLOW_IS_CONNECTED"

    @Provides
    @Reusable
    @Named(IS_CONNECTED_STATE_FLOW)
    fun isConnectedStateFlow(@ApplicationContext context: Context, scope: CoroutineScope) = context.isConnectedFlow()
        .stateIn(scope, WhileSubscribed(5_000), false)

    @Provides
    @Singleton
    fun firebaseRemoteConfig() = Firebase.remoteConfig.apply {
        setDefaultsAsync(CONFIG_DEFAULTS)
        fetchAndActivate()
    }
}
