package org.cru.godtools.base.ui.youtubeplayer

import androidx.annotation.VisibleForTesting
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import org.cru.godtools.ui.R

var YouTubePlayerView.recueVideo
    get() = recueListener.enabled
    set(value) {
        recueListener.enabled = value
    }

@VisibleForTesting
internal val YouTubePlayerView.recueListener: RecueYouTubePlayerListener
    get() = getTag(R.id.youtubeplayer_recue_listener) as? RecueYouTubePlayerListener
        ?: RecueYouTubePlayerListener().also {
            addYouTubePlayerListener(it)
            setTag(R.id.youtubeplayer_recue_listener, it)
        }

@VisibleForTesting
internal class RecueYouTubePlayerListener : AbstractYouTubePlayerListener() {
    var enabled = false
    private var video: String? = null

    override fun onVideoId(youTubePlayer: YouTubePlayer, videoId: String) {
        video = videoId
    }

    override fun onStateChange(youTubePlayer: YouTubePlayer, state: PlayerConstants.PlayerState) {
        if (state == PlayerConstants.PlayerState.ENDED && enabled) video?.let { youTubePlayer.cueVideo(it, 0f) }
    }
}
