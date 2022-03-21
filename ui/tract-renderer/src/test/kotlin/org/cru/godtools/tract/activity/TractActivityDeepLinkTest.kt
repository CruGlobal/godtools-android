package org.cru.godtools.tract.activity

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric

@RunWith(AndroidJUnit4::class)
class TractActivityDeepLinkTest {
    lateinit var activity: TractActivity

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(TractActivity::class.java).get()
    }

    @Test
    fun verifyDeepLinkSelectedLanguage() {
        with(activity) {
            assertEquals(Locale.ENGLISH, Uri.parse("https://knowgod.com/en/kgp").deepLinkSelectedLanguage)
            assertEquals(Locale.ENGLISH, Uri.parse("https://knowgod.com/en/kgp/1").deepLinkSelectedLanguage)
        }
    }

    @Test
    fun verifyDeepLinkTool() {
        with(activity) {
            assertEquals("kgp", Uri.parse("https://knowgod.com/en/kgp").deepLinkTool)
            assertEquals("kgp", Uri.parse("https://knowgod.com/en/kgp/1").deepLinkTool)
        }
    }

    @Test
    fun verifyDeepLinkPage() {
        with(activity) {
            assertNull(Uri.parse("https://knowgod.com/en/kgp").deepLinkPage)
            assertNull(Uri.parse("https://knowgod.com/en/kgp/asdf").deepLinkPage)
            assertEquals(1, Uri.parse("https://knowgod.com/en/kgp/1").deepLinkPage)
            assertEquals(15, Uri.parse("https://knowgod.com/en/kgp/15").deepLinkPage)
        }
    }

    @Test
    fun verifyDeepLinkLanguages() {
        Uri.parse("https://knowgod.com/en/kgp")
            .assertDeepLinkLanguages(listOf(Locale.ENGLISH), emptyList())
        Uri.parse("https://knowgod.com/en-CA/kgp")
            .assertDeepLinkLanguages(listOf(Locale.CANADA, Locale.ENGLISH), emptyList())
        Uri.parse("https://knowgod.com/en/kgp?primaryLanguage=en")
            .assertDeepLinkLanguages(listOf(Locale.ENGLISH), emptyList())
        Uri.parse("https://knowgod.com/fr/kgp?primaryLanguage=en")
            .assertDeepLinkLanguages(listOf(Locale.ENGLISH, Locale.FRENCH), emptyList())
        Uri.parse("https://knowgod.com/fr/kgp?parallelLanguage=en")
            .assertDeepLinkLanguages(listOf(Locale.FRENCH), listOf(Locale.ENGLISH))
        Uri.parse("https://knowgod.com/en/kgp?primaryLanguage=en&parallelLanguage=fr")
            .assertDeepLinkLanguages(listOf(Locale.ENGLISH), listOf(Locale.FRENCH))
        Uri.parse("https://knowgod.com/fr/kgp?primaryLanguage=en&parallelLanguage=fr")
            .assertDeepLinkLanguages(listOf(Locale.ENGLISH), listOf(Locale.FRENCH))
        Uri.parse("https://knowgod.com/fr/kgp?primaryLanguage=en&parallelLanguage=fr-CA")
            .assertDeepLinkLanguages(listOf(Locale.ENGLISH), listOf(Locale.CANADA_FRENCH, Locale.FRENCH))
    }

    private fun Uri.assertDeepLinkLanguages(expectedPrimary: List<Locale>, expectedParallel: List<Locale>) {
        with(activity) {
            val (primary, parallel) = deepLinkLanguages
            assertEquals(expectedPrimary, primary)
            assertEquals(expectedParallel, parallel)
        }
    }
}
