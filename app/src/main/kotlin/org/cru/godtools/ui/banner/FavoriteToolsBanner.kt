package org.cru.godtools.ui.banner

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import org.cru.godtools.base.Settings
import org.cru.godtools.ui.banner.favoritetools.FavoriteToolsBannerLayout
import org.cru.godtools.ui.banner.favoritetools.FavoriteToolsBannerPresenter.UiState

@Composable
@Deprecated("Switch to the state version of this banner")
internal fun FavoriteToolsBanner(modifier: Modifier = Modifier, viewModel: FavoriteToolsBannerViewModel = viewModel()) {
    FavoriteToolsBannerLayout(
        state = UiState { viewModel.dismiss() },
        modifier = modifier
    )
}

@HiltViewModel
internal class FavoriteToolsBannerViewModel @Inject constructor(val settings: Settings) : ViewModel() {
    fun dismiss() {
        settings.setFeatureDiscovered(Settings.FEATURE_TOOL_FAVORITE)
    }
}
