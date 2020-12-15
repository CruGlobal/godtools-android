package org.cru.godtools.analytics.model

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cru.godtools.analytics.adobe.ADOBE_ATTR_LANGUAGE_SECONDARY
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AnalyticsActionEventTest {
    @Test
    fun testFirebaseParamsAdobeMigration() {
        val event = object : AnalyticsActionEvent("") {
            override val adobeAttributes = mapOf(ADOBE_ATTR_LANGUAGE_SECONDARY to "en")
        }

        val firebaseParams = event.firebaseParams
        assertTrue(firebaseParams.containsKey("cru_contentlanguagesecondary"))
        assertEquals("en", firebaseParams["cru_contentlanguagesecondary"])
        assertFalse(firebaseParams.containsKey("cru.contentlanguagesecondary"))
    }
}
