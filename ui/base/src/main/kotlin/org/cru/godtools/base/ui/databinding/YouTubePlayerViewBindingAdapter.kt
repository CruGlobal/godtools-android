package org.cru.godtools.base.ui.databinding

import androidx.databinding.BindingAdapter
import androidx.databinding.adapters.ListenerUtil
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerCallback
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.YouTubePlayerTracker
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import org.cru.godtools.base.ui.youtubeplayer.recueVideo
import org.cru.godtools.ui.R

@BindingAdapter("pauseVideoWhen")
internal fun YouTubePlayerView.pauseVideoWhen(pause: Boolean) {
    if (!pause) return
    getYouTubePlayerWhenReady(object : YouTubePlayerCallback {
        override fun onYouTubePlayer(youTubePlayer: YouTubePlayer) = youTubePlayer.pause()
    })
}

@BindingAdapter("recue")
internal fun YouTubePlayerView.recueVideo(recue: Boolean) {
    recueVideo = recue
}

// region updateVideo()
@BindingAdapter("videoId")
internal fun YouTubePlayerView.updateVideo(videoId: String?) = updateVideo(videoId, false)

@BindingAdapter("videoId", "autoPlay")
internal fun YouTubePlayerView.updateVideo(videoId: String?, autoPlay: Boolean) {
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

private val YouTubePlayerView.tracker
    get() = ListenerUtil.getListener<YouTubePlayerTracker>(this, R.id.youtubeplayer_tracker)
        ?: YouTubePlayerTracker().also {
            ListenerUtil.trackListener(this, it, R.id.youtubeplayer_tracker)
            addYouTubePlayerListener(it)
        }
// region updateVideo()
