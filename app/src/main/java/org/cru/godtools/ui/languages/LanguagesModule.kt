package org.cru.godtools.ui.languages

import androidx.lifecycle.ViewModel
import com.squareup.inject.assisted.dagger2.AssistedModule
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import org.ccci.gto.android.common.dagger.viewmodel.AssistedSavedStateViewModelFactory
import org.ccci.gto.android.common.dagger.viewmodel.ViewModelKey

@Module
@AssistedModule
@InstallIn(SingletonComponent::class)
abstract class LanguagesModule {
    @Binds
    @IntoMap
    @ViewModelKey(LanguageSettingsFragmentDataModel::class)
    internal abstract fun languageSettingsFragmentDataModel(dataModel: LanguageSettingsFragmentDataModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(LanguagesFragmentViewModel::class)
    abstract fun languagesFragmentViewModel(f: LanguagesFragmentViewModel.Factory):
        AssistedSavedStateViewModelFactory<out ViewModel>
}
