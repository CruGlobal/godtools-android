package org.cru.godtools.tutorial.activity

import android.app.Activity
import org.cru.godtools.base.ui.activity.BaseActivity
import org.cru.godtools.tutorial.R
import org.cru.godtools.tutorial.databinding.TutorialYoutubeActivityBinding
import splitties.bundle.BundleSpec
import splitties.bundle.bundle
import splitties.bundle.withExtras
import splitties.intents.ActivityIntentSpec
import splitties.intents.activitySpec
import splitties.intents.start

internal fun Activity.startYoutubePlayerActivity(videoId: String) = start(YoutubePlayerActivity) { _, extras ->
    extras.videoId = videoId
}

internal class YoutubePlayerActivity :
    BaseActivity<TutorialYoutubeActivityBinding>(R.layout.tutorial_youtube_activity) {
    companion object : ActivityIntentSpec<YoutubePlayerActivity, ExtrasSpec> by activitySpec(ExtrasSpec)
    object ExtrasSpec : BundleSpec() {
        var videoId: String by bundle()
    }

    override fun inflateBinding() = TutorialYoutubeActivityBinding.inflate(layoutInflater)

    override fun onBindingChanged() {
        super.onBindingChanged()
        binding.activity = this
        binding.lifecycleOwner2 = this
        withExtras(ExtrasSpec) {
            binding.videoId = videoId
        }
    }
}
