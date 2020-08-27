package org.cru.godtools.ui.profile

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import org.ccci.gto.android.common.dagger.viewmodel.ViewModelKey

@Module
@InstallIn(SingletonComponent::class)
abstract class ProfileModule {
    @ContributesAndroidInjector
    internal abstract fun profileActivityInjector(): ProfileActivity

    @ContributesAndroidInjector
    internal abstract fun globalActivityFragmentInjector(): GlobalActivityFragment

    @Binds
    @IntoMap
    @ViewModelKey(GlobalActivityFragmentViewModel::class)
    internal abstract fun globalActivityFragmentViewModel(dataModel: GlobalActivityFragmentViewModel): ViewModel
}
