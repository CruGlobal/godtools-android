package org.cru.godtools.databinding

import androidx.databinding.BindingAdapter
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerCallback
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.YouTubePlayerTracker
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import org.cru.godtools.R

@BindingAdapter("videoId")
fun YouTubePlayerView.updateVideo(videoId: String?) = updateVideo(videoId, false)

@BindingAdapter("videoId", "autoPlay")
fun YouTubePlayerView.updateVideo(videoId: String?, autoPlay: Boolean) {
    getYouTubePlayerWhenReady(object : YouTubePlayerCallback {
        override fun onYouTubePlayer(youTubePlayer: YouTubePlayer) {
            when {
                tracker.videoId == videoId -> Unit
                videoId == null -> youTubePlayer.pause()
                autoPlay -> youTubePlayer.loadVideo(videoId, 0f)
                else -> youTubePlayer.cueVideo(videoId, 0f)
            }
        }
    })
}

private val YouTubePlayerView.tracker: YouTubePlayerTracker
    get() = getTag(R.id.tracker) as? YouTubePlayerTracker ?: YouTubePlayerTracker().also {
        setTag(R.id.tracker, it)
        addYouTubePlayerListener(it)
    }
