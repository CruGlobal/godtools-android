package org.cru.godtools.ui.languages

import androidx.lifecycle.ViewModel
import com.squareup.inject.assisted.dagger2.AssistedModule
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import org.ccci.gto.android.common.dagger.viewmodel.AssistedSavedStateViewModelFactory
import org.ccci.gto.android.common.dagger.viewmodel.ViewModelKey

@AssistedModule
@Module(includes = [AssistedInject_LanguagesModule::class])
abstract class LanguagesModule {
    @ContributesAndroidInjector
    internal abstract fun languageSelectionActivityInjector(): LanguageSelectionActivity

    @ContributesAndroidInjector
    internal abstract fun languageSettingsActivityInjector(): LanguageSettingsActivity

    @ContributesAndroidInjector
    internal abstract fun languageSettingsFragmentInjector(): LanguageSettingsFragment

    @Binds
    @IntoMap
    @ViewModelKey(LanguageSettingsFragmentDataModel::class)
    internal abstract fun languageSettingsFragmentDataModel(dataModel: LanguageSettingsFragmentDataModel): ViewModel

    @ContributesAndroidInjector
    internal abstract fun languagesFragmentInjector(): LanguagesFragment

    @Binds
    @IntoMap
    @ViewModelKey(LanguagesFragmentViewModel::class)
    abstract fun languagesFragmentViewModel(f: LanguagesFragmentViewModel.Factory):
        AssistedSavedStateViewModelFactory<out ViewModel>
}
