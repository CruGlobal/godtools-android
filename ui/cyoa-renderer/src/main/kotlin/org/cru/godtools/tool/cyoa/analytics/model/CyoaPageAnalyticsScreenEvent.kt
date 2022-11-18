package org.cru.godtools.tool.cyoa.analytics.model

import org.cru.godtools.base.tool.analytics.model.ToolAnalyticsScreenEvent
import org.cru.godtools.shared.tool.analytics.ToolAnalyticsScreenNames
import org.cru.godtools.shared.tool.parser.model.page.Page

open class CyoaPageAnalyticsScreenEvent(
    page: Page,
    screen: String = ToolAnalyticsScreenNames.forCyoaPage(page)
) : ToolAnalyticsScreenEvent(
    screen = screen,
    tool = page.manifest.code,
    locale = page.manifest.locale
)
