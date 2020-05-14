package org.cru.godtools.tract.activity

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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
    fun verifyIsDeepLinkValid() {
        assertFalse(activity.isDeepLinkValid(null))
        assertFalse(activity.isDeepLinkValid(Uri.parse("wss://knowgod.com/en/kgp")))
        assertFalse(activity.isDeepLinkValid(Uri.parse("https://example.com/en/kgp")))
        assertFalse(activity.isDeepLinkValid(Uri.parse("https://knowgod.com/en")))
        assertTrue(activity.isDeepLinkValid(Uri.parse("http://knowgod.com/en/kgp")))
        assertTrue(activity.isDeepLinkValid(Uri.parse("https://knowgod.com/en/kgp")))
        assertTrue(activity.isDeepLinkValid(Uri.parse("https://www.knowgod.com/en/kgp")))
        assertTrue(activity.isDeepLinkValid(Uri.parse("https://knowgod.com/en/fourlaws")))
        assertTrue(activity.isDeepLinkValid(Uri.parse("https://knowgod.com/en/kgp/2")))
    }
}
