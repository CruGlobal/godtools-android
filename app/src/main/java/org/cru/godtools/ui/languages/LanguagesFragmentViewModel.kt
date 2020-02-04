package org.cru.godtools.ui.languages

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.map
import org.ccci.gto.android.common.db.Query
import org.ccci.gto.android.common.db.getAsLiveData
import org.ccci.gto.android.common.lifecycle.combineWith
import org.cru.godtools.model.Language
import org.keynote.godtools.android.db.Contract
import org.keynote.godtools.android.db.GodToolsDao

private const val KEY_QUERY = "query"
private const val KEY_IS_SEARCH_VIEW_OPEN = "isSearchViewOpen"

class LanguagesFragmentViewModel(
    application: Application,
    private val savedState: SavedStateHandle
) : AndroidViewModel(application) {
    private val dao = GodToolsDao.getInstance(application)

    // region Search
    val query: MutableLiveData<String> = savedState.getLiveData(KEY_QUERY, null)

    var isSearchViewOpen: Boolean
        get() = savedState.get<Boolean>(KEY_IS_SEARCH_VIEW_OPEN) == true
        set(value) = savedState.set(KEY_IS_SEARCH_VIEW_OPEN, value)
    // endregion Search

    // region Languages
    val showNone = MutableLiveData<Boolean>(false)

    private val rawLanguages = Query.select<Language>()
        .join(Contract.LanguageTable.SQL_JOIN_TRANSLATION)
        .where(Contract.TranslationTable.SQL_WHERE_PUBLISHED)
        .getAsLiveData(dao)
    private val sortedLanguages: LiveData<Map<String, Language>> = rawLanguages
        .map { it.associateBy { lang -> lang.getDisplayName(application) }.toSortedMap(String.CASE_INSENSITIVE_ORDER) }
    private val filteredLanguages = query.distinctUntilChanged().combineWith(sortedLanguages) { query, languages ->
        when {
            query.isNullOrEmpty() -> languages?.values?.toList()
            else -> languages?.entries?.filter { it.key.contains(query, true) }?.map { it.value }?.toList()
        }
    }

    // List of sorted filtered languages with none (null) prepended if requested
    val languages = showNone.distinctUntilChanged().combineWith(filteredLanguages) { showNone, languages ->
        when (showNone) {
            true -> listOf<Language?>(null) + languages.orEmpty()
            else -> languages.orEmpty()
        }
    }
    // endregion Languages
}
