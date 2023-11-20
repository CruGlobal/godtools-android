package org.cru.godtools.tutorial

import java.util.Locale
import org.junit.Assert.assertTrue
import org.junit.Test

class PageTest {
    @Test
    fun testSupportsLocaleOnboarding() {
        with(Locale.ENGLISH) {
            assertTrue(Page.ONBOARDING_WELCOME.supportsLocale(this))
            assertTrue(Page.ONBOARDING_SHARE.supportsLocale(this))
        }
        with(Locale("en", "AU")) {
            assertTrue(Page.ONBOARDING_WELCOME.supportsLocale(this))
            assertTrue(Page.ONBOARDING_SHARE.supportsLocale(this))
        }
        with(Locale.GERMAN) {
            assertTrue(Page.ONBOARDING_WELCOME.supportsLocale(this))
            assertTrue(Page.ONBOARDING_SHARE.supportsLocale(this))
        }
    }
}
