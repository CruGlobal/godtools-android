package org.cru.godtools.tool.cyoa

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.util.Locale
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CyoaDeepLinkTest {
    // region parseKnowGodDeepLink()
    @Test
    fun `parseKnowGodDeepLink() - Base`() {
        assertNotNull(CyoaDeepLink.parseKnowGodDeepLink(Uri.parse("https://knowgod.com/en/tool/v2/openers"))) {
            assertEquals("openers", it.tool)
            assertEquals(listOf(Locale.ENGLISH), it.primaryLocales)
            assertEquals(emptyList(), it.parallelLocales)
            assertEquals(Locale.ENGLISH, it.activeLocale)
            assertNull(it.page)
        }
    }

    @Test
    fun `parseKnowGodDeepLink() - With Page Number`() {
        assertNotNull(CyoaDeepLink.parseKnowGodDeepLink(Uri.parse("https://knowgod.com/en/tool/v2/openers/family"))) {
            assertEquals("openers", it.tool)
            assertEquals(listOf(Locale.ENGLISH), it.primaryLocales)
            assertEquals(emptyList(), it.parallelLocales)
            assertEquals(Locale.ENGLISH, it.activeLocale)
            assertEquals("family", it.page)
        }
    }

    @Test
    fun `parseKnowGodDeepLink() - Invalid`() {
        assertNull(CyoaDeepLink.parseKnowGodDeepLink(Uri.parse("https://knowgod.com/")))
        assertNull(CyoaDeepLink.parseKnowGodDeepLink(Uri.parse("https://knowgod.com/tool/v2/openers")))
        assertNull(CyoaDeepLink.parseKnowGodDeepLink(Uri.parse("https://knowgod.com/en/tool/v2")))
        assertNull(CyoaDeepLink.parseKnowGodDeepLink(Uri.parse("https://knowgod.com/en/tool/v1/openers")))
    }
    // endregion parseKnowGodDeepLink()
}
