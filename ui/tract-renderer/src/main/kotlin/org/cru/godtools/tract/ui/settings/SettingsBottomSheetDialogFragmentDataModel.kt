package org.cru.godtools.tract.ui.settings

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.distinctUntilChanged
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.ccci.gto.android.common.androidx.lifecycle.combineWith
import org.cru.godtools.base.util.deviceLocale
import org.cru.godtools.db.repository.LanguagesRepository
import org.cru.godtools.db.repository.TranslationsRepository
import org.cru.godtools.model.sortedByDisplayName

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class SettingsBottomSheetDialogFragmentDataModel @Inject constructor(
    @ApplicationContext context: Context,
    languagesRepository: LanguagesRepository,
    translationsRepository: TranslationsRepository,
) : ViewModel() {
    val toolCode = MutableStateFlow<String?>(null)
    val deviceLocale = MutableLiveData(context.deviceLocale)

    private val rawLanguages = toolCode
        .flatMapLatest { it?.let { translationsRepository.getTranslationsFlowForTool(it) } ?: flowOf(emptyList()) }
        .map { it.map { it.languageCode }.toSet() }
        .distinctUntilChanged()
        .flatMapLatest { languagesRepository.getLanguagesFlowForLocales(it) }
        .asLiveData()
    val sortedLanguages = deviceLocale.distinctUntilChanged()
        .combineWith(rawLanguages) { locale, languages -> languages.sortedByDisplayName(context, locale) }
        .distinctUntilChanged()
}
