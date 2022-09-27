package org.cru.godtools.ui.languages

import android.app.Activity
import android.content.Intent
import androidx.annotation.MainThread
import androidx.fragment.app.commit
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import javax.inject.Inject
import org.cru.godtools.R
import org.cru.godtools.activity.BasePlatformActivity
import org.cru.godtools.analytics.model.AnalyticsScreenEvent
import org.cru.godtools.analytics.model.AnalyticsScreenEvent.Companion.SCREEN_LANGUAGE_SELECTION
import org.cru.godtools.base.Settings
import org.cru.godtools.base.ui.activity.BaseActivity
import org.cru.godtools.download.manager.GodToolsDownloadManager
import org.cru.godtools.ui.databinding.ActivityGenericFragmentBinding

private const val EXTRA_PRIMARY = "org.cru.godtools.ui.languages.LanguageSelectionActivity.PRIMARY"

fun Activity.startLanguageSelectionActivity(primary: Boolean) {
    Intent(this, LanguageSelectionActivity::class.java)
        .putExtras(BaseActivity.buildExtras(this))
        .putExtra(EXTRA_PRIMARY, primary)
        .also { startActivity(it) }
}

@AndroidEntryPoint
class LanguageSelectionActivity : BasePlatformActivity<ActivityGenericFragmentBinding>(), LocaleSelectedListener {
    @Inject
    internal lateinit var downloadManager: GodToolsDownloadManager

    private val primary: Boolean by lazy { intent?.getBooleanExtra(EXTRA_PRIMARY, true) ?: true }

    // region Lifecycle
    override fun onSetupActionBar() {
        super.onSetupActionBar()
        setTitle(if (primary) R.string.title_language_primary else R.string.title_language_parallel)
    }

    override fun onStart() {
        super.onStart()
        loadPrimaryFragmentIfNeeded()
    }

    override fun onResume() {
        super.onResume()
        eventBus.post(AnalyticsScreenEvent(SCREEN_LANGUAGE_SELECTION))
    }

    override fun onLocaleSelected(locale: Locale?) {
        storeLocale(locale)
        finish()
    }
    // endregion Lifecycle

    override fun inflateBinding() = ActivityGenericFragmentBinding.inflate(layoutInflater)

    private fun storeLocale(locale: Locale?) {
        if (primary) {
            settings.primaryLanguage = locale ?: Settings.defaultLanguage
        } else {
            settings.parallelLanguage = locale
        }
    }

    @MainThread
    private fun loadPrimaryFragmentIfNeeded() {
        with(supportFragmentManager) {
            if (primaryNavigationFragment != null) return

            commit {
                val fragment = LanguagesFragment(primary)
                replace(org.cru.godtools.ui.R.id.frame, fragment)
                setPrimaryNavigationFragment(fragment)
            }
        }
    }
}
