package org.cru.godtools.tool.cyoa.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import org.ccci.gto.android.common.androidx.lifecycle.delegate
import org.cru.godtools.base.EXTRA_PAGE

class CyoaActivitySavedState(savedStateHandle: SavedStateHandle) : ViewModel() {
    var initialPage by savedStateHandle.delegate<String?>(EXTRA_PAGE)
}
