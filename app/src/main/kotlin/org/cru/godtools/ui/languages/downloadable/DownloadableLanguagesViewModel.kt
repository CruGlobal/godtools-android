package org.cru.godtools.ui.languages.downloadable

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.cru.godtools.base.Settings
import org.cru.godtools.db.repository.LanguagesRepository
import org.cru.godtools.model.Language
import org.cru.godtools.model.Language.Companion.filterByDisplayAndNativeName
import org.cru.godtools.sync.GodToolsSyncService

private const val KEY_FLOATED_LANGUAGES = "floatedLanguages"
private const val KEY_SEARCH_QUERY = "searchQuery"

@HiltViewModel
class DownloadableLanguagesViewModel @Inject constructor(
    @ApplicationContext context: Context,
    languagesRepository: LanguagesRepository,
    settings: Settings,
    syncService: GodToolsSyncService,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val searchQuery = savedStateHandle.getStateFlow(KEY_SEARCH_QUERY, "")
    fun updateSearchQuery(query: String) = savedStateHandle.set(KEY_SEARCH_QUERY, query)

    private var floatedLanguages: Set<Locale>?
        get() = savedStateHandle.get<List<Locale>>(KEY_FLOATED_LANGUAGES)?.toSet()
        set(value) = savedStateHandle.set(KEY_FLOATED_LANGUAGES, value?.let { ArrayList(it) })
    private val sortedLanguages = languagesRepository.getLanguagesFlow()
        .combine(settings.appLanguageFlow) { langs, appLanguage ->
            val floated = floatedLanguages
                ?: langs.filter { it.isAdded }.mapTo(mutableSetOf()) { it.code }.also { floatedLanguages = it }
            langs.sortedWith(
                compareByDescending<Language> { it.code in floated }
                    .then(Language.displayNameComparator(context, appLanguage))
            )
        }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val languages = combine(sortedLanguages, settings.appLanguageFlow, searchQuery) { it, appLanguage, query ->
        it.filterByDisplayAndNativeName(query, context, appLanguage)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // region Sync logic
    init {
        viewModelScope.launch { syncService.syncLanguages() }
    }
    // endregion Sync logic
}
