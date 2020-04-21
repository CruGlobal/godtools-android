package org.cru.godtools.ui.languages

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import org.ccci.gto.android.common.androidx.lifecycle.orEmpty
import org.ccci.gto.android.common.db.findLiveData
import org.cru.godtools.R
import org.cru.godtools.activity.startLanguageSelectionActivity
import org.cru.godtools.base.Settings
import org.cru.godtools.databinding.LanguageSettingsFragmentBinding
import org.cru.godtools.fragment.BasePlatformFragment
import org.cru.godtools.model.Language
import org.keynote.godtools.android.db.GodToolsDao
import javax.inject.Inject

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

class LanguageSettingsFragmentDataModel @Inject constructor(dao: GodToolsDao, settings: Settings) : ViewModel() {
    val primaryLanguage = settings.primaryLanguageLiveData.switchMap { dao.findLiveData<Language>(it) }
    val parallelLanguage =
        settings.parallelLanguageLiveData.switchMap { it?.let { dao.findLiveData<Language>(it) }.orEmpty() }
}

interface LanguageSettingsFragmentBindingCallbacks {
    fun editPrimaryLanguage()
    fun editParallelLanguage()
}
