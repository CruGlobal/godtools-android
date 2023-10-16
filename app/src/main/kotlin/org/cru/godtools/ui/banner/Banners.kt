package org.cru.godtools.ui.banner

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.with
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
@OptIn(ExperimentalAnimationApi::class)
internal fun Banners(banner: () -> BannerType?, modifier: Modifier = Modifier) = Box(modifier.heightIn(min = 1.dp)) {
    AnimatedContent(
        targetState = banner(),
        transitionSpec = {
            slideInVertically(initialOffsetY = { -it }) with slideOutVertically(targetOffsetY = { -it })
        },
        label = "Banner Visibility",
    ) {
        when (it) {
            BannerType.TOOL_LIST_FAVORITES -> FavoriteToolsBanner()
            BannerType.TUTORIAL_FEATURES -> TutorialFeaturesBanner()
            else -> Unit
        }
    }
}
