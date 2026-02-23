package org.cru.godtools.tutorial.ui.youtubeplayer

import android.content.pm.ActivityInfo
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.slack.circuit.overlay.AnimatedOverlay
import com.slack.circuit.overlay.OverlayNavigator
import com.slack.circuit.overlay.OverlayTransitionController
import org.ccci.gto.android.common.androidx.activity.compose.RequestedOrientation
import org.cru.godtools.base.ui.youtubeplayer.YouTubePlayer

class YoutubePlayerOverlay(private val videoId: String) :
    AnimatedOverlay<Unit>(enterTransition = fadeIn(), exitTransition = fadeOut()) {

    @Composable
    override fun AnimatedVisibilityScope.AnimatedContent(
        navigator: OverlayNavigator<Unit>,
        transitionController: OverlayTransitionController,
    ) {
        BackHandler { navigator.finish(Unit) }

        RequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR)

        YouTubePlayer(
            videoId = videoId,
            autoPlay = true,
            recue = true,
            onPlaybackEnd = { navigator.finish(Unit) },
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.6f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) { navigator.finish(Unit) }
                .wrapContentHeight(Alignment.CenterVertically),
        )
    }
}
