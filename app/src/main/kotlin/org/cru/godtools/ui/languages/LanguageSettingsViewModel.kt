package org.cru.godtools.ui.languages

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.ccci.gto.android.common.androidx.core.app.LocaleConfigCompat
import org.ccci.gto.android.common.androidx.core.os.asIterable
import org.cru.godtools.base.Settings
import org.cru.godtools.db.repository.LanguagesRepository
import org.cru.godtools.model.Language

@HiltViewModel
internal class LanguageSettingsViewModel @Inject constructor(
    @ApplicationContext context: Context,
    languagesRepository: LanguagesRepository,
    settings: Settings,
) : ViewModel() {
    val appLanguage = settings.appLanguageFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), settings.appLanguage)

    val appLanguages = flow { emit(LocaleConfigCompat.getSupportedLocales(context)?.asIterable() ?: emptyList()) }
        .map { it.count() }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val pinnedLanguages = languagesRepository.getPinnedLanguagesFlow()
        .combine(settings.appLanguageFlow) { pinned, app ->
            pinned.sortedWith(Language.displayNameComparator(context, app))
                .toImmutableList()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), persistentListOf())
}
