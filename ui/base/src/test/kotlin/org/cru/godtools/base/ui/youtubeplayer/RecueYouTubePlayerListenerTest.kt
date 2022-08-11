package org.cru.godtools.base.ui.youtubeplayer

import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerCallback
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import io.mockk.Called
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.mockk.verifyAll
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RecueYouTubePlayerListenerTest {
    private val player = mockk<YouTubePlayer>(relaxUnitFun = true)
    private lateinit var view: YouTubePlayerView

    @Before
    fun setup() {
        val tags = mutableMapOf<Any?, Any?>()

        view = mockk {
            every { getYouTubePlayerWhenReady(any()) } answers {
                (it.invocation.args[0] as YouTubePlayerCallback).onYouTubePlayer(player)
            }
            every { setTag(any(), any()) } answers { tags[it.invocation.args[0]] = it.invocation.args[1] }
            every { getTag(any()) } answers { tags[it.invocation.args[0]] }
        }
    }

    // region recueVideo()
    @Test
    fun testRecueVideoListenerEnabledAndDisabled() {
        val listener = slot<RecueYouTubePlayerListener>()
        every { view.addYouTubePlayerListener(capture(listener)) } returns true andThenThrows IllegalStateException()

        view.recueVideo = true
        verify(exactly = 1) { view.addYouTubePlayerListener(any()) }
        assertTrue(view.recueVideo)
        assertTrue(listener.captured.enabled)
        assertSame(listener.captured, view.recueListener)

        view.recueVideo = false
        assertFalse(view.recueVideo)
        assertFalse(listener.captured.enabled)
        verify(exactly = 1) { view.addYouTubePlayerListener(any()) }
        verify(exactly = 0) { view.removeYouTubePlayerListener(any()) }
    }

    @Test
    fun testRecuesVideoWhenEnabled() {
        every { view.addYouTubePlayerListener(any()) } returns true
        val listener = view.recueListener
        listener.onVideoId(player, "test")
        verify { player wasNot Called }

        listener.enabled = false
        listener.onStateChange(player, PlayerConstants.PlayerState.ENDED)
        verify { player wasNot Called }

        listener.enabled = true
        listener.onStateChange(player, PlayerConstants.PlayerState.ENDED)
        verifyAll { player.cueVideo("test", 0f) }
    }
    // endregion recueVideo()
}
