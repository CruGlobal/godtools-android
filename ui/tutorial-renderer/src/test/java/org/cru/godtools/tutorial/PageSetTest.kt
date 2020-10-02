package org.cru.godtools.tutorial

import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.util.Locale
import org.hamcrest.Matchers.greaterThanOrEqualTo
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [17, 21, 28])
class PageSetTest {
    @Test
    fun testOnboardingSupportedLanguages() {
        assertTrue(PageSet.ONBOARDING.supportsLocale(Locale.forLanguageTag("en")))
        assertTrue(PageSet.ONBOARDING.supportsLocale(Locale.forLanguageTag("zh-CN")))
        assertTrue(PageSet.ONBOARDING.supportsLocale(Locale.forLanguageTag("es-419")))
        assertTrue(PageSet.ONBOARDING.supportsLocale(Locale.forLanguageTag("es-ES")))
        assertTrue(PageSet.ONBOARDING.supportsLocale(Locale.forLanguageTag("es-MX")))
        assertTrue(PageSet.ONBOARDING.supportsLocale(Locale.forLanguageTag("fr-CA")))
        assertTrue(PageSet.ONBOARDING.supportsLocale(Locale.forLanguageTag("id")))
        assertTrue(PageSet.ONBOARDING.supportsLocale(Locale.forLanguageTag("in")))
        assertTrue(PageSet.ONBOARDING.supportsLocale(Locale.forLanguageTag("ru-MD")))
        assertTrue(PageSet.ONBOARDING.supportsLocale(Locale.forLanguageTag("zh-Hans-CN")))
    }

    @Test
    fun testOnboardingUnsupportedLanguages() {
        assertFalse(PageSet.ONBOARDING.supportsLocale(Locale.forLanguageTag("de")))

        assumeThat(Build.VERSION.SDK_INT, greaterThanOrEqualTo(Build.VERSION_CODES.LOLLIPOP))
        assertFalse(PageSet.ONBOARDING.supportsLocale(Locale.forLanguageTag("zh-TW")))
        assertFalse(PageSet.ONBOARDING.supportsLocale(Locale.forLanguageTag("zh")))
        assertFalse(PageSet.ONBOARDING.supportsLocale(Locale.forLanguageTag("zh-Hant")))
    }

    @Test
    fun testTrainingSupportedLanguages() {
        assertTrue(PageSet.TRAINING.supportsLocale(Locale.forLanguageTag("en")))
        assertTrue(PageSet.TRAINING.supportsLocale(Locale.forLanguageTag("zh-CN")))
        assertTrue(PageSet.TRAINING.supportsLocale(Locale.forLanguageTag("es-419")))
        assertTrue(PageSet.TRAINING.supportsLocale(Locale.forLanguageTag("es-ES")))
        assertTrue(PageSet.TRAINING.supportsLocale(Locale.forLanguageTag("es-MX")))
        assertTrue(PageSet.TRAINING.supportsLocale(Locale.forLanguageTag("fr-CA")))
        assertTrue(PageSet.TRAINING.supportsLocale(Locale.forLanguageTag("id")))
        assertTrue(PageSet.TRAINING.supportsLocale(Locale.forLanguageTag("in")))
        assertTrue(PageSet.TRAINING.supportsLocale(Locale.forLanguageTag("ru-MD")))
        assertTrue(PageSet.TRAINING.supportsLocale(Locale.forLanguageTag("zh-Hans-CN")))
    }

    @Test
    fun testTrainingUnsupportedLanguages() {
        assertFalse(PageSet.TRAINING.supportsLocale(Locale.forLanguageTag("de")))

        assumeThat(Build.VERSION.SDK_INT, greaterThanOrEqualTo(Build.VERSION_CODES.LOLLIPOP))
        assertFalse(PageSet.TRAINING.supportsLocale(Locale.forLanguageTag("zh-TW")))
        assertFalse(PageSet.TRAINING.supportsLocale(Locale.forLanguageTag("zh")))
        assertFalse(PageSet.TRAINING.supportsLocale(Locale.forLanguageTag("zh-Hant")))
    }
}
