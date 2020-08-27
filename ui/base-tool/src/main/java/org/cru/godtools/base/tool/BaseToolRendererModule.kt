package org.cru.godtools.base.tool

import android.content.Context
import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import javax.inject.Named
import org.ccci.gto.android.common.androidx.lifecycle.net.isConnectedLiveData
import org.ccci.gto.android.common.dagger.viewmodel.ViewModelKey
import org.cru.godtools.base.tool.activity.BaseSingleToolActivityDataModel
import org.cru.godtools.base.tool.viewmodel.LatestPublishedManifestDataModel

@Module
@InstallIn(SingletonComponent::class)
abstract class BaseToolRendererModule {
    @Binds
    @IntoMap
    @ViewModelKey(BaseSingleToolActivityDataModel::class)
    internal abstract fun baseSingleToolActivityDataModel(dataModel: BaseSingleToolActivityDataModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(LatestPublishedManifestDataModel::class)
    internal abstract fun latestPublishedManifestDataModel(dataModel: LatestPublishedManifestDataModel): ViewModel

    companion object {
        const val IS_CONNECTED_LIVE_DATA = "LIVE_DATA_IS_CONNECTED"

        @Provides
        @Reusable
        @Named(IS_CONNECTED_LIVE_DATA)
        fun isConnectedLiveData(@ApplicationContext context: Context) = context.isConnectedLiveData()
    }
}
