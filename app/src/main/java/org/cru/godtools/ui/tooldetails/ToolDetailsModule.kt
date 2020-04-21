package org.cru.godtools.ui.tooldetails

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import org.ccci.gto.android.common.dagger.viewmodel.ViewModelKey

@Module
abstract class ToolDetailsModule {
    @ContributesAndroidInjector
    internal abstract fun toolDetailsActivityInjector(): ToolDetailsActivity

    @ContributesAndroidInjector
    internal abstract fun toolDetailsFragmentInjector(): ToolDetailsFragment

    @Binds
    @IntoMap
    @ViewModelKey(ToolDetailsFragmentDataModel::class)
    internal abstract fun toolDetailsFragmentDataModel(dataModel: ToolDetailsFragmentDataModel): ViewModel
}
