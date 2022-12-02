package org.cru.godtools.ui.tooldetails.analytics.model

import org.cru.godtools.analytics.model.AnalyticsScreenEvent
import org.cru.godtools.shared.analytics.AnalyticsAppSectionNames

class ToolDetailsScreenEvent(tool: String) : AnalyticsScreenEvent("$tool-tool-info") {
    override val appSection get() = AnalyticsAppSectionNames.TOOLS
}
