package org.cru.godtools.ui.tools

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import org.ccci.gto.android.common.dagger.viewmodel.ViewModelKey

@Module
@InstallIn(SingletonComponent::class)
abstract class ToolsModule {
    @Binds
    @IntoMap
    @ViewModelKey(ToolsFragmentDataModel::class)
    internal abstract fun toolsFragmentDataModel(dataModel: ToolsFragmentDataModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ToolsAdapterToolViewModel::class)
    internal abstract fun toolsAdapterToolViewModel(dataModel: ToolsAdapterToolViewModel): ViewModel
}
