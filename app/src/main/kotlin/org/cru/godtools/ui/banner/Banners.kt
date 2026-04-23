package org.cru.godtools.ui.banner

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.cru.godtools.ui.banner.favoritetools.FavoriteToolsBannerLayout
import org.cru.godtools.ui.banner.favoritetools.FavoriteToolsBannerPresenter
import org.cru.godtools.ui.banner.tutorial.TutorialFeaturesBannerLayout
import org.cru.godtools.ui.banner.tutorial.TutorialFeaturesBannerPresenter

@Composable
internal fun Banners(state: Banner.UiState?, modifier: Modifier = Modifier) = Box(modifier.heightIn(min = 1.dp)) {
    AnimatedContent(
        targetState = state,
        transitionSpec = {
            slideInVertically(initialOffsetY = { -it }) togetherWith slideOutVertically(targetOffsetY = { -it })
        },
        label = "Banner Visibility",
        contentKey = { it?.type },
    ) {
        when (it) {
            is FavoriteToolsBannerPresenter.UiState -> FavoriteToolsBannerLayout(it)
            is TutorialFeaturesBannerPresenter.UiState -> TutorialFeaturesBannerLayout(it)
            else -> Unit
        }
    }
}
