package org.cru.godtools.tutorial

import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.annotation.RawRes
import androidx.annotation.StringRes
import java.util.Locale
import org.ccci.gto.android.common.util.LocaleUtils

private val ONBOARDING_EXTENDED_LOCALES = setOf(Locale.ENGLISH, Locale.FRENCH, Locale("es"), Locale("lv"), Locale("vi"))

internal enum class Page(
    @LayoutRes val layout: Int,
    @StringRes val title: Int? = null,
    @StringRes val content: Int? = null,
    @StringRes val content2: Int? = null,
    @StringRes val action: Int? = null,
    @RawRes val animation: Int? = null,
    @DrawableRes val image: Int? = null,
    private val supportedLocales: Set<Locale> = emptySet(),
    private val disabledLocales: Set<Locale> = emptySet(),
    val showIndicator: Boolean = true,
    val showMenu: Boolean = true
) {
    ONBOARDING_WELCOME(
        R.layout.tutorial_page_compose,
        showIndicator = false,
        showMenu = false
    ),
    ONBOARDING_CONVERSATIONS(
        R.layout.tutorial_page_compose,
        title = R.string.tutorial_onboarding_conversations_headline,
        content = R.string.tutorial_onboarding_conversations_subhead,
        animation = R.raw.anim_tutorial_onboarding_guys
    ),
    ONBOARDING_PREPARE(
        R.layout.tutorial_page_compose,
        title = R.string.tutorial_onboarding_prepare_headline,
        content = R.string.tutorial_onboarding_prepare_subhead,
        animation = R.raw.anim_tutorial_onboarding_dog
    ),
    ONBOARDING_SHARE(
        R.layout.tutorial_page_compose,
        title = R.string.tutorial_onboarding_share_headline,
        content = R.string.tutorial_onboarding_share_subhead,
        animation = R.raw.anim_tutorial_onboarding_distance,
        supportedLocales = ONBOARDING_EXTENDED_LOCALES
    ),
    ONBOARDING_SHARE_FINAL(
        R.layout.tutorial_page_compose,
        title = R.string.tutorial_onboarding_share_headline,
        content = R.string.tutorial_onboarding_share_subhead,
        animation = R.raw.anim_tutorial_onboarding_distance,
        disabledLocales = ONBOARDING_EXTENDED_LOCALES,
        showMenu = false
    ),
    ONBOARDING_LINKS(
        R.layout.tutorial_page_compose,
        supportedLocales = ONBOARDING_EXTENDED_LOCALES,
        showMenu = false
    ),
    FEATURES_TOOLS(
        R.layout.tutorial_page_compose,
        title = R.string.tutorial_features_tools_headline,
        content = R.string.tutorial_features_tools_subhead,
        action = R.string.tutorial_features_action_continue,
        image = R.drawable.img_tutorial_features_tools
    ),
    FEATURES_TIPS(
        R.layout.tutorial_page_compose,
        title = R.string.tutorial_features_tips_headline,
        content = R.string.tutorial_features_tips_subhead,
        action = R.string.tutorial_features_action_continue,
        animation = R.raw.anim_tutorial_features_tips
    ),
    FEATURES_LIVE_SHARE(
        R.layout.tutorial_page_compose,
        title = R.string.tutorial_features_live_share_headline,
        content = R.string.tutorial_features_live_share_subhead,
        action = R.string.tutorial_features_action_continue,
        animation = R.raw.anim_tutorial_features_live_share
    ),
    FEATURES_LESSONS(
        R.layout.tutorial_page_compose,
        title = R.string.tutorial_features_lessons_headline,
        content = R.string.tutorial_features_lessons_subhead,
        action = R.string.tutorial_features_action_continue,
        animation = R.raw.anim_tutorial_features_lessons
    ),
    FEATURES_FINAL(
        R.layout.tutorial_page_compose,
        title = R.string.tutorial_features_final_headline,
        action = R.string.tutorial_features_action_close,
        image = R.drawable.img_tutorial_training_menu
    ),
    LIVE_SHARE_DESCRIPTION(
        R.layout.tutorial_page_compose,
        title = R.string.tutorial_live_share_description_headline,
        content = R.string.tutorial_live_share_description_text,
        action = R.string.tutorial_live_share_action_continue,
        image = R.drawable.img_tutorial_live_share_people
    ),
    LIVE_SHARE_MIRRORED(
        R.layout.tutorial_page_compose,
        title = R.string.tutorial_live_share_mirrored_headline,
        content = R.string.tutorial_live_share_mirrored_text,
        action = R.string.tutorial_live_share_action_continue,
        animation = R.raw.anim_tutorial_live_share_devices
    ),
    LIVE_SHARE_START(
        R.layout.tutorial_page_compose,
        title = R.string.tutorial_live_share_start_headline,
        content = R.string.tutorial_live_share_start_text,
        action = R.string.tutorial_live_share_action_start,
        animation = R.raw.anim_tutorial_live_share_messages,
        showMenu = false
    ),
    TIPS_LEARN(
        R.layout.tutorial_page_compose,
        title = R.string.tutorial_tips_learn_headline,
        content = R.string.tutorial_tips_learn_text,
        animation = R.raw.anim_tutorial_tips_people
    ),
    TIPS_LIGHT(
        R.layout.tutorial_page_compose,
        title = R.string.tutorial_tips_light_headline,
        content = R.string.tutorial_tips_light_text1,
        content2 = R.string.tutorial_tips_light_text2,
        animation = R.raw.anim_tutorial_tips_tool
    ),
    TIPS_START(
        R.layout.tutorial_page_compose,
        title = R.string.tutorial_tips_start_headline,
        content = R.string.tutorial_tips_start_text,
        animation = R.raw.anim_tutorial_tips_light,
    );

    fun supportsLocale(locale: Locale) =
        (supportedLocales.isEmpty() || LocaleUtils.getFallbacks(locale).any { it in supportedLocales }) &&
            (disabledLocales.isEmpty() || LocaleUtils.getFallbacks(locale).none { it in disabledLocales })
}
