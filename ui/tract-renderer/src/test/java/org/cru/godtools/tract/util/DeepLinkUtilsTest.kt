package org.cru.godtools.tract.util

import android.app.Activity
import android.content.Context
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric

@RunWith(AndroidJUnit4::class)
class DeepLinkUtilsTest {
    lateinit var context: Context

    @Before
    fun setup() {
        context = Robolectric.buildActivity(Activity::class.java).get()
    }

    @Test
    fun verifyIsTractDeepLink() {
        assertFalse(Uri.parse("wss://knowgod.com/en/kgp").isTractDeepLink(context))
        assertFalse(Uri.parse("https://example.com/en/kgp").isTractDeepLink(context))
        assertFalse(Uri.parse("https://knowgod.com/en").isTractDeepLink(context))
        assertTrue(Uri.parse("http://knowgod.com/en/kgp").isTractDeepLink(context))
        assertTrue(Uri.parse("https://knowgod.com/en/kgp").isTractDeepLink(context))
        assertTrue(Uri.parse("https://www.knowgod.com/en/kgp").isTractDeepLink(context))
        assertTrue(Uri.parse("https://knowgod.com/en/fourlaws").isTractDeepLink(context))
        assertTrue(Uri.parse("https://knowgod.com/en/kgp/2").isTractDeepLink(context))
    }
}
