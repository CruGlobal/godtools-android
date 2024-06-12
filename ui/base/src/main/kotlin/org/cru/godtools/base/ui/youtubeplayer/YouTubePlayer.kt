package org.cru.godtools.base.ui.youtubeplayer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerCallback
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.YouTubePlayerTracker
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

@Composable
fun YouTubePlayer(
    videoId: String,
    modifier: Modifier = Modifier,
    autoPlay: Boolean = false,
    recue: Boolean = false,
    onPlaybackEnd: () -> Unit = {},
) {
    val videoId by rememberUpdatedState(videoId)
    val autoPlay by rememberUpdatedState(autoPlay)
    val onPlaybackEnd by rememberUpdatedState(onPlaybackEnd)

    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val tracker = remember { YouTubePlayerTracker() }

    AndroidView(
        factory = {
            YouTubePlayerView(context).apply {
                lifecycleOwner.lifecycle.addObserver(this)
                addYouTubePlayerListener(tracker)
                addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                    override fun onStateChange(youTubePlayer: YouTubePlayer, state: PlayerConstants.PlayerState) {
                        if (state == PlayerConstants.PlayerState.ENDED) onPlaybackEnd()
                    }
                })
            }
        },
        modifier = modifier,
        update = {
            it.recueVideo = recue

            it.getYouTubePlayerWhenReady(object : YouTubePlayerCallback {
                override fun onYouTubePlayer(youTubePlayer: YouTubePlayer) {
                    // update videoId if necessary
                    when {
                        videoId == tracker.videoId -> Unit
                        autoPlay -> youTubePlayer.loadVideo(videoId, 0f)
                        else -> youTubePlayer.cueVideo(videoId, 0f)
                    }
                }
            })
        }
    )
}
