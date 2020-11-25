package org.cru.godtools.base.ui.databinding

import androidx.databinding.adapters.ListenerUtil
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.clearInvocations
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import org.cru.godtools.base.ui.R
import org.junit.Assert.assertSame
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
        val listener = recueListener
        argumentCaptor<YouTubePlayerListener> {
            verify(view).addYouTubePlayerListener(capture())
            verify(view, never()).removeYouTubePlayerListener(any())

            assertSame(listener, firstValue)
        }
        clearInvocations(view)

        view.recueVideo(false)
        argumentCaptor<YouTubePlayerListener> {
            verify(view, never()).addYouTubePlayerListener(any())
            verify(view).removeYouTubePlayerListener(capture())

            assertSame(listener, firstValue)
        }
    }

    @Test
    fun testRecueVideoListener() {
        view.recueVideo(true)
        val listener = recueListener
        listener.onVideoId(player, "test")
        verify(player, never()).cueVideo(any(), any())

        listener.onStateChange(player, PlayerConstants.PlayerState.ENDED)
        verify(player).cueVideo("test", 0f)
    }

    private val recueListener
        get() = ListenerUtil.getListener<YouTubePlayerListener>(view, R.id.youtubeplayer_recue_listener)
    // endregion recueVideo()
}
