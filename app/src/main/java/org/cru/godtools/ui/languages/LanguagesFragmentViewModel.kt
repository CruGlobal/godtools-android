package org.cru.godtools.ui.languages

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import java.util.Locale
import org.ccci.gto.android.common.androidx.lifecycle.combineWith
import org.ccci.gto.android.common.dagger.viewmodel.AssistedSavedStateViewModelFactory
import org.ccci.gto.android.common.db.Query
import org.ccci.gto.android.common.db.getAsLiveData
import org.cru.godtools.base.Settings
import org.cru.godtools.model.Language
import org.keynote.godtools.android.db.Contract
import org.keynote.godtools.android.db.GodToolsDao

private const val KEY_QUERY = "query"
private const val KEY_IS_SEARCH_VIEW_OPEN = "isSearchViewOpen"

class LanguagesFragmentViewModel @AssistedInject constructor(
    context: Context,
    dao: GodToolsDao,
    settings: Settings,
    @Assisted private val savedState: SavedStateHandle
) : ViewModel() {
    @AssistedInject.Factory
    interface Factory : AssistedSavedStateViewModelFactory<LanguagesFragmentViewModel>

    val isPrimary = MutableLiveData<Boolean>(true)

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

    private val rawLanguages = Query.select<Language>()
        .join(Contract.LanguageTable.SQL_JOIN_TRANSLATION)
        .where(Contract.TranslationTable.SQL_WHERE_PUBLISHED)
        .getAsLiveData(dao)
    private val sortedLanguages: LiveData<Map<String, Language>> = rawLanguages
        .map { it.associateBy { lang -> lang.getDisplayName(context) }.toSortedMap(String.CASE_INSENSITIVE_ORDER) }
    private val filteredLanguages = query.distinctUntilChanged().combineWith(sortedLanguages) { query, languages ->
        when {
            query.isNullOrEmpty() -> languages.values.toList()
            else -> languages.entries.filter { it.key.contains(query, true) }.map { it.value }.toList()
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
