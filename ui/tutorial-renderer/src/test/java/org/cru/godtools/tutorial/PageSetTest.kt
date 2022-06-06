package org.cru.godtools.tutorial

import androidx.test.ext.junit.runners.AndroidJUnit4
import java.util.Locale
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.hasItem
import org.hamcrest.Matchers.hasItems
import org.hamcrest.Matchers.not
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
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
