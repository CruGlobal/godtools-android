package org.cru.godtools.tool.cyoa.analytics.model

import org.cru.godtools.shared.tool.analytics.ToolAnalyticsScreenNames
import org.cru.godtools.shared.tool.parser.model.page.CardCollectionPage.Card

class CyoaCardCollectionPageAnalyticsScreenEvent(card: Card) :
    CyoaPageAnalyticsScreenEvent(card.page, ToolAnalyticsScreenNames.forCyoaCardCollectionCard(card))
