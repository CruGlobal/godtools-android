package org.cru.godtools.base.ui.databinding

import androidx.annotation.VisibleForTesting
import androidx.databinding.BindingAdapter
import androidx.databinding.adapters.ListenerUtil
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerCallback
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.YouTubePlayerTracker
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import org.cru.godtools.base.ui.R

@BindingAdapter("pauseVideoWhen")
internal fun YouTubePlayerView.pauseVideoWhen(pause: Boolean) {
    if (!pause) return
    getYouTubePlayerWhenReady(object : YouTubePlayerCallback {
        override fun onYouTubePlayer(youTubePlayer: YouTubePlayer) = youTubePlayer.pause()
    })
}

// region recueVideo()
@BindingAdapter("recue")
internal fun YouTubePlayerView.recueVideo(recue: Boolean) {
    recueListener.enabled = recue
}

@VisibleForTesting
internal val YouTubePlayerView.recueListener: RecueYouTubePlayerListener
    get() = ListenerUtil.getListener(this, R.id.youtubeplayer_recue_listener)
        ?: RecueYouTubePlayerListener().also {
            addYouTubePlayerListener(it)
            ListenerUtil.trackListener(this, it, R.id.youtubeplayer_recue_listener)
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
// endregion recueVideo()

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

// region YouTubePlayerListener
private const val ON_PLAYBACK_ENDED = "onPlaybackEnded"

@BindingAdapter(ON_PLAYBACK_ENDED)
internal fun YouTubePlayerView.setYouTubePlayerListener(playbackEnded: OnPlaybackEnded?) {
    val newValue = when {
        playbackEnded == null -> null
        else -> object : AbstractYouTubePlayerListener() {
            override fun onStateChange(youTubePlayer: YouTubePlayer, state: PlayerConstants.PlayerState) {
                if (state == PlayerConstants.PlayerState.ENDED) playbackEnded.onPlaybackEnded(youTubePlayer)
            }
        }
    }

    ListenerUtil.trackListener(this, newValue, R.id.youtubeplayer_listener)
        ?.let { removeYouTubePlayerListener(it) }
    newValue?.let { addYouTubePlayerListener(it) }
}

internal interface OnPlaybackEnded {
    fun onPlaybackEnded(player: YouTubePlayer)
}
// endregion YouTubePlayerListener
