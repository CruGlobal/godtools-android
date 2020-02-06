package org.cru.godtools.ui.languages

import android.os.Bundle
import butterknife.OnClick
import butterknife.Optional
import org.cru.godtools.R
import org.cru.godtools.activity.startLanguageSelectionActivity
import org.cru.godtools.databinding.LanguageSettingsFragmentBinding
import org.cru.godtools.fragment.BaseBindingPlatformFragment

class LanguageSettingsFragment :
    BaseBindingPlatformFragment<LanguageSettingsFragmentBinding>(R.layout.language_settings_fragment) {
    // region Lifecycle
    override fun onBindingCreated(binding: LanguageSettingsFragmentBinding, savedInstanceState: Bundle?) {
        super.onBindingCreated(binding, savedInstanceState)
        binding.primaryLanguage = settings.primaryLanguageLiveData
        binding.parallelLanguage = settings.parallelLanguageLiveData
    }
    // endregion Lifecycle

    @Optional
    @OnClick(R.id.primary_language_button)
    internal fun editPrimaryLanguage() = requireActivity().startLanguageSelectionActivity(true)

    @Optional
    @OnClick(R.id.parallel_language_button)
    internal fun editParallelLanguage() = requireActivity().startLanguageSelectionActivity(false)
}
