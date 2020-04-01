package org.cru.godtools.article.aem.service

import org.junit.Assert.assertEquals
import org.junit.Test

class AemArticleManagerTest {
    @Test
    fun testRoundTimestamp() {
        assertEquals(15000, AemArticleManager.roundTimestamp(15234, 1000))
    }
}
