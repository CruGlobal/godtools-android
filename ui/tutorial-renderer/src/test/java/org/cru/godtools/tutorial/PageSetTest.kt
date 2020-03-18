package org.cru.godtools.tutorial

import junitparams.JUnitParamsRunner
import junitparams.Parameters
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Locale

@RunWith(JUnitParamsRunner::class)
class PageSetTest {
    @Test
    @Parameters("en", "zh-CN", "es-419", "es-ES", "es-MX", "zh-Hans-CN")
    fun testOnboardingSupportedLanguages(code: String) {
        assertTrue(PageSet.ONBOARDING.supportsLocale(Locale.forLanguageTag(code)))
    }

    @Test
    @Parameters("fr", "zh-TW")
    fun testOnboardingUnsupportedLanguages(code: String) {
        assertFalse(PageSet.ONBOARDING.supportsLocale(Locale.forLanguageTag(code)))
    }

    @Test
    @Parameters("en", "zh-CN", "es-419", "es-ES", "es-MX", "zh-Hans-CN")
    fun testTrainingSupportedLanguages(code: String) {
        assertTrue(PageSet.TRAINING.supportsLocale(Locale.forLanguageTag(code)))
    }

    @Test
    @Parameters("fr", "zh-TW")
    fun testTrainingUnsupportedLanguages(code: String) {
        assertFalse(PageSet.TRAINING.supportsLocale(Locale.forLanguageTag(code)))
    }
}
