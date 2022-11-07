package org.cru.godtools.base.tool.activity

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.util.Locale
import org.ccci.gto.android.common.androidx.lifecycle.getMutableStateFlow
import org.cru.godtools.base.EXTRA_TOOL

open class BaseToolRendererViewModel(
    savedState: SavedStateHandle,
) : ViewModel() {
    protected companion object {
        internal const val STATE_ACTIVE_LOCALE = "activeLocale"
    }

    val toolCode = savedState.getMutableStateFlow<String?>(viewModelScope, EXTRA_TOOL, null)
    val locale = savedState.getMutableStateFlow<Locale?>(viewModelScope, STATE_ACTIVE_LOCALE, null)
}
