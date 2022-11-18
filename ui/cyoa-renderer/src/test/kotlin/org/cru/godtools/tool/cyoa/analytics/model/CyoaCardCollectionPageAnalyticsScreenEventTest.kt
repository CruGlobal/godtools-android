package org.cru.godtools.tool.cyoa.analytics.model

import io.mockk.every
import io.mockk.mockk
import java.util.Locale
import org.cru.godtools.shared.tool.parser.model.Manifest
import org.cru.godtools.shared.tool.parser.model.page.CardCollectionPage
import org.junit.Assert.assertEquals
import org.junit.Test

class CyoaCardCollectionPageAnalyticsScreenEventTest {
    private companion object {
        private const val TOOL = "tool"
        private const val PAGE = "page"
        private const val CARD = "card"
    }

    private val card: CardCollectionPage.Card = mockk {
        every { id } returns CARD
        every { page } returns mockk {
            every { manifest } returns Manifest(code = TOOL, locale = Locale.ENGLISH)
            every { id } returns PAGE
        }
    }

    @Test
    fun `Property - screen`() {
        assertEquals("tool:page:card", CyoaCardCollectionPageAnalyticsScreenEvent(card).screen)
    }
}
