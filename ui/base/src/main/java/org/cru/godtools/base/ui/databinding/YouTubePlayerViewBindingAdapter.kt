package org.cru.godtools.base.ui.databinding

import androidx.databinding.BindingAdapter
import androidx.databinding.adapters.ListenerUtil
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import org.cru.godtools.base.ui.R

@BindingAdapter("recue")
fun YouTubePlayerView.recueVideo(recue: Boolean) {
    val current = ListenerUtil.getListener<YouTubePlayerListener>(this, R.id.youtubeplayer_recue_listener)
    when {
        recue -> addYouTubePlayerListener(current ?: object : AbstractYouTubePlayerListener() {
            private lateinit var video: String
            override fun onVideoId(youTubePlayer: YouTubePlayer, videoId: String) {
                video = videoId
            }

            override fun onStateChange(youTubePlayer: YouTubePlayer, state: PlayerConstants.PlayerState) {
                if (state == PlayerConstants.PlayerState.ENDED) youTubePlayer.cueVideo(video, 0f)
            }
        }.also { ListenerUtil.trackListener(this, it, R.id.youtubeplayer_recue_listener) })
        current != null -> removeYouTubePlayerListener(current)
    }
}
