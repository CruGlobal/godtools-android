package org.cru.godtools.tract.util

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DeepLinkUtilsTest {
    @Test
    fun verifyIsTractDeepLink() {
        assertFalse(Uri.parse("wss://knowgod.com/en/kgp").isTractDeepLink())
        assertFalse(Uri.parse("https://example.com/en/kgp").isTractDeepLink())
        assertFalse(Uri.parse("https://knowgod.com/en").isTractDeepLink())
        assertTrue(Uri.parse("http://knowgod.com/en/kgp").isTractDeepLink())
        assertTrue(Uri.parse("https://knowgod.com/en/kgp").isTractDeepLink())
        assertFalse(Uri.parse("https://www.knowgod.com/en/kgp").isTractDeepLink())
        assertTrue(Uri.parse("https://knowgod.com/en/fourlaws").isTractDeepLink())
        assertTrue(Uri.parse("https://knowgod.com/en/kgp/2").isTractDeepLink())
    }
}
