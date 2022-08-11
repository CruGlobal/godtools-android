package org.cru.godtools.tutorial.activity

import android.app.Activity
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.cru.godtools.base.ui.youtubeplayer.YouTubePlayer
import splitties.bundle.BundleSpec
import splitties.bundle.bundle
import splitties.bundle.withExtras
import splitties.intents.ActivityIntentSpec
import splitties.intents.activitySpec
import splitties.intents.start

internal fun Activity.startYoutubePlayerActivity(videoId: String) = start(YoutubePlayerActivity) { _, extras ->
    extras.videoId = videoId
}

internal class YoutubePlayerActivity : AppCompatActivity() {
    companion object : ActivityIntentSpec<YoutubePlayerActivity, ExtrasSpec> by activitySpec(ExtrasSpec)
    object ExtrasSpec : BundleSpec() {
        var videoId: String by bundle()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            YouTubePlayer(
                videoId = withExtras(ExtrasSpec) { videoId },
                autoPlay = true,
                recue = true,
                onPlaybackEnded = { finish() },
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { finish() }
                    .wrapContentHeight(Alignment.CenterVertically)
            )
        }
    }
}
