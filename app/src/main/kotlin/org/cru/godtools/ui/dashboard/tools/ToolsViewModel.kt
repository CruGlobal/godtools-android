package org.cru.godtools.ui.dashboard.tools

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import java.util.Locale

private const val KEY_SELECTED_CATEGORY = "selectedCategory"
private const val KEY_SELECTED_LANGUAGE = "selectedLanguage"
private const val KEY_LANGUAGE_QUERY = "languageQuery"

class ToolsViewModel(private val savedState: SavedStateHandle) : ViewModel() {
    // region Tools
    val selectedCategory = savedState.getStateFlow<String?>(KEY_SELECTED_CATEGORY, null)
    fun setSelectedCategory(category: String?) = savedState.set(KEY_SELECTED_CATEGORY, category)

    internal val selectedLocale = savedState.getStateFlow<Locale?>(KEY_SELECTED_LANGUAGE, null)
    fun setSelectedLocale(locale: Locale?) = savedState.set(KEY_SELECTED_LANGUAGE, locale)

    val languageQuery = savedState.getStateFlow(KEY_LANGUAGE_QUERY, "")
    fun setLanguageQuery(query: String) = savedState.set(KEY_LANGUAGE_QUERY, query)
    // endregion Tools
}
