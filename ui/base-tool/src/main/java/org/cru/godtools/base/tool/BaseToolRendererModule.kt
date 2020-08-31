package org.cru.godtools.base.tool

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import org.ccci.gto.android.common.androidx.lifecycle.net.isConnectedLiveData

@Module
@InstallIn(SingletonComponent::class)
abstract class BaseToolRendererModule {
    companion object {
        const val IS_CONNECTED_LIVE_DATA = "LIVE_DATA_IS_CONNECTED"

        @Provides
        @Reusable
        @Named(IS_CONNECTED_LIVE_DATA)
        fun isConnectedLiveData(@ApplicationContext context: Context) = context.isConnectedLiveData()
    }
}
