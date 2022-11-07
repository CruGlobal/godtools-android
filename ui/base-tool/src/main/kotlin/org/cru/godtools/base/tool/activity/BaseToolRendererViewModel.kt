package org.cru.godtools.base.tool.activity

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.stateIn
import org.ccci.gto.android.common.androidx.lifecycle.getMutableStateFlow
import org.ccci.gto.android.common.kotlin.coroutines.flow.combineTransformLatest
import org.cru.godtools.base.EXTRA_TOOL
import org.cru.godtools.base.tool.service.ManifestManager
import org.cru.godtools.shared.tool.parser.model.Manifest

open class BaseToolRendererViewModel(
    manifestManager: ManifestManager,
    savedState: SavedStateHandle,
) : ViewModel() {
    protected companion object {
        internal const val STATE_ACTIVE_LOCALE = "activeLocale"
    }

    val supportedType = MutableStateFlow<Manifest.Type?>(null)
    val toolCode = savedState.getMutableStateFlow<String?>(viewModelScope, EXTRA_TOOL, null)
    val locale = savedState.getMutableStateFlow<Locale?>(viewModelScope, STATE_ACTIVE_LOCALE, null)

    val manifest = toolCode
        .combineTransformLatest(locale) { tool, locale ->
            when {
                tool == null || locale == null -> emit(null)
                else -> emitAll(manifestManager.getLatestPublishedManifestFlow(tool, locale))
            }
        }
        .combine(supportedType) { m, t -> m?.takeIf { t == null || it.type == t } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)
}
