package org.cru.godtools.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.annotation.MainThread
import androidx.fragment.app.commit
import org.cru.godtools.R
import org.cru.godtools.analytics.model.AnalyticsScreenEvent
import org.cru.godtools.analytics.model.AnalyticsScreenEvent.SCREEN_LANGUAGE_SELECTION
import org.cru.godtools.base.ui.activity.BaseActivity
import org.cru.godtools.download.manager.GodToolsDownloadManager
import org.cru.godtools.fragment.LanguagesFragment
import java.util.Locale

private const val EXTRA_PRIMARY = "org.cru.godtools.activity.LanguageSelectionActivity.PRIMARY"

fun Activity.startLanguageSelectionActivity(primary: Boolean) {
    Intent(this, LanguageSelectionActivity::class.java)
        .putExtras(BaseActivity.buildExtras(this))
        .putExtra(EXTRA_PRIMARY, primary)
        .also { startActivity(it) }
}

class LanguageSelectionActivity : BasePlatformActivity(), LanguagesFragment.Callbacks {
    private val primary: Boolean by lazy { intent?.getBooleanExtra(EXTRA_PRIMARY, true) ?: true }

    // region Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_generic_fragment)
    }

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
        mEventBus.post(AnalyticsScreenEvent(SCREEN_LANGUAGE_SELECTION))
    }

    override fun onLocaleSelected(locale: Locale?) {
        GodToolsDownloadManager.getInstance(this).addLanguage(locale)
        storeLocale(locale)
        finish()
    }
    // endregion Lifecycle

    private fun storeLocale(locale: Locale?) {
        if (primary) {
            settings.setPrimaryLanguage(locale)
        } else {
            settings.parallelLanguage = locale
        }
    }

    @MainThread
    private fun loadPrimaryFragmentIfNeeded() {
        with(supportFragmentManager) {
            if (primaryNavigationFragment != null) return

            commit {
                val fragment = LanguagesFragment.newInstance(primary)
                replace(R.id.frame, fragment)
                setPrimaryNavigationFragment(fragment)
            }
        }
    }
}
