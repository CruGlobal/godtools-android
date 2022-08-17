package org.cru.godtools.tutorial

import java.util.Locale
import org.ccci.gto.android.common.androidx.compose.material3.ui.appbar.AppBarAction
import org.ccci.gto.android.common.util.includeFallbacks
import org.cru.godtools.base.Settings

enum class PageSet(
    internal val feature: String? = null,
    private val pages: List<Page>,
    private val supportedLocales: Set<Locale> = emptySet(),
    internal val menu: List<Pair<AppBarAction, Action>> = emptyList(),
    internal val showUpNavigation: Boolean = true,
    internal val analyticsBaseScreenName: String
) {
    ONBOARDING(
        feature = Settings.FEATURE_TUTORIAL_ONBOARDING,
        showUpNavigation = false,
        menu = listOf(AppBarAction(titleRes = R.string.tutorial_onboarding_action_skip) to Action.ONBOARDING_SKIP),
        analyticsBaseScreenName = "onboarding",
        pages = listOf(
            Page.ONBOARDING_WELCOME,
            Page.ONBOARDING_CONVERSATIONS,
            Page.ONBOARDING_PREPARE,
            Page.ONBOARDING_SHARE_FINAL,
            Page.ONBOARDING_SHARE,
            Page.ONBOARDING_LINKS
        )
    ),
    FEATURES(
        feature = Settings.FEATURE_TUTORIAL_FEATURES,
        analyticsBaseScreenName = "tutorial",
        pages = listOf(
            Page.FEATURES_LESSONS,
            Page.FEATURES_TOOLS,
            Page.FEATURES_TIPS,
            Page.FEATURES_LIVE_SHARE,
            Page.FEATURES_FINAL
        ),
        supportedLocales = setOf(Locale.ENGLISH, Locale("lv"))
    ),
    LIVE_SHARE(
        menu = listOf(AppBarAction(titleRes = R.string.tutorial_live_share_action_skip) to Action.LIVE_SHARE_SKIP),
        // TODO: we probably need a better analytics base screen name
        analyticsBaseScreenName = "tutorial-live-share",
        pages = listOf(
            Page.LIVE_SHARE_DESCRIPTION,
            Page.LIVE_SHARE_MIRRORED,
            Page.LIVE_SHARE_START
        )
    ),
    TIPS(
        menu = listOf(AppBarAction(titleRes = R.string.tutorial_tips_action_skip) to Action.TIPS_SKIP),
        analyticsBaseScreenName = "tutorial-tips",
        pages = listOf(
            Page.TIPS_LEARN,
            Page.TIPS_LIGHT,
            Page.TIPS_START
        )
    );

    fun supportsLocale(locale: Locale?) =
        locale != null && sequenceOf(locale).includeFallbacks().any { supportedLocales.contains(it) }

    internal fun pagesFor(locale: Locale) = pages.filter { it.supportsLocale(locale) }

    companion object {
        val DEFAULT = ONBOARDING
    }
}
