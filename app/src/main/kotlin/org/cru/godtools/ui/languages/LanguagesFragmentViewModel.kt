package org.cru.godtools.ui.languages

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.switchMap
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import org.ccci.gto.android.common.androidx.lifecycle.combineWith
import org.cru.godtools.base.Settings
import org.cru.godtools.db.repository.LanguagesRepository
import org.cru.godtools.db.repository.TranslationsRepository
import org.cru.godtools.model.Language
import org.cru.godtools.model.Language.Companion.toDisplayNameSortedMap

private const val KEY_QUERY = "query"
private const val KEY_IS_SEARCH_VIEW_OPEN = "isSearchViewOpen"

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class LanguagesFragmentViewModel @Inject constructor(
    @ApplicationContext context: Context,
    languagesRepository: LanguagesRepository,
    settings: Settings,
    translationsRepository: TranslationsRepository,
    private val savedState: SavedStateHandle
) : ViewModel() {
    val isPrimary = MutableLiveData(true)

    // region Search
    val query: MutableLiveData<String?> = savedState.getLiveData(KEY_QUERY, null)

    var isSearchViewOpen: Boolean
        get() = savedState.get<Boolean>(KEY_IS_SEARCH_VIEW_OPEN) == true
        set(value) = savedState.set(KEY_IS_SEARCH_VIEW_OPEN, value)
    // endregion Search

    // region Languages
    @Suppress("UNCHECKED_CAST")
    val selectedLanguage = isPrimary.switchMap {
        when (it) {
            true -> settings.primaryLanguageLiveData as LiveData<Locale?>
            else -> settings.parallelLanguageLiveData
        }
    }

    val sortLocale = MutableLiveData<Locale>()
    private val rawLanguages = translationsRepository.getTranslationsFlow()
        .map { it.map { it.languageCode }.toSet() }
        .flatMapLatest { languagesRepository.getLanguagesFlowForLocales(it) }
        .asLiveData()
    private val sortedLanguages: LiveData<Map<String, Language>> = sortLocale.distinctUntilChanged()
        .combineWith(rawLanguages) { locale, languages -> languages.toDisplayNameSortedMap(context, locale) }
    private val filteredLanguages = query.distinctUntilChanged().combineWith(sortedLanguages) { query, languages ->
        when {
            query.isNullOrEmpty() -> languages.values.toList()
            else -> languages.entries.filter { it.key.contains(query, true) }.map { it.value }
        }
    }

    // List of sorted filtered languages with none (null) prepended if requested
    val languages = isPrimary.distinctUntilChanged().combineWith(filteredLanguages) { isPrimary, languages ->
        when (isPrimary) {
            false -> listOf<Language?>(null) + languages
            else -> languages
        }
    }
    // endregion Languages
}
