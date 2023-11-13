package org.cru.godtools.ui.languages.app

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.shareIn
import org.ccci.gto.android.common.androidx.core.app.LocaleConfigCompat
import org.ccci.gto.android.common.androidx.core.os.asIterable
import org.cru.godtools.base.Settings

@HiltViewModel
class AppLanguageViewModel @Inject constructor(
    @ApplicationContext context: Context,
    settings: Settings,
) : ViewModel() {
    val languages = flow { emit(LocaleConfigCompat.getSupportedLocales(context)?.asIterable() ?: emptyList()) }
        .combine(settings.appLanguageFlow) { langs, appLang ->
            langs.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.getDisplayName(appLang) })
        }
        .distinctUntilChanged()
        .shareIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), replay = 1)
}
