package org.cru.godtools.ui.languages.paralleldialog

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.distinctUntilChanged
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import org.ccci.gto.android.common.androidx.fragment.app.DataBindingDialogFragment
import org.ccci.gto.android.common.androidx.lifecycle.combineWith
import org.cru.godtools.R
import org.cru.godtools.base.Settings
import org.cru.godtools.base.ui.languages.LanguagesDropdownAdapter
import org.cru.godtools.base.util.deviceLocale
import org.cru.godtools.databinding.LanguagesParallelDialogBinding
import org.cru.godtools.db.repository.LanguagesRepository
import org.cru.godtools.db.repository.TranslationsRepository
import org.cru.godtools.download.manager.GodToolsDownloadManager
import org.cru.godtools.model.sortedByDisplayName

@AndroidEntryPoint
class ParallelLanguageDialogFragment :
    DataBindingDialogFragment<LanguagesParallelDialogBinding>(R.layout.languages_parallel_dialog) {
    private val dataModel: ParallelLanguageDialogDataModel by viewModels()

    @Inject
    internal lateinit var downloadManager: GodToolsDownloadManager
    @Inject
    internal lateinit var settings: Settings

    override fun onCreateDialog(savedInstanceState: Bundle?) = MaterialAlertDialogBuilder(requireContext())
        .setPositiveButton(R.string.languages_parallel_dialog_action_start) { _, _ ->
            settings.parallelLanguage = dataModel.selectedLocale.value
        }
        .setNegativeButton(R.string.languages_parallel_dialog_action_cancel, null)
        .create()

    override fun onBindingCreated(binding: LanguagesParallelDialogBinding) {
        binding.deviceLocale = dataModel.deviceLocale
        binding.selectedLanguage = dataModel.selectedLanguage

        binding.parallelLanguage.apply {
            val adapter = LanguagesDropdownAdapter(context)
            dataModel.sortedLanguages.observe(dialogLifecycleOwner, adapter)
            setAdapter(adapter)
            setOnItemClickListener { _, _, pos, _ -> dataModel.selectedLocale.value = adapter.getItem(pos)?.code }
        }
    }

    override fun onResume() {
        super.onResume()
        dataModel.deviceLocale.value = requireContext().deviceLocale
    }
}

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
internal class ParallelLanguageDialogDataModel @Inject constructor(
    @ApplicationContext context: Context,
    languagesRepository: LanguagesRepository,
    settings: Settings,
    translationsRepository: TranslationsRepository,
    savedState: SavedStateHandle
) : ViewModel() {
    val deviceLocale = MutableLiveData(context.deviceLocale)
    val selectedLocale = savedState.getLiveData("selectedLocale", settings.parallelLanguage)

    private val rawLanguages = translationsRepository.getTranslationsFlow()
        .map { it.filter { it.isPublished }.map { it.languageCode }.toSet() }
        .distinctUntilChanged()
        .flatMapLatest { languagesRepository.getLanguagesForLocalesFlow(it) }
        .asLiveData()
    val sortedLanguages = deviceLocale.distinctUntilChanged().combineWith(rawLanguages) { locale, languages ->
        languages.sortedByDisplayName(context, locale)
    }

    val selectedLanguage = selectedLocale.distinctUntilChanged().combineWith(rawLanguages) { locale, languages ->
        languages.firstOrNull { it.code == locale }
    }
}
