package org.cru.godtools.base.ui.databinding

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class YouTubePlayerViewBindingAdapterTest {
    private lateinit var player: YouTubePlayer
    private lateinit var view: YouTubePlayerView

    @Before
    fun setup() {
        player = mock()
        view = mock {
            on { setTag(any(), any()) }.thenCallRealMethod()
            on { getTag(any()) }.thenCallRealMethod()
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
}
