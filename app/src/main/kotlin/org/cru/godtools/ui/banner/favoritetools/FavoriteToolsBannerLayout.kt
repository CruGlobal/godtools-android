package org.cru.godtools.ui.banner.favoritetools

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import org.cru.godtools.R
import org.cru.godtools.ui.banner.MaterialBanner
import org.cru.godtools.ui.banner.favoritetools.FavoriteToolsBannerPresenter.UiState

@Composable
internal fun FavoriteToolsBannerLayout(state: UiState, modifier: Modifier = Modifier) {
    MaterialBanner(
        text = stringResource(R.string.tools_list_favorites_banner_text),
        primaryButton = stringResource(R.string.tools_list_favorites_banner_action_dismiss),
        primaryAction = { state.eventSink(FavoriteToolsBannerPresenter.UiEvent.Dismiss) },
        icon = painterResource(R.drawable.ic_favorite_24dp),
        iconTint = MaterialTheme.colorScheme.primary,
        modifier = modifier
    )
}
