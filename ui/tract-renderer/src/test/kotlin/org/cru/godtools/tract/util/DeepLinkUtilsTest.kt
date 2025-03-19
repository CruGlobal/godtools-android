package org.cru.godtools.tract.util

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DeepLinkUtilsTest {
    @Test
    fun verifyIsTractLegacyDeepLink() {
        assertFalse(Uri.parse("wss://knowgod.com/en/kgp").isTractLegacyDeepLink())
        assertFalse(Uri.parse("https://example.com/en/kgp").isTractLegacyDeepLink())
        assertFalse(Uri.parse("https://knowgod.com/en").isTractLegacyDeepLink())
        assertTrue(Uri.parse("http://knowgod.com/en/kgp").isTractLegacyDeepLink())
        assertTrue(Uri.parse("https://knowgod.com/en/kgp").isTractLegacyDeepLink())
        assertFalse(Uri.parse("https://www.knowgod.com/en/kgp").isTractLegacyDeepLink())
        assertTrue(Uri.parse("https://knowgod.com/en/fourlaws").isTractLegacyDeepLink())
        assertTrue(Uri.parse("https://knowgod.com/en/kgp/2").isTractLegacyDeepLink())
    }
}
