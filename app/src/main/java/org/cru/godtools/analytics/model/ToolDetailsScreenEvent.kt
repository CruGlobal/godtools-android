package org.cru.godtools.analytics.model

import java.util.Locale

class ToolDetailsScreenEvent(tool: String, locale: Locale? = null) : AnalyticsScreenEvent("$tool-tool-info", locale) {
    override val adobeSiteSection get() = ADOBE_SITE_SECTION_TOOLS
}
