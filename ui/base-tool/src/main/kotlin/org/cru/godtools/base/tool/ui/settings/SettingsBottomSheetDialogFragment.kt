package org.cru.godtools.base.tool.ui.settings

import android.os.Bundle
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.asLiveData
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.ccci.gto.android.common.androidx.lifecycle.combineWith
import org.ccci.gto.android.common.material.bottomsheet.BindingBottomSheetDialogFragment
import org.cru.godtools.base.tool.activity.MultiLanguageToolActivity
import org.cru.godtools.base.tool.activity.MultiLanguageToolActivityDataModel
import org.cru.godtools.base.tool.ui.shareable.ShareableImageBottomSheetDialogFragment
import org.cru.godtools.base.ui.languages.LanguagesDropdownAdapter
import org.cru.godtools.model.Language
import org.cru.godtools.shared.tool.parser.model.shareable.ShareableImage
import org.cru.godtools.tool.R
import org.cru.godtools.tool.databinding.ToolSettingsSheetBinding

@AndroidEntryPoint
class SettingsBottomSheetDialogFragment :
    BindingBottomSheetDialogFragment<ToolSettingsSheetBinding>(R.layout.tool_settings_sheet),
    ToolOptionsSheetCallbacks {

    // region Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupDataModel()
    }

    override fun onBindingCreated(binding: ToolSettingsSheetBinding, savedInstanceState: Bundle?) {
        binding.callbacks = this
        binding.tool = activityDataModel.tool
        binding.activeManifest = activityDataModel.manifest.asLiveData()
        binding.hasTips = activityDataModel.hasTips
        binding.showTips = activityDataModel.showTips
        binding.primaryLanguage = primaryLanguage
        binding.parallelLanguage = parallelLanguage
        setupActions(binding)
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
                activityDataModel.toolCode.filterNotNull().collect(dataModel.toolCode)
            }
        }
    }
    // endregion Data Model

    // region UI
    private fun setupActions(binding: ToolSettingsSheetBinding) {
        val adapter = SettingsActionsAdapter(viewLifecycleOwner)
        (activity as? MultiLanguageToolActivity<*>)?.let {
            it.settingsActionsFlow
                .flowWithLifecycle(viewLifecycleOwner.lifecycle)
                .onEach { adapter.actions = it }
                .launchIn(viewLifecycleOwner.lifecycleScope)
        }
        binding.actions.adapter = adapter
    }

    private fun setupLanguageViews(binding: ToolSettingsSheetBinding) {
        binding.languagePrimaryDropdown.apply {
            val adapter = LanguagesDropdownAdapter(context)
            dataModel.sortedLanguages
                .combineWith(parallelLanguage) { langs, para -> langs.filterNot { it.code == para?.code } }
                .observe(viewLifecycleOwner, adapter)
            setAdapter(adapter)
            setOnItemClickListener { _, _, pos, _ -> adapter.getItem(pos)?.code?.let { updatePrimaryLanguage(it) } }
        }
        binding.languageParallelDropdown.apply {
            val none = Language(code = Locale("x", "none"), name = getString(R.string.tract_settings_languages_none))
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

    private fun setupShareables(binding: ToolSettingsSheetBinding) {
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

    // region ToolOptionsSettingsSheetCallbacks
    override fun swapLanguages() {
        val languages = activityDataModel.primaryLocales.value
        activityDataModel.primaryLocales.value = activityDataModel.parallelLocales.value
        activityDataModel.parallelLocales.value = languages
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
    // endregion ToolOptionsSettingsSheetCallbacks
}

interface ToolOptionsSheetCallbacks {
    fun shareShareable(shareable: ShareableImage?)
    fun swapLanguages()
}
