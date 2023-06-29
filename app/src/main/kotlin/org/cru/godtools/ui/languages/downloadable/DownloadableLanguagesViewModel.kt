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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.cru.godtools.db.repository.LanguagesRepository
import org.cru.godtools.model.Language
import org.cru.godtools.sync.GodToolsSyncService

private const val KEY_FLOATED_LANGUAGES = "floatedLanguages"
private const val KEY_SEARCH_QUERY = "searchQuery"

@HiltViewModel
class DownloadableLanguagesViewModel @Inject constructor(
    @ApplicationContext context: Context,
    languagesRepository: LanguagesRepository,
    syncService: GodToolsSyncService,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val searchQuery = savedStateHandle.getStateFlow(KEY_SEARCH_QUERY, "")
    fun updateSearchQuery(query: String) = savedStateHandle.set(KEY_SEARCH_QUERY, query)

    private var floatedLanguages: Set<Locale>?
        get() = savedStateHandle.get<List<Locale>>(KEY_FLOATED_LANGUAGES)?.toSet()
        set(value) = savedStateHandle.set(KEY_FLOATED_LANGUAGES, value?.let { ArrayList(it) })
    val languages = languagesRepository.getLanguagesFlow()
        .map {
            val floated = floatedLanguages
                ?: it.filter { it.isAdded }.map { it.code }.toSet().also { floatedLanguages = it }
            it.sortedWith(
                compareByDescending<Language> { it.code in floated }
                    .then(Language.COMPARATOR_DISPLAY_NAME(context))
            )
        }
        .flowOn(Dispatchers.Default)
        .combine(searchQuery.map { it.split(Regex("\\s+")).filter { it.isNotBlank() } }) { langs, terms ->
            langs.filter {
                val displayName by lazy { it.getDisplayName(context) }
                val nativeName by lazy { it.getDisplayName(context, it.code) }
                terms.all { displayName.contains(it, true) || nativeName.contains(it, true) }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // region Sync logic
    init {
        viewModelScope.launch { syncService.syncLanguages() }
    }
    // endregion Sync logic
}
