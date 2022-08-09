package org.cru.godtools.base.ui.databinding

import androidx.databinding.adapters.ListenerUtil
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerCallback
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.YouTubePlayerTracker
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import io.mockk.Called
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import io.mockk.verifyAll
import org.cru.godtools.base.ui.R
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class YouTubePlayerViewBindingAdapterTest {
    private val player = mockk<YouTubePlayer>(relaxUnitFun = true)
    private val tracker = spyk(YouTubePlayerTracker())
    private lateinit var view: YouTubePlayerView

    @Before
    fun setup() {
        view = spyk(YouTubePlayerView(ApplicationProvider.getApplicationContext())) {
            every { getYouTubePlayerWhenReady(any()) } answers {
                (it.invocation.args[0] as YouTubePlayerCallback).onYouTubePlayer(player)
            }
        }
    }

    // region updateVideo()
    @Test
    fun testUpdateVideo() {
        view.updateVideo("new")
        verifyAll { player.cueVideo("new", any()) }
    }

    @Test
    fun testUpdateVideoAutoplay() {
        view.updateVideo("new", true)
        verifyAll { player.loadVideo("new", any()) }
    }

    @Test
    fun testUpdateVideoPauseWhenNull() {
        ListenerUtil.trackListener(view, tracker, R.id.youtubeplayer_tracker)
        every { tracker.videoId } returns "current"

        view.updateVideo(null)
        verifyAll { player.pause() }
    }

    @Test
    fun testUpdateVideoNoChange() {
        ListenerUtil.trackListener(view, tracker, R.id.youtubeplayer_tracker)
        every { tracker.videoId } returns "current"
        view.updateVideo("current")
        verify { player wasNot Called }
    }
    // endregion updateVideo()
}
