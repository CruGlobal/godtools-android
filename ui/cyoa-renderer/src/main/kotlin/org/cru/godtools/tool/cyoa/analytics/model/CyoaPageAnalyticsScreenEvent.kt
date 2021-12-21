package org.cru.godtools.tool.cyoa.analytics.model

import org.cru.godtools.base.tool.analytics.model.ToolAnalyticsScreenEvent
import org.cru.godtools.tool.model.page.Page

private const val SEPARATOR = ":"

open class CyoaPageAnalyticsScreenEvent(page: Page, suffix: String? = null) : ToolAnalyticsScreenEvent(
    screen = page.toAnalyticsScreenName(suffix),
    tool = page.manifest.code,
    locale = page.manifest.locale
)

private fun Page.toAnalyticsScreenName(suffix: String?) =
    listOfNotNull(manifest.code.orEmpty(), id, suffix).joinToString(SEPARATOR)
