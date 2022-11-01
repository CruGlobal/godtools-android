package org.cru.godtools.base.tool.ui.shareable

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import org.cru.godtools.base.tool.service.ManifestManager
import org.cru.godtools.shared.tool.parser.model.shareable.ShareableImage

@HiltViewModel
class ShareableImageBottomSheetDialogFragmentDataModel @Inject constructor(manifestManager: ManifestManager) :
    ViewModel() {
    val tool = MutableStateFlow<String?>(null)
    val locale = MutableStateFlow<Locale?>(null)
    val shareableId = MutableStateFlow<String?>(null)

    val shareable = combine(tool, locale) { tool, locale -> Pair(tool, locale) }
        .flatMapLatest { (tool, locale) ->
            when {
                tool == null || locale == null -> flowOf(null)
                else -> manifestManager.getLatestPublishedManifestFlow(tool, locale)
            }
        }
        .combine(shareableId) { manifest, shareable -> manifest?.findShareable(shareable) as? ShareableImage }
        .stateIn(viewModelScope, started = SharingStarted.Eagerly, null)
}
