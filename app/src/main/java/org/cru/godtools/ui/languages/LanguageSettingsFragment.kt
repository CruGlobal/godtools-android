package org.cru.godtools.ui.languages

import android.os.Bundle
import org.cru.godtools.R
import org.cru.godtools.activity.startLanguageSelectionActivity
import org.cru.godtools.databinding.LanguageSettingsFragmentBinding
import org.cru.godtools.fragment.BasePlatformFragment

class LanguageSettingsFragment :
    BasePlatformFragment<LanguageSettingsFragmentBinding>(R.layout.language_settings_fragment),
    LanguageSettingsFragmentBindingCallbacks {
    // region Lifecycle
    override fun onBindingCreated(binding: LanguageSettingsFragmentBinding, savedInstanceState: Bundle?) {
        super.onBindingCreated(binding, savedInstanceState)
        binding.callbacks = this
        binding.primaryLocale = settings.primaryLanguageLiveData
        binding.parallelLocale = settings.parallelLanguageLiveData
    }
    // endregion Lifecycle

    override fun editPrimaryLanguage() = requireActivity().startLanguageSelectionActivity(true)
    override fun editParallelLanguage() = requireActivity().startLanguageSelectionActivity(false)
}

interface LanguageSettingsFragmentBindingCallbacks {
    fun editPrimaryLanguage()
    fun editParallelLanguage()
}
