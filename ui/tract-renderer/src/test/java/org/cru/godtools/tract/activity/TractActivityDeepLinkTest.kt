package org.cru.godtools.tract.activity

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import java.util.Locale

@RunWith(AndroidJUnit4::class)
class TractActivityDeepLinkTest {
    lateinit var activity: TractActivity

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(TractActivity::class.java).get()
    }

    @Test
    fun verifyIsDeepLinkValid() {
        with(activity) {
            assertFalse(Uri.parse("wss://knowgod.com/en/kgp").isDeepLinkValid())
            assertFalse(Uri.parse("https://example.com/en/kgp").isDeepLinkValid())
            assertFalse(Uri.parse("https://knowgod.com/en").isDeepLinkValid())
            assertTrue(Uri.parse("http://knowgod.com/en/kgp").isDeepLinkValid())
            assertTrue(Uri.parse("https://knowgod.com/en/kgp").isDeepLinkValid())
            assertTrue(Uri.parse("https://www.knowgod.com/en/kgp").isDeepLinkValid())
            assertTrue(Uri.parse("https://knowgod.com/en/fourlaws").isDeepLinkValid())
            assertTrue(Uri.parse("https://knowgod.com/en/kgp/2").isDeepLinkValid())
        }
    }

    @Test
    fun verifyExtractToolFromDeepLink() {
        with(activity) {
            assertNull(Uri.parse("https://knowgod.com/en").extractToolFromDeepLink())
            assertEquals("kgp", Uri.parse("https://knowgod.com/en/kgp").extractToolFromDeepLink())
            assertEquals("kgp", Uri.parse("https://knowgod.com/en/kgp/1").extractToolFromDeepLink())
        }
    }

    @Test
    fun verifyExtractPageFromDeepLink() {
        with(activity) {
            assertNull(Uri.parse("https://knowgod.com/en/kgp").extractPageFromDeepLink())
            assertNull(Uri.parse("https://knowgod.com/en/kgp/asdf").extractPageFromDeepLink())
            assertEquals(1, Uri.parse("https://knowgod.com/en/kgp/1").extractPageFromDeepLink())
            assertEquals(15, Uri.parse("https://knowgod.com/en/kgp/15").extractPageFromDeepLink())
        }
    }

    @Test
    fun verifyExtractLanguagesFromDeepLink() {
        Uri.parse("https://knowgod.com/en/kgp")
            .assertExtractedLanguages(listOf(Locale.ENGLISH), emptyList())
        Uri.parse("https://knowgod.com/en-CA/kgp")
            .assertExtractedLanguages(listOf(Locale.CANADA, Locale.ENGLISH), emptyList())
        Uri.parse("https://knowgod.com/en/kgp?primaryLanguage=en")
            .assertExtractedLanguages(listOf(Locale.ENGLISH), emptyList())
        Uri.parse("https://knowgod.com/fr/kgp?primaryLanguage=en")
            .assertExtractedLanguages(listOf(Locale.ENGLISH, Locale.FRENCH), emptyList())
        Uri.parse("https://knowgod.com/fr/kgp?parallelLanguage=en")
            .assertExtractedLanguages(listOf(Locale.FRENCH), listOf(Locale.ENGLISH))
        Uri.parse("https://knowgod.com/en/kgp?primaryLanguage=en&parallelLanguage=fr")
            .assertExtractedLanguages(listOf(Locale.ENGLISH), listOf(Locale.FRENCH))
    }

    private fun Uri.assertExtractedLanguages(expectedPrimary: List<Locale>, expectedParallel: List<Locale>) {
        with(activity) {
            val (primary, parallel) = extractLanguagesFromDeepLink()
            assertEquals(expectedPrimary, primary)
            assertEquals(expectedParallel, parallel)
        }
    }
}
