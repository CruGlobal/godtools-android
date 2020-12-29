package org.cru.godtools.base.tool.analytics.model

import org.cru.godtools.analytics.model.AnalyticsActionEvent
import org.cru.godtools.analytics.model.AnalyticsSystem

object ToolOpenedViaShortcutAnalyticsActionEvent :
    AnalyticsActionEvent("tool_opened_shortcut", system = AnalyticsSystem.FIREBASE)
