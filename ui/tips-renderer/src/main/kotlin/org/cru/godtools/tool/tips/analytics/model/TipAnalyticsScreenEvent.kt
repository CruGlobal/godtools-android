package org.cru.godtools.tool.tips.analytics.model

import java.util.Locale
import org.cru.godtools.base.tool.analytics.model.ToolAnalyticsScreenEvent
import org.cru.godtools.shared.tool.analytics.ToolAnalyticsScreenNames

class TipAnalyticsScreenEvent(tool: String, locale: Locale, tipId: String, page: Int) :
    ToolAnalyticsScreenEvent(ToolAnalyticsScreenNames.forTipPage(tool, tipId, page), tool, locale)
