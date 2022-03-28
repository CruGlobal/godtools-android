package org.cru.godtools.ui.languages.paralleldialog

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.distinctUntilChanged
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import org.ccci.gto.android.common.androidx.fragment.app.DataBindingDialogFragment
import org.ccci.gto.android.common.androidx.lifecycle.combineWith
import org.ccci.gto.android.common.db.Query
import org.ccci.gto.android.common.db.getAsLiveData
import org.cru.godtools.R
import org.cru.godtools.base.Settings
import org.cru.godtools.base.ui.languages.LanguagesDropdownAdapter
import org.cru.godtools.base.util.deviceLocale
import org.cru.godtools.databinding.LanguagesParallelDialogBinding
import org.cru.godtools.download.manager.GodToolsDownloadManager
import org.cru.godtools.model.Language
import org.cru.godtools.model.sortedByDisplayName
import org.keynote.godtools.android.db.Contract
import org.keynote.godtools.android.db.GodToolsDao

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
            dataModel.selectedLocale.value?.let {
                downloadManager.pinLanguageAsync(it)
                settings.parallelLanguage = it
            }
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
internal class ParallelLanguageDialogDataModel @Inject constructor(
    @ApplicationContext context: Context,
    dao: GodToolsDao,
    settings: Settings,
    savedState: SavedStateHandle
) : ViewModel() {
    val deviceLocale = MutableLiveData(context.deviceLocale)
    val selectedLocale = savedState.getLiveData<Locale?>("selectedLocale", settings.parallelLanguage)

    private val rawLanguages = Query.select<Language>()
        .join(Contract.LanguageTable.SQL_JOIN_TRANSLATION)
        .where(Contract.TranslationTable.SQL_WHERE_PUBLISHED)
        .getAsLiveData(dao)
    val sortedLanguages = deviceLocale.distinctUntilChanged().combineWith(rawLanguages) { locale, languages ->
        languages.sortedByDisplayName(context, locale)
    }

    val selectedLanguage = selectedLocale.distinctUntilChanged().combineWith(rawLanguages) { locale, languages ->
        languages.firstOrNull { it.code == locale }
    }
}
