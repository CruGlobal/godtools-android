package org.cru.godtools.article.aem.service

import org.junit.Assert.assertEquals
import org.junit.Test

class AEMDownloadManagerTest {
    @Test
    fun testRoundTimestamp() {
        assertEquals(15000, AemArticleManger.roundTimestamp(15234, 1000))
    }
}
