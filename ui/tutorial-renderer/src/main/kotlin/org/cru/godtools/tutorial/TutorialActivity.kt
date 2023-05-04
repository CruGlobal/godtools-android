package org.cru.godtools.tutorial

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import javax.inject.Inject
import org.ccci.gto.android.common.compat.content.getSerializableExtraCompat
import org.ccci.gto.android.common.util.includeFallbacks
import org.cru.godtools.base.Settings
import org.cru.godtools.base.ui.startArticlesActivity
import org.cru.godtools.base.ui.startDashboardActivity
import org.cru.godtools.base.util.deviceLocale
import org.cru.godtools.shared.analytics.TutorialAnalyticsActionNames
import org.cru.godtools.tutorial.analytics.model.TutorialAnalyticsActionEvent
import org.cru.godtools.tutorial.layout.TutorialLayout
import org.cru.godtools.tutorial.theme.GodToolsTutorialTheme
import org.greenrobot.eventbus.EventBus
import org.cru.godtools.base.ui.dashboard.Page as DashboardPage

private const val ARG_PAGE_SET = "pageSet"

// TODO: this should be dynamic based upon the available languages in the database
private val ARTICLES_SUPPORTED_LANGUAGES = setOf(
    Locale("bg"),
    Locale.ENGLISH,
    Locale("es"),
    Locale.FRENCH,
    Locale("lv"),
    Locale("ru"),
    Locale("vi")
)

fun Context.buildTutorialActivityIntent(pageSet: PageSet) = Intent(this, TutorialActivity::class.java)
    .putExtra(ARG_PAGE_SET, pageSet)

fun Context.startTutorialActivity(pageSet: PageSet) = startActivity(buildTutorialActivityIntent(pageSet))

@AndroidEntryPoint
class TutorialActivity : AppCompatActivity() {
    private val pageSet get() = intent?.getSerializableExtraCompat(ARG_PAGE_SET, PageSet::class.java) ?: PageSet.DEFAULT

    @Inject
    internal lateinit var eventBus: EventBus
    @Inject
    internal lateinit var settings: Settings

    // region Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GodToolsTutorialTheme {
                TutorialLayout(
                    pageSet,
                    onTutorialAction = { onTutorialAction(it) },
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        pageSet.feature?.let { settings.setFeatureDiscovered(it) }
    }

    override fun onBackPressed() {
        setResult(RESULT_CANCELED)
        super.onBackPressed()
    }
    // endregion Lifecycle

    // region TutorialCallbacks
    private fun onTutorialAction(action: Action) {
        when (action) {
            Action.BACK -> {
                setResult(RESULT_CANCELED)
                finish()
            }
            Action.ONBOARDING_WATCH_VIDEO -> startYoutubePlayerActivity("RvhZ_wuxAgE")
            Action.ONBOARDING_LAUNCH_ARTICLES -> {
                eventBus.post(TutorialAnalyticsActionEvent(TutorialAnalyticsActionNames.ONBOARDING_LINK_ARTICLES))
                val locale = sequenceOf(deviceLocale, Locale.ENGLISH).filterNotNull().includeFallbacks()
                    .firstOrNull { ARTICLES_SUPPORTED_LANGUAGES.contains(it) } ?: Locale.ENGLISH
                startArticlesActivity("es", locale)
                finish()
            }
            Action.ONBOARDING_LAUNCH_LESSONS -> {
                eventBus.post(TutorialAnalyticsActionEvent(TutorialAnalyticsActionNames.ONBOARDING_LINK_LESSONS))
                startDashboardActivity(DashboardPage.LESSONS)
                finish()
            }
            Action.ONBOARDING_LAUNCH_TOOLS -> {
                eventBus.post(TutorialAnalyticsActionEvent(TutorialAnalyticsActionNames.ONBOARDING_LINK_TOOLS))
                startDashboardActivity(DashboardPage.ALL_TOOLS)
                finish()
            }
            Action.ONBOARDING_SKIP -> {
                eventBus.post(TutorialAnalyticsActionEvent(TutorialAnalyticsActionNames.ONBOARDING_SKIP))
                setResult(RESULT_OK)
                finish()
            }
            Action.ONBOARDING_FINISH -> {
                eventBus.post(TutorialAnalyticsActionEvent(TutorialAnalyticsActionNames.ONBOARDING_FINISH))
                setResult(RESULT_OK)
                finish()
            }
            Action.FEATURES_FINISH,
            Action.LIVE_SHARE_SKIP,
            Action.LIVE_SHARE_FINISH,
            Action.TIPS_SKIP,
            Action.TIPS_FINISH -> {
                setResult(RESULT_OK)
                finish()
            }
        }
    }
    // endregion TutorialCallbacks
}
