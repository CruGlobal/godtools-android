package org.cru.godtools.base.tool.analytics.model

import org.cru.godtools.analytics.model.AnalyticsActionEvent
import org.cru.godtools.analytics.model.AnalyticsSystem

object ToolOpenedViaShortcutAnalyticsActionEvent :
    AnalyticsActionEvent("tool-opened-shortcut", system = AnalyticsSystem.ADOBE)
