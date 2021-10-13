package org.cru.godtools.base.ui.databinding

import androidx.databinding.adapters.ListenerUtil
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerCallback
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.YouTubePlayerTracker
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import org.cru.godtools.base.ui.R
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever

@RunWith(AndroidJUnit4::class)
class YouTubePlayerViewBindingAdapterTest {
    private lateinit var player: YouTubePlayer
    private lateinit var tracker: YouTubePlayerTracker
    private lateinit var view: YouTubePlayerView

    @Before
    fun setup() {
        player = mock()
        tracker = spy(YouTubePlayerTracker())
        view = mock {
            on { setTag(any(), any()) }.thenCallRealMethod()
            on { getTag(any()) }.thenCallRealMethod()

            on { getYouTubePlayerWhenReady(any()) } doAnswer {
                it.getArgument<YouTubePlayerCallback>(0).onYouTubePlayer(player)
            }
        }
    }

    // region recueVideo()
    @Test
    fun testRecueVideoEnabledAndDisabled() {
        view.recueVideo(true)
        argumentCaptor<RecueYouTubePlayerListener> {
            verify(view).addYouTubePlayerListener(capture())
            assertTrue(firstValue.enabled)
            assertSame(view.recueListener, firstValue)
        }

        view.recueVideo(false)
        assertFalse(view.recueListener.enabled)
        verify(view, never()).removeYouTubePlayerListener(any())
    }

    @Test
    fun testRecueVideoListener() {
        val listener = view.recueListener
        listener.onVideoId(player, "test")
        verify(player, never()).cueVideo(any(), any())

        listener.enabled = false
        listener.onStateChange(player, PlayerConstants.PlayerState.ENDED)
        verify(player, never()).cueVideo(any(), any())

        listener.enabled = true
        listener.onStateChange(player, PlayerConstants.PlayerState.ENDED)
        verify(player).cueVideo("test", 0f)
    }
    // endregion recueVideo()

    // region updateVideo()
    @Test
    fun testUpdateVideo() {
        view.updateVideo("new")
        verify(player).cueVideo(eq("new"), any())
        verifyNoMoreInteractions(player)
    }

    @Test
    fun testUpdateVideoAutoplay() {
        view.updateVideo("new", true)
        verify(player).loadVideo(eq("new"), any())
        verifyNoMoreInteractions(player)
    }

    @Test
    fun testUpdateVideoPauseWhenNull() {
        ListenerUtil.trackListener(view, tracker, R.id.youtubeplayer_tracker)
        whenever(tracker.videoId).thenReturn("current")

        view.updateVideo(null)
        verify(player).pause()
        verifyNoMoreInteractions(player)
    }

    @Test
    fun testUpdateVideoNoChange() {
        ListenerUtil.trackListener(view, tracker, R.id.youtubeplayer_tracker)
        whenever(tracker.videoId).thenReturn("current")
        view.updateVideo("current")
        verifyNoInteractions(player)
    }
    // endregion updateVideo()
}
