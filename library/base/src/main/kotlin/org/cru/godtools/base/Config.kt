package org.cru.godtools.base

const val CONFIG_TOOL_CONTENT_FEATURE_PAGE_COLLECTION = "tool_content_feature_page_collection_page_enabled"
const val CONFIG_TUTORIAL_LESSON_PAGE_SWIPE = "tutorial_lesson_page_swipe_enabled"
const val CONFIG_UI_DASHBOARD_HOME_FAVORITE_TOOLS = "ui_dashboard_home_favorite_tool_cards_count"
const val CONFIG_UI_GLOBAL_ACTIVITY_ENABLED = "ui_account_globalactivity_enabled"

// region optInNotification
const val CONFIG_UI_OPT_IN_NOTIFICATION_ENABLED = "ui_opt_in_notification_enabled"
const val CONFIG_UI_OPT_IN_NOTIFICATION_TIME_INTERVAL = "ui_opt_in_notification_time_interval"
const val CONFIG_UI_OPT_IN_NOTIFICATION_PROMPT_LIMIT = "ui_opt_in_notification_prompt_limit"
// endregion optInNotification

internal val CONFIG_DEFAULTS = mapOf(
    CONFIG_TOOL_CONTENT_FEATURE_PAGE_COLLECTION to true,
    CONFIG_TUTORIAL_LESSON_PAGE_SWIPE to true,
    CONFIG_UI_DASHBOARD_HOME_FAVORITE_TOOLS to 5,
    CONFIG_UI_GLOBAL_ACTIVITY_ENABLED to true,

    // region optInNotification
    CONFIG_UI_OPT_IN_NOTIFICATION_ENABLED to true,
    CONFIG_UI_OPT_IN_NOTIFICATION_TIME_INTERVAL to 41,
    CONFIG_UI_OPT_IN_NOTIFICATION_PROMPT_LIMIT to 5,
    // endregion optInNotification

)
