package org.cru.godtools.tool.tract

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.util.Locale
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ToolDeepLinkTest {
    // region parseKnowGodDeepLink()
    @Test
    fun `parseKnowGodDeepLink() - Base`() {
        assertNotNull(TractDeepLink.parseKnowGodDeepLink(Uri.parse("https://knowgod.com/en/tool/v1/kgp"))) {
            assertEquals("kgp", it.tool)
            assertEquals(listOf(Locale.ENGLISH), it.primaryLocales)
            assertEquals(emptyList(), it.parallelLocales)
            assertEquals(Locale.ENGLISH, it.activeLocale)
            assertNull(it.page)
        }
    }

    @Test
    fun `parseKnowGodDeepLink() - With Page Number`() {
        assertNotNull(TractDeepLink.parseKnowGodDeepLink(Uri.parse("https://knowgod.com/en/tool/v1/kgp/7"))) {
            assertEquals("kgp", it.tool)
            assertEquals(listOf(Locale.ENGLISH), it.primaryLocales)
            assertEquals(emptyList(), it.parallelLocales)
            assertEquals(Locale.ENGLISH, it.activeLocale)
            assertEquals(7, it.page)
        }
    }

    @Test
    fun `parseKnowGodDeepLink() - With Primary & Parallel Locales`() {
        val uri = Uri.parse("https://knowgod.com/fr/tool/v1/kgp?primaryLanguage=en,de&parallelLanguage=fr,de")
        assertNotNull(TractDeepLink.parseKnowGodDeepLink(uri)) {
            assertEquals("kgp", it.tool)
            assertEquals(listOf(Locale.ENGLISH, Locale.GERMAN), it.primaryLocales)
            assertEquals(listOf(Locale.FRENCH, Locale.GERMAN), it.parallelLocales)
            assertEquals(Locale.FRENCH, it.activeLocale)
            assertNull(it.page)
        }
    }

    @Test
    fun `parseKnowGodDeepLink() - Invalid`() {
        assertNull(TractDeepLink.parseKnowGodDeepLink(Uri.parse("https://knowgod.com")))
        assertNull(TractDeepLink.parseKnowGodDeepLink(Uri.parse("https://knowgod.com/en/kgp")))
        assertNull(TractDeepLink.parseKnowGodDeepLink(Uri.parse("https://knowgod.com/en/tool/v2/kgp")))
        assertNull(TractDeepLink.parseKnowGodDeepLink(Uri.parse("https://knowgod.com/en/tool/v1")))
    }
    // endregion parseKnowGodDeepLink()
}
