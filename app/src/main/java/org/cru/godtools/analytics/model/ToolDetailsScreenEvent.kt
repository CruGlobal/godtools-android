package org.cru.godtools.analytics.model

import java.util.Locale

class ToolDetailsScreenEvent(toolCode: String, locale: Locale? = null) :
    AnalyticsScreenEvent("$toolCode-tool-info", locale) {
    override val adobeSiteSection: String?
        get() = SITE_SECTION_TOOLS
}
