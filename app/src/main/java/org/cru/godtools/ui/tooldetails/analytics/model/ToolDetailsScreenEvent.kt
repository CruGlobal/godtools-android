package org.cru.godtools.ui.tooldetails.analytics.model

import org.cru.godtools.analytics.model.AnalyticsScreenEvent
import java.util.Locale

class ToolDetailsScreenEvent(tool: String, locale: Locale? = null) : AnalyticsScreenEvent("$tool-tool-info", locale) {
    override val adobeSiteSection get() = ADOBE_SITE_SECTION_TOOLS
}
