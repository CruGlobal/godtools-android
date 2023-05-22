package org.cru.godtools.ui.languages.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import org.ccci.gto.android.common.androidx.core.app.LocaleConfigCompat
import org.ccci.gto.android.common.androidx.core.os.asIterable

class AppLanguageViewModel(application: Application) : AndroidViewModel(application) {
    val languages = flow { emit(LocaleConfigCompat.getSupportedLocales(application)?.asIterable() ?: emptyList()) }
        .map { it.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.getDisplayName(it) }) }
        .distinctUntilChanged()
        .shareIn(viewModelScope, SharingStarted.WhileSubscribed(), replay = 1)
}
