package org.cru.godtools.ui.about

import android.app.Activity
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import org.cru.godtools.activity.BasePlatformActivity
import org.cru.godtools.analytics.model.AnalyticsScreenEvent
import org.cru.godtools.analytics.model.AnalyticsScreenEvent.Companion.SCREEN_ABOUT
import org.cru.godtools.base.ui.activity.BaseActivity
import org.cru.godtools.base.ui.theme.GodToolsTheme
import org.cru.godtools.base.util.deviceLocale
import org.cru.godtools.databinding.ActivityGenericComposeWithNavDrawerBinding

fun Activity.startAboutActivity() {
    Intent(this, AboutActivity::class.java)
        .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        .putExtras(BaseActivity.buildExtras(this))
        .also { startActivity(it) }
}

@AndroidEntryPoint
class AboutActivity : BasePlatformActivity<ActivityGenericComposeWithNavDrawerBinding>() {
    // region Lifecycle
    override fun onBindingChanged() {
        super.onBindingChanged()
        binding.genericActivity.frame.setContent {
            GodToolsTheme {
                AboutLayout()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        eventBus.post(AnalyticsScreenEvent(SCREEN_ABOUT, deviceLocale))
    }
    // endregion Lifecycle

    override fun inflateBinding() = ActivityGenericComposeWithNavDrawerBinding.inflate(layoutInflater)
}
