package org.cru.godtools.tract.ui.settings

import android.os.Bundle
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import org.ccci.gto.android.common.androidx.lifecycle.combineWith
import org.ccci.gto.android.common.androidx.lifecycle.toggleValue
import org.ccci.gto.android.common.kotlin.coroutines.collectInto
import org.ccci.gto.android.common.material.bottomsheet.BindingBottomSheetDialogFragment
import org.cru.godtools.base.tool.activity.BaseToolActivity
import org.cru.godtools.base.tool.activity.MultiLanguageToolActivityDataModel
import org.cru.godtools.base.tool.ui.shareable.ShareableImageBottomSheetDialogFragment
import org.cru.godtools.base.ui.languages.LanguagesDropdownAdapter
import org.cru.godtools.base.util.deviceLocale
import org.cru.godtools.model.Language
import org.cru.godtools.tool.model.shareable.ShareableImage
import org.cru.godtools.tract.R
import org.cru.godtools.tract.activity.TractActivity
import org.cru.godtools.tract.databinding.TractSettingsSheetBinding
import org.cru.godtools.tract.databinding.TractSettingsSheetCallbacks

@AndroidEntryPoint
class SettingsBottomSheetDialogFragment :
    BindingBottomSheetDialogFragment<TractSettingsSheetBinding>(R.layout.tract_settings_sheet),
    TractSettingsSheetCallbacks {
    // region Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupDataModel()
    }

    override fun onBindingCreated(binding: TractSettingsSheetBinding, savedInstanceState: Bundle?) {
        binding.callbacks = this
        binding.tool = activityDataModel.tool
        binding.activeManifest = activityDataModel.activeManifest
        binding.hasTips = activityDataModel.hasTips
        binding.showTips = activityDataModel.showTips
        binding.primaryLanguage = primaryLanguage
        binding.parallelLanguage = parallelLanguage
        setupLanguageViews(binding)
        setupShareables(binding)
    }
    // endregion Lifecycle

    // region Data Model
    private val activityDataModel by activityViewModels<MultiLanguageToolActivityDataModel>()
    private val dataModel by viewModels<SettingsBottomSheetDialogFragmentDataModel>()

    private val primaryLanguage by lazy {
        dataModel.sortedLanguages.combineWith(activityDataModel.primaryLocales) { languages, prim ->
            languages.firstOrNull { it.code in prim }
        }
    }
    private val parallelLanguage by lazy {
        dataModel.sortedLanguages.combineWith(activityDataModel.parallelLocales) { languages, para ->
            languages.firstOrNull { it.code in para }
        }
    }

    private fun setupDataModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // sync toolCode from activity to local DataModel
                activityDataModel.toolCode.filterNotNull().collectInto(dataModel.toolCode)
            }
        }
        context?.deviceLocale?.let { dataModel.deviceLocale.value = it }
    }
    // endregion Data Model

    // region UI
    private fun setupLanguageViews(binding: TractSettingsSheetBinding) {
        binding.languagePrimaryDropdown.apply {
            val adapter = LanguagesDropdownAdapter(context)
            dataModel.sortedLanguages
                .combineWith(parallelLanguage) { langs, para -> langs.filterNot { it.code == para?.code } }
                .observe(viewLifecycleOwner, adapter)
            setAdapter(adapter)
            setOnItemClickListener { _, _, pos, _ -> adapter.getItem(pos)?.code?.let { updatePrimaryLanguage(it) } }
        }
        binding.languageParallelDropdown.apply {
            val none = Language().apply {
                id = -2
                code = Locale("x", "none")
                name = getString(R.string.tract_settings_languages_none)
            }
            val adapter = LanguagesDropdownAdapter(context)
            dataModel.sortedLanguages
                .combineWith(primaryLanguage) { l, prim -> listOf(none) + l.filterNot { it.code == prim?.code } }
                .observe(viewLifecycleOwner, adapter)
            setAdapter(adapter)
            setOnItemClickListener { _, _, pos, _ ->
                updateParallelLanguage(adapter.getItem(pos)?.code?.takeUnless { it == none.code })
            }
        }
    }

    private fun setupShareables(binding: TractSettingsSheetBinding) {
        val adapter = ShareablesAdapter(viewLifecycleOwner, this)
        activityDataModel.activeManifest.observe(viewLifecycleOwner) { adapter.shareables = it?.shareables }
        binding.shareables.adapter = adapter
    }
    // endregion UI

    private fun updatePrimaryLanguage(locale: Locale) = with(activityDataModel) {
        val updateActiveLocale = activeLocale.value in primaryLocales.value.orEmpty()
        primaryLocales.value = listOf(locale)
        if (updateActiveLocale) activeLocale.value = locale
    }

    private fun updateParallelLanguage(locale: Locale?) = with(activityDataModel) {
        val updateActiveLocale = activeLocale.value in parallelLocales.value.orEmpty()
        parallelLocales.value = listOfNotNull(locale)
        if (updateActiveLocale) activeLocale.value = locale
    }

    // region TractSettingsSheetCallbacks
    override fun shareLink() {
        (activity as? BaseToolActivity<*>)?.shareCurrentTool()
        dismissAllowingStateLoss()
    }

    override fun shareScreen() {
        (activity as? TractActivity)?.shareLiveShareLink()
        dismissAllowingStateLoss()
    }

    override fun shareShareable(shareable: ShareableImage?) {
        val manifest = shareable?.manifest
        val tool = manifest?.code
        val locale = manifest?.locale
        val id = shareable?.id
        if (tool != null && locale != null && id != null) {
            activity?.supportFragmentManager
                ?.let { ShareableImageBottomSheetDialogFragment(tool, locale, id).show(it, null) }
        }
        dismissAllowingStateLoss()
    }

    override fun toggleTrainingTips() = activityDataModel.showTips.toggleValue()

    override fun swapLanguages() {
        val languages = activityDataModel.primaryLocales.value
        activityDataModel.primaryLocales.value = activityDataModel.parallelLocales.value
        activityDataModel.parallelLocales.value = languages
    }
    // endregion TractSettingsSheetCallbacks
}
