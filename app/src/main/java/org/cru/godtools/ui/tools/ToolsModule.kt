package org.cru.godtools.ui.tools

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import org.ccci.gto.android.common.dagger.viewmodel.ViewModelKey
import org.cru.godtools.fragment.ToolsFragment

@Module
abstract class ToolsModule {
    @ContributesAndroidInjector
    internal abstract fun toolsFragmentInjector(): ToolsFragment

    @Binds
    @IntoMap
    @ViewModelKey(ToolsAdapterToolViewModel::class)
    internal abstract fun toolsAdapterToolViewModel(dataModel: ToolsAdapterToolViewModel): ViewModel
}
