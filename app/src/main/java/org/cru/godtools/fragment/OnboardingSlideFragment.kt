package org.cru.godtools.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import org.cru.godtools.activity.OnBoardingCallbacks
import org.cru.godtools.databinding.OnboardingExploreSlideBinding


class OnboardingSlideFragment : Fragment() {

    private lateinit var callback: OnBoardingCallbacks

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return when (arguments?.getInt(ARG_SLIDE_POSITION)) {
            0 -> {
                val binding = OnboardingExploreSlideBinding.inflate(inflater, container, false).apply {
                    setYoutubePlayer()
                }
                binding.callback = callback
                return binding.root
            }
            else -> container
        }
    }

    private fun OnboardingExploreSlideBinding.setYoutubePlayer() {
        lifecycle.addObserver(youTubePlayerView)
        youTubePlayerView.getPlayerUiController().also {
            it.showVideoTitle(false)
            it.showCurrentTime(false)
            it.showDuration(false)
            it.showFullscreenButton(false)
            it.showYouTubeButton(false)
            it.showSeekBar(false)
            it.showMenuButton(false)
            it.showCustomAction1(false)
            it.showCustomAction2(false)
            it.showBufferingProgress(false)
        }
        youTubePlayerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                val videoId = "9HRYIFXbT_s"
                youTubePlayer.cueVideo(videoId, 0f)
            }
        })
    }

    fun setCallback(callback: OnBoardingCallbacks) {
        this.callback = callback
    }

    companion object {

        private const val ARG_SLIDE_POSITION = "slide_position"

        @JvmStatic
        fun newInstance(position: Int): OnboardingSlideFragment {
            return OnboardingSlideFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_SLIDE_POSITION, position)
                }
            }
        }
    }
}
