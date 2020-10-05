package org.cru.godtools.tutorial

import java.util.Locale
import org.ccci.gto.android.common.compat.util.LocaleCompat
import org.ccci.gto.android.common.util.LocaleUtils
import org.cru.godtools.base.Settings

enum class PageSet(
    internal val feature: String? = null,
    internal val pages: List<Page>,
    internal val supportedLocales: Set<Locale> = emptySet(),
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
            Page.ONBOARDING_OTHERS,
            Page.ONBOARDING_TOOLS,
            Page.ONBOARDING_READY,
            Page.ONBOARDING_FINAL
        ),
        supportedLocales = setOf(
            Locale.ENGLISH,
            Locale("es"),
            Locale.FRENCH,
            Locale("in"),
            Locale("ru"),
            Locale.SIMPLIFIED_CHINESE,
            LocaleCompat.forLanguageTag("zh-Hans")
        )
    ),
    TRAINING(
        feature = Settings.FEATURE_TUTORIAL_TRAINING,
        analyticsBaseScreenName = "tutorial",
        pages = listOf(
            Page.TRAINING_WATCH,
            Page.TRAINING_PREPARE,
            Page.TRAINING_TRY,
            Page.TRAINING_FINAL
        ),
        supportedLocales = setOf(
            Locale.ENGLISH,
            Locale("es"),
            Locale.FRENCH,
            Locale("in"),
            Locale("ru"),
            Locale.SIMPLIFIED_CHINESE,
            LocaleCompat.forLanguageTag("zh-Hans")
        )
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
        feature = Settings.FEATURE_TUTORIAL_TIPS,
        analyticsBaseScreenName = "tutorial-tips",
        pages = listOf(
            Page.TIPS_LEARN,
            Page.TIPS_LIGHT,
            Page.TIPS_START
        )
    );

    fun supportsLocale(locale: Locale) = LocaleUtils.getFallbacks(locale).any { supportedLocales.contains(it) }

    companion object {
        val DEFAULT = ONBOARDING
    }
}
