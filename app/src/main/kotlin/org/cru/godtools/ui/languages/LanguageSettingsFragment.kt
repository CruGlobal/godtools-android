package org.cru.godtools.ui.languages

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import org.cru.godtools.R
import org.cru.godtools.base.Settings
import org.cru.godtools.databinding.LanguageSettingsFragmentBinding
import org.cru.godtools.db.repository.LanguagesRepository
import org.cru.godtools.fragment.BasePlatformFragment

@AndroidEntryPoint
class LanguageSettingsFragment :
    BasePlatformFragment<LanguageSettingsFragmentBinding>(R.layout.language_settings_fragment),
    LanguageSettingsFragmentBindingCallbacks {
    override fun onBindingCreated(binding: LanguageSettingsFragmentBinding, savedInstanceState: Bundle?) {
        super.onBindingCreated(binding, savedInstanceState)
        binding.callbacks = this
        binding.primaryLocale = settings.primaryLanguageLiveData
        binding.primaryLanguage = dataModel.primaryLanguage
        binding.parallelLocale = settings.parallelLanguageLiveData
        binding.parallelLanguage = dataModel.parallelLanguage
    }

    private val dataModel: LanguageSettingsFragmentDataModel by viewModels()

    override fun editPrimaryLanguage() = requireActivity().startLanguageSelectionActivity(true)
    override fun editParallelLanguage() = requireActivity().startLanguageSelectionActivity(false)
}

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class LanguageSettingsFragmentDataModel @Inject constructor(
    languagesRepository: LanguagesRepository,
    settings: Settings
) : ViewModel() {
    val primaryLanguage = settings.primaryLanguageFlow
        .flatMapLatest { languagesRepository.getLanguageFlow(it) }
        .asLiveData()
    val parallelLanguage = settings.parallelLanguageFlow
        .flatMapLatest { it?.let { languagesRepository.getLanguageFlow(it) } ?: flowOf(null) }
        .asLiveData()
}

interface LanguageSettingsFragmentBindingCallbacks {
    fun editPrimaryLanguage()
    fun editParallelLanguage()
}
