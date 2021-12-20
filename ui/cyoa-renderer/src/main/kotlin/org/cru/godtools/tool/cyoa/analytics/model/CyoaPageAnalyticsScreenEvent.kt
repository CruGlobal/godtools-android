package org.cru.godtools.tool.cyoa.analytics.model

import org.cru.godtools.base.tool.analytics.model.ToolAnalyticsScreenEvent
import org.cru.godtools.tool.model.page.Page

open class CyoaPageAnalyticsScreenEvent(page: Page, suffix: String = "") : ToolAnalyticsScreenEvent(
    screen = page.toAnalyticsScreenName(suffix),
    tool = page.manifest.code,
    locale = page.manifest.locale
)

private fun Page.toAnalyticsScreenName(suffix: String) = "${manifest.code.orEmpty()}:$id$suffix"
