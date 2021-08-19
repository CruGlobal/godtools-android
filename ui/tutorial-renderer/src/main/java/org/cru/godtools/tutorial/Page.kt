package org.cru.godtools.tutorial

import androidx.annotation.LayoutRes
import java.util.Locale
import org.ccci.gto.android.common.util.LocaleUtils

private val ONBOARDING_EXTENDED_LOCALES = setOf(Locale.ENGLISH)

internal enum class Page(
    @LayoutRes val layout: Int,
    private val supportedLocales: Set<Locale> = emptySet(),
    private val disabledLocales: Set<Locale> = emptySet(),
    val showIndicator: Boolean = true,
    val showMenu: Boolean = true
) {
    ONBOARDING_WELCOME(
        R.layout.tutorial_onboarding_welcome,
        showIndicator = false,
        showMenu = false
    ),
    ONBOARDING_CONVERSATIONS(R.layout.tutorial_onboarding_conversations),
    ONBOARDING_PREPARE(R.layout.tutorial_onboarding_prepare),
    ONBOARDING_SHARE(R.layout.tutorial_onboarding_share, supportedLocales = ONBOARDING_EXTENDED_LOCALES),
    ONBOARDING_SHARE_FINAL(
        R.layout.tutorial_onboarding_share,
        disabledLocales = ONBOARDING_EXTENDED_LOCALES,
        showMenu = false
    ),
    ONBOARDING_LINKS(
        R.layout.tutorial_onboarding_links,
        supportedLocales = ONBOARDING_EXTENDED_LOCALES,
        showMenu = false
    ),
    TRAINING_WATCH(R.layout.tutorial_training_watch),
    TRAINING_PREPARE(R.layout.tutorial_training_prepare),
    TRAINING_TRY(R.layout.tutorial_training_try),
    TRAINING_FINAL(R.layout.tutorial_training_final),
    LIVE_SHARE_DESCRIPTION(R.layout.tutorial_live_share_description),
    LIVE_SHARE_MIRRORED(R.layout.tutorial_live_share_mirrored),
    LIVE_SHARE_START(R.layout.tutorial_live_share_start, showMenu = false),
    TIPS_LEARN(R.layout.tutorial_tips_learn),
    TIPS_LIGHT(R.layout.tutorial_tips_light),
    TIPS_START(R.layout.tutorial_tips_start);

    fun supportsLocale(locale: Locale) =
        (supportedLocales.isEmpty() || LocaleUtils.getFallbacks(locale).any { it in supportedLocales }) &&
            (disabledLocales.isEmpty() || LocaleUtils.getFallbacks(locale).none { it in disabledLocales })
}
