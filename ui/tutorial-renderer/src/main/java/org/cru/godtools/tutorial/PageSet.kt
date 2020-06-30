package org.cru.godtools.tutorial

import org.ccci.gto.android.common.compat.util.LocaleCompat
import org.ccci.gto.android.common.util.LocaleUtils
import org.cru.godtools.base.Settings
import java.util.Locale

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
            LocaleCompat.forLanguageTag("zh-Hans"),
            Locale.SIMPLIFIED_CHINESE,
            Locale("es")
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
            LocaleCompat.forLanguageTag("zh-Hans"),
            Locale.SIMPLIFIED_CHINESE,
            Locale("es")
        )
    ),
    LIVE_SHARE(
        feature = Settings.FEATURE_TUTORIAL_LIVE_SHARE,
        menu = R.menu.tutorial_live_share_menu,
        // TODO: we probably need a better analytics base screen name
        analyticsBaseScreenName = "tutorial-live-share",
        pages = listOf(
            Page.LIVE_SHARE_DESCRIPTION,
            Page.LIVE_SHARE_MIRRORED,
            Page.LIVE_SHARE_START
        )
    );

    fun supportsLocale(locale: Locale) = LocaleUtils.getFallbacks(locale).any { supportedLocales.contains(it) }

    companion object {
        val DEFAULT = ONBOARDING
    }
}
