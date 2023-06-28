package org.cru.godtools.ui.languages

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint
import org.cru.godtools.analytics.model.AnalyticsScreenEvent
import org.cru.godtools.base.Settings.Companion.FEATURE_LANGUAGE_SETTINGS
import org.cru.godtools.base.ui.activity.BaseActivity
import org.cru.godtools.base.ui.startAppLanguageActivity
import org.cru.godtools.base.ui.theme.GodToolsTheme
import org.cru.godtools.shared.analytics.AnalyticsScreenNames
import org.cru.godtools.ui.drawer.DrawerMenuLayout
import org.cru.godtools.ui.languages.downloadable.startDownloadableLanguagesActivity

fun Context.startLanguageSettingsActivity() {
    Intent(this, LanguageSettingsActivity::class.java)
        .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        .putExtras(BaseActivity.buildExtras(this))
        .also { startActivity(it) }
}

@AndroidEntryPoint
class LanguageSettingsActivity : BaseActivity() {
    // region Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GodToolsTheme {
                DrawerMenuLayout {
                    LanguageSettingsLayout(
                        onEvent = {
                            when (it) {
                                LanguageSettingsEvent.NavigateUp -> onNavigateUp()
                                LanguageSettingsEvent.AppLanguage -> startAppLanguageActivity()
                                LanguageSettingsEvent.DownloadableLanguages -> startDownloadableLanguagesActivity()
                            }
                        }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        settings.setFeatureDiscovered(FEATURE_LANGUAGE_SETTINGS)
        eventBus.post(AnalyticsScreenEvent(AnalyticsScreenNames.SETTINGS_LANGUAGES))
    }
    // endregion Lifecycle
}
