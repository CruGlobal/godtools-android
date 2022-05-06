package org.cru.godtools.ui.tooldetails.analytics.model

import org.cru.godtools.analytics.model.AnalyticsScreenEvent

class ToolDetailsScreenEvent(tool: String) : AnalyticsScreenEvent("$tool-tool-info") {
    override val appSection get() = APP_SECTION_TOOLS
}
