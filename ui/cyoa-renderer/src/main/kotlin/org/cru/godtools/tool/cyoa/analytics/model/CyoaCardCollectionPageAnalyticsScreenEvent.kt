package org.cru.godtools.tool.cyoa.analytics.model

import org.cru.godtools.tool.model.page.CardCollectionPage.Card

class CyoaCardCollectionPageAnalyticsScreenEvent(card: Card) : CyoaPageAnalyticsScreenEvent(card.page, suffix = card.id)
