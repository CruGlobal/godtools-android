package org.cru.godtools.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.annotation.MainThread
import androidx.fragment.app.transaction
import org.cru.godtools.R
import org.cru.godtools.analytics.model.AnalyticsScreenEvent
import org.cru.godtools.analytics.model.AnalyticsScreenEvent.SCREEN_LANGUAGE_SELECTION
import org.cru.godtools.base.ui.activity.BaseActivity
import org.cru.godtools.download.manager.GodToolsDownloadManager
import org.cru.godtools.fragment.LanguagesFragment
import java.util.Locale

private const val EXTRA_PRIMARY = "org.cru.godtools.activity.LanguageSelectionActivity.PRIMARY"

private const val TAG_MAIN_FRAGMENT = "mainFragment"

fun Activity.startLanguageSelectionActivity(primary: Boolean) {
    Intent(this, LanguageSelectionActivity::class.java)
        .putExtras(BaseActivity.buildExtras(this))
        .putExtra(EXTRA_PRIMARY, primary)
        .also { startActivity(it) }
}

class LanguageSelectionActivity : BasePlatformActivity(), LanguagesFragment.Callbacks {
    private val primary: Boolean by lazy { intent?.getBooleanExtra(EXTRA_PRIMARY, true) ?: true }

    // region Lifecycle Events

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
        loadInitialFragmentIfNeeded()
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

    // endregion Lifecycle Events

    private fun storeLocale(locale: Locale?) {
        if (primary) {
            settings.setPrimaryLanguage(locale)
        } else {
            settings.parallelLanguage = locale
        }
    }

    @MainThread
    private fun loadInitialFragmentIfNeeded() {
        supportFragmentManager?.apply {
            if (findFragmentByTag(TAG_MAIN_FRAGMENT) == null) {
                transaction {
                    replace(R.id.frame, LanguagesFragment.newInstance(primary), TAG_MAIN_FRAGMENT)
                }
            }
        }
    }
}
