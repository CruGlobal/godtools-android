package org.cru.godtools.base.ui.youtubeplayer

import android.app.Application
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.testing.TestLifecycleOwner
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlin.test.Test
import kotlin.test.assertEquals
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
@OptIn(ExperimentalTestApi::class)
class YouTubePlayerTest {
    private val lifecycleOwner = TestLifecycleOwner()

    // region Lifecycle
    @Test
    fun `YouTubePlayer() - Lifecycle - observer removed when leaving composition`() = runComposeUiTest {
        var showPlayer by mutableStateOf(true)
        setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
                if (showPlayer) YouTubePlayer(videoId = "video")
            }
        }
        assertEquals(1, lifecycleOwner.observerCount)

        showPlayer = false
        waitForIdle()
        assertEquals(0, lifecycleOwner.observerCount)
    }
    // endregion Lifecycle
}
