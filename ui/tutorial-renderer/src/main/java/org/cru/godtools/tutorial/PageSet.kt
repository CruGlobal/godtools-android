package org.cru.godtools.tutorial

import java.util.Locale
import org.ccci.gto.android.common.util.includeFallbacks
import org.cru.godtools.base.Settings

enum class PageSet(
    internal val feature: String? = null,
    private val pages: List<Page>,
    private val supportedLocales: Set<Locale> = emptySet(),
    internal val menu: Int? = null,
    internal val showUpNavigation: Boolean = true,
    internal val analyticsBaseScreenName: String
) {
    ONBOARDING(
        feature = Settings.FEATURE_TUTORIAL_ONBOARDING,
        showUpNavigation = false,
        menu = R.menu.tutorial_onboarding_menu,
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
        menu = R.menu.tutorial_live_share_menu,
        // TODO: we probably need a better analytics base screen name
        analyticsBaseScreenName = "tutorial-live-share",
        pages = listOf(
            Page.LIVE_SHARE_DESCRIPTION,
            Page.LIVE_SHARE_MIRRORED,
            Page.LIVE_SHARE_START
        )
    ),
    TIPS(
        menu = R.menu.tutorial_tips_menu,
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
