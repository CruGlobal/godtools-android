package org.cru.godtools.tract.analytics.model

import org.cru.godtools.base.tool.analytics.model.ToolAnalyticsScreenEvent
import org.cru.godtools.shared.tool.analytics.ToolAnalyticsScreenNames
import org.cru.godtools.shared.tool.parser.model.tract.TractPage
import org.cru.godtools.shared.tool.parser.model.tract.TractPage.Card

class TractPageAnalyticsScreenEvent(page: TractPage, card: Card? = null) :
    ToolAnalyticsScreenEvent(ToolAnalyticsScreenNames.forTractPage(page, card), page.manifest)
