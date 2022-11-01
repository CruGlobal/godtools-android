package org.cru.godtools.tract.analytics.model

import org.cru.godtools.base.tool.analytics.model.ToolAnalyticsScreenEvent
import org.cru.godtools.shared.tool.parser.model.tract.TractPage
import org.cru.godtools.shared.tool.parser.model.tract.TractPage.Card

class TractPageAnalyticsScreenEvent(page: TractPage, card: Card? = null) :
    ToolAnalyticsScreenEvent(page.toAnalyticsScreenName(card), page.manifest)

private fun TractPage.toAnalyticsScreenName(card: Card?) = buildString {
    append(manifest.code).append('-').append(position)
    when (val pos = card?.position) {
        null -> Unit
        // convert card index to letter 'a'-'z'
        in 0..25 -> append((97 + pos).toChar())
        else -> append('-').append(pos)
    }
}
