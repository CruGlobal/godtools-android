package org.cru.godtools.tract.analytics.model

import org.cru.godtools.analytics.model.AnalyticsSystem
import org.cru.godtools.base.tool.analytics.model.ToolAnalyticsScreenEvent
import java.util.Locale

class TractPageAnalyticsScreenEvent(tool: String, locale: Locale, page: Int, card: Int?) :
    ToolAnalyticsScreenEvent(tractPageToScreenName(tool, page, card), tool, locale) {
    override fun isForSystem(system: AnalyticsSystem) = system != AnalyticsSystem.APPSFLYER
}

private fun tractPageToScreenName(tool: String, page: Int, card: Int?) = buildString {
    append(tool).append('-').append(page)
    when (card) {
        null -> Unit
        // convert card index to letter 'a'-'z'
        in 0..25 -> append((97 + card).toChar())
        else -> append('-').append(card)
    }
}
