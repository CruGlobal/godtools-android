package org.cru.godtools.tract.analytics.model

import java.util.Locale
import org.cru.godtools.base.tool.analytics.model.ToolAnalyticsScreenEvent

class TipAnalyticsScreenEvent(tool: String, locale: Locale, tipId: String) :
    ToolAnalyticsScreenEvent("$tool-tip-$tipId", tool, locale)
