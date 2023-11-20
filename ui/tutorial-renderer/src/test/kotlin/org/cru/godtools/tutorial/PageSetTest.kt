package org.cru.godtools.tutorial

import java.util.Locale
import org.hamcrest.Matchers.not
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PageSetTest {
    @Test
    fun testTrainingSupportedLanguages() {
        assertTrue(PageSet.FEATURES.supportsLocale(Locale.forLanguageTag("en")))
        assertTrue(PageSet.FEATURES.supportsLocale(Locale.forLanguageTag("en-AU")))
    }

    @Test
    fun testTrainingUnsupportedLanguages() {
        assertFalse(PageSet.FEATURES.supportsLocale(Locale.forLanguageTag("de")))
        assertFalse(PageSet.FEATURES.supportsLocale(Locale.forLanguageTag("zh-TW")))
        assertFalse(PageSet.FEATURES.supportsLocale(Locale.forLanguageTag("zh")))
        assertFalse(PageSet.FEATURES.supportsLocale(Locale.forLanguageTag("zh-Hant")))
    }
}
