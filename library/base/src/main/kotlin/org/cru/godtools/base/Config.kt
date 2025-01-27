package org.cru.godtools.base

const val CONFIG_TOOL_CONTENT_FEATURE_PAGE_COLLECTION = "tool_content_feature_page_collection_page_enabled"
const val CONFIG_UI_DASHBOARD_HOME_FAVORITE_TOOLS = "ui_dashboard_home_favorite_tool_cards_count"
const val CONFIG_UI_GLOBAL_ACTIVITY_ENABLED = "ui_account_globalactivity_enabled"

internal val CONFIG_DEFAULTS = mapOf(
    CONFIG_TOOL_CONTENT_FEATURE_PAGE_COLLECTION to true,
    CONFIG_UI_DASHBOARD_HOME_FAVORITE_TOOLS to 5,
    CONFIG_UI_GLOBAL_ACTIVITY_ENABLED to true,
)
