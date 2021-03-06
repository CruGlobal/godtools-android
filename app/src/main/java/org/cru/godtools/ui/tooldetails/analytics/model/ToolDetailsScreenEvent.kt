package org.cru.godtools.ui.tooldetails.analytics.model

import java.util.Locale
import org.cru.godtools.analytics.model.AnalyticsScreenEvent

class ToolDetailsScreenEvent(tool: String, locale: Locale? = null) : AnalyticsScreenEvent("$tool-tool-info", locale) {
    override val appSection get() = APP_SECTION_TOOLS
}
