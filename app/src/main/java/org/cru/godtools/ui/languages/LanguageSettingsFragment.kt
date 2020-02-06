package org.cru.godtools.ui.languages

import android.os.Bundle
import org.cru.godtools.R
import org.cru.godtools.activity.startLanguageSelectionActivity
import org.cru.godtools.databinding.LanguageSettingsFragmentBinding
import org.cru.godtools.fragment.BaseBindingPlatformFragment

class LanguageSettingsFragment :
    BaseBindingPlatformFragment<LanguageSettingsFragmentBinding>(R.layout.language_settings_fragment) {
    // region Lifecycle
    override fun onBindingCreated(binding: LanguageSettingsFragmentBinding, savedInstanceState: Bundle?) {
        super.onBindingCreated(binding, savedInstanceState)
        binding.callbacks = this
        binding.primaryLanguage = settings.primaryLanguageLiveData
        binding.parallelLanguage = settings.parallelLanguageLiveData
    }
    // endregion Lifecycle

    fun editPrimaryLanguage() = requireActivity().startLanguageSelectionActivity(true)
    fun editParallelLanguage() = requireActivity().startLanguageSelectionActivity(false)
}
