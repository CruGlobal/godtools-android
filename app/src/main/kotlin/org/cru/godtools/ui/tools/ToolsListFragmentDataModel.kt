package org.cru.godtools.ui.tools

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import org.cru.godtools.base.Settings
import org.cru.godtools.base.Settings.Companion.FEATURE_TUTORIAL_FEATURES
import org.cru.godtools.tutorial.PageSet
import org.cru.godtools.ui.tools.ToolsListFragment.Companion.MODE_ADDED
import org.cru.godtools.widget.BannerType
import org.keynote.godtools.android.db.repository.ToolsRepository

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class ToolsListFragmentDataModel @Inject constructor(
    settings: Settings,
    toolsRepository: ToolsRepository
) : ViewModel() {
    val mode = MutableStateFlow(MODE_ADDED)

    val tools = mode.flatMapLatest { mode ->
        when (mode) {
            MODE_ADDED -> toolsRepository.favoriteTools
            else -> flowOf(emptyList())
        }
    }.asLiveData()

    val banner = combine(mode, settings.isFeatureDiscoveredFlow(FEATURE_TUTORIAL_FEATURES)) { mode, training ->
        when {
            mode == MODE_ADDED && !training && PageSet.FEATURES.supportsLocale(Locale.getDefault()) ->
                BannerType.TUTORIAL_FEATURES
            else -> null
        }
    }.asLiveData()
}
