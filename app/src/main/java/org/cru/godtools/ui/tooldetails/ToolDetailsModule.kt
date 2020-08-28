package org.cru.godtools.ui.tooldetails

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import org.ccci.gto.android.common.dagger.viewmodel.ViewModelKey

@Module
@InstallIn(SingletonComponent::class)
abstract class ToolDetailsModule {
    @Binds
    @IntoMap
    @ViewModelKey(ToolDetailsFragmentDataModel::class)
    internal abstract fun toolDetailsFragmentDataModel(dataModel: ToolDetailsFragmentDataModel): ViewModel
}
