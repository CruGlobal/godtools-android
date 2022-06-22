package org.cru.godtools.tutorial

import java.util.Locale
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PageTest {
    @Test
    fun testSupportsLocaleOnboarding() {
        with(Locale.ENGLISH) {
            assertTrue(Page.ONBOARDING_WELCOME.supportsLocale(this))
            assertTrue(Page.ONBOARDING_SHARE.supportsLocale(this))
            assertTrue(Page.ONBOARDING_LINKS.supportsLocale(this))
            assertFalse(Page.ONBOARDING_SHARE_FINAL.supportsLocale(this))
        }
        with(Locale("en", "AU")) {
            assertTrue(Page.ONBOARDING_WELCOME.supportsLocale(this))
            assertTrue(Page.ONBOARDING_SHARE.supportsLocale(this))
            assertTrue(Page.ONBOARDING_LINKS.supportsLocale(this))
            assertFalse(Page.ONBOARDING_SHARE_FINAL.supportsLocale(this))
        }
        with(Locale.GERMAN) {
            assertTrue(Page.ONBOARDING_WELCOME.supportsLocale(this))
            assertTrue(Page.ONBOARDING_SHARE_FINAL.supportsLocale(this))
            assertFalse(Page.ONBOARDING_SHARE.supportsLocale(this))
            assertFalse(Page.ONBOARDING_LINKS.supportsLocale(this))
        }
    }
}
