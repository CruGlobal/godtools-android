package org.cru.godtools.tract.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.distinctUntilChanged
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.cru.godtools.base.Settings
import org.cru.godtools.db.repository.LanguagesRepository
import org.cru.godtools.db.repository.TranslationsRepository
import org.cru.godtools.model.Language.Companion.sortedByDisplayName

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class SettingsBottomSheetDialogFragmentDataModel @Inject constructor(
    @ApplicationContext context: Context,
    languagesRepository: LanguagesRepository,
    settings: Settings,
    translationsRepository: TranslationsRepository,
) : ViewModel() {
    val toolCode = MutableStateFlow<String?>(null)

    private val rawLanguages = toolCode
        .flatMapLatest { it?.let { translationsRepository.getTranslationsFlowForTool(it) } ?: flowOf(emptyList()) }
        .map { it.map { it.languageCode }.toSet() }
        .distinctUntilChanged()
        .flatMapLatest { languagesRepository.getLanguagesFlowForLocales(it) }
    val sortedLanguages = settings.appLanguageFlow
        .combine(rawLanguages) { locale, languages -> languages.sortedByDisplayName(context, locale) }
        .asLiveData()
        .distinctUntilChanged()
}
