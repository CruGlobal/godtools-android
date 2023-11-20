package org.cru.godtools.tutorial

import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import androidx.annotation.StringRes

internal enum class Page(
    @StringRes val title: Int? = null,
    @StringRes val content: Int? = null,
    @StringRes val content2: Int? = null,
    @StringRes val action: Int? = null,
    @RawRes val animation: Int? = null,
    @DrawableRes val image: Int? = null,
    val showIndicator: Boolean = true,
    val showMenu: Boolean = true
) {
    ONBOARDING_WELCOME(
        showIndicator = false,
        showMenu = false
    ),
    ONBOARDING_CONVERSATIONS(
        title = R.string.tutorial_onboarding_conversations_headline,
        content = R.string.tutorial_onboarding_conversations_subhead,
        action = R.string.tutorial_onboarding_action_next,
        animation = R.raw.anim_tutorial_onboarding_guys
    ),
    ONBOARDING_PREPARE(
        title = R.string.tutorial_onboarding_prepare_headline,
        content = R.string.tutorial_onboarding_prepare_subhead,
        action = R.string.tutorial_onboarding_action_next,
        animation = R.raw.anim_tutorial_onboarding_dog
    ),
    ONBOARDING_SHARE(
        title = R.string.tutorial_onboarding_share_headline,
        content = R.string.tutorial_onboarding_share_subhead,
        action = R.string.tutorial_onboarding_action_start,
        animation = R.raw.anim_tutorial_onboarding_distance,
        showMenu = false
    ),
    FEATURES_TOOLS(
        title = R.string.tutorial_features_tools_headline,
        content = R.string.tutorial_features_tools_subhead,
        action = R.string.tutorial_features_action_continue,
        image = R.drawable.img_tutorial_features_tools
    ),
    FEATURES_TIPS(
        title = R.string.tutorial_features_tips_headline,
        content = R.string.tutorial_features_tips_subhead,
        action = R.string.tutorial_features_action_continue,
        animation = R.raw.anim_tutorial_features_tips
    ),
    FEATURES_LIVE_SHARE(
        title = R.string.tutorial_features_live_share_headline,
        content = R.string.tutorial_features_live_share_subhead,
        action = R.string.tutorial_features_action_continue,
        animation = R.raw.anim_tutorial_features_live_share
    ),
    FEATURES_LESSONS(
        title = R.string.tutorial_features_lessons_headline,
        content = R.string.tutorial_features_lessons_subhead,
        action = R.string.tutorial_features_action_continue,
        animation = R.raw.anim_tutorial_features_lessons
    ),
    FEATURES_FINAL(
        title = R.string.tutorial_features_final_headline,
        action = R.string.tutorial_features_action_close,
        image = R.drawable.img_tutorial_training_menu
    ),
    LIVE_SHARE_DESCRIPTION(
        title = R.string.tutorial_live_share_description_headline,
        content = R.string.tutorial_live_share_description_text,
        action = R.string.tutorial_live_share_action_continue,
        image = R.drawable.img_tutorial_live_share_people
    ),
    LIVE_SHARE_MIRRORED(
        title = R.string.tutorial_live_share_mirrored_headline,
        content = R.string.tutorial_live_share_mirrored_text,
        action = R.string.tutorial_live_share_action_continue,
        animation = R.raw.anim_tutorial_live_share_devices
    ),
    LIVE_SHARE_START(
        title = R.string.tutorial_live_share_start_headline,
        content = R.string.tutorial_live_share_start_text,
        action = R.string.tutorial_live_share_action_start,
        animation = R.raw.anim_tutorial_live_share_messages,
        showMenu = false
    ),
    TIPS_LEARN(
        title = R.string.tutorial_tips_learn_headline,
        content = R.string.tutorial_tips_learn_text,
        animation = R.raw.anim_tutorial_tips_people
    ),
    TIPS_LIGHT(
        title = R.string.tutorial_tips_light_headline,
        content = R.string.tutorial_tips_light_text1,
        content2 = R.string.tutorial_tips_light_text2,
        animation = R.raw.anim_tutorial_tips_tool
    ),
    TIPS_START(
        title = R.string.tutorial_tips_start_headline,
        content = R.string.tutorial_tips_start_text,
        animation = R.raw.anim_tutorial_tips_light,
        showMenu = false
    )
}
