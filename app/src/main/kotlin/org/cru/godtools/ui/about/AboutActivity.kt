package org.cru.godtools.ui.about

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dagger.hilt.android.AndroidEntryPoint
import org.cru.godtools.R
import org.cru.godtools.activity.BasePlatformActivity
import org.cru.godtools.analytics.model.AnalyticsScreenEvent
import org.cru.godtools.analytics.model.AnalyticsScreenEvent.Companion.SCREEN_ABOUT
import org.cru.godtools.base.ui.activity.BaseActivity
import org.cru.godtools.base.ui.theme.GodToolsAppBarColors
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
@OptIn(ExperimentalMaterial3Api::class)
class AboutActivity : BasePlatformActivity<ActivityGenericComposeWithNavDrawerBinding>() {
    // region Lifecycle
    override fun onBindingChanged() {
        super.onBindingChanged()
        binding.compose.setContent {
            GodToolsTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text(stringResource(R.string.title_about)) },
                            colors = GodToolsAppBarColors,
                            navigationIcon = {
                                IconButton(onClick = { onSupportNavigateUp() }) {
                                    Icon(Icons.Filled.ArrowBack, null)
                                }
                            },
                        )
                    }
                ) { AboutContent(modifier = Modifier.padding(it)) }
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
