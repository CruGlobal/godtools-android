package org.cru.godtools.ui.banner

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import org.cru.godtools.R
import org.cru.godtools.base.Settings

@Composable
internal fun FavoriteToolsBanner(modifier: Modifier = Modifier) {
    val viewModel = viewModel<FavoriteToolsBannerViewModel>()

    Banner(
        text = stringResource(R.string.tools_list_favorites_banner_text),
        primaryButton = stringResource(R.string.tools_list_favorites_banner_action_dismiss),
        primaryAction = { viewModel.dismiss() },
        icon = painterResource(R.drawable.ic_favorite_24dp),
        iconTint = MaterialTheme.colorScheme.primary,
        modifier = modifier
    )
}

@HiltViewModel
internal class FavoriteToolsBannerViewModel @Inject constructor(val settings: Settings) : ViewModel() {
    fun dismiss() {
        settings.setFeatureDiscovered(Settings.FEATURE_TOOL_FAVORITE)
    }
}
