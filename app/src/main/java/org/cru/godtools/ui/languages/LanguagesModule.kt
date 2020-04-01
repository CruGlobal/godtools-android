package org.cru.godtools.ui.languages

import androidx.lifecycle.ViewModel
import com.squareup.inject.assisted.dagger2.AssistedModule
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import org.ccci.gto.android.common.androidx.lifecycle.dagger.viewmodel.AssistedSavedStateViewModelFactory
import org.ccci.gto.android.common.androidx.lifecycle.dagger.viewmodel.ViewModelKey

@AssistedModule
@Module(includes = [AssistedInject_LanguagesModule::class])
abstract class LanguagesModule {
    @ContributesAndroidInjector
    internal abstract fun languagesFragmentInjector(): LanguagesFragment

    @Binds
    @IntoMap
    @ViewModelKey(LanguagesFragmentViewModel::class)
    abstract fun languagesFragmentViewModel(f: LanguagesFragmentViewModel.Factory):
        AssistedSavedStateViewModelFactory<out ViewModel>
}
