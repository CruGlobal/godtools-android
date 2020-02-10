package org.cru.godtools.analytics.model

import java.util.Locale

private const val SITE_SECTION_TOOLS = "tools"
private const val SITE_SUB_SECTION_ADD_TOOLS = "add tools"

class ToolDetailsScreenEvent(toolCode: String, locale: Locale? = null) :
    AnalyticsScreenEvent("$toolCode-tool-info", locale) {
    override val adobeSiteSection: String?
        get() = SITE_SECTION_TOOLS

    override val adobeSiteSubSection: String?
        get() = SITE_SUB_SECTION_ADD_TOOLS
}
