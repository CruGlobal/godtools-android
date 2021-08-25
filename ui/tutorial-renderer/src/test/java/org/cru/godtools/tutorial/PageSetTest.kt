package org.cru.godtools.tutorial

import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.util.Locale
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.greaterThanOrEqualTo
import org.hamcrest.Matchers.hasItem
import org.hamcrest.Matchers.hasItems
import org.hamcrest.Matchers.not
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.Config.TARGET_SDK

@RunWith(AndroidJUnit4::class)
@Config(sdk = [21, TARGET_SDK])
class PageSetTest {
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

    @Test
    fun testOnboardingPages() {
        assertThat(
            PageSet.ONBOARDING.pagesFor(Locale.ENGLISH),
            allOf(
                hasItems(Page.ONBOARDING_WELCOME, Page.ONBOARDING_SHARE, Page.ONBOARDING_LINKS),
                not(hasItem(Page.ONBOARDING_SHARE_FINAL))
            )
        )
        assertThat(
            PageSet.ONBOARDING.pagesFor(Locale.GERMAN),
            allOf(
                not(hasItems(Page.ONBOARDING_SHARE, Page.ONBOARDING_LINKS)),
                hasItems(Page.ONBOARDING_WELCOME, Page.ONBOARDING_SHARE_FINAL)
            )
        )
    }
}
