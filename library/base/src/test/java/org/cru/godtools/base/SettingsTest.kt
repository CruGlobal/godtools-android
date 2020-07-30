package org.cru.godtools.base

import android.app.Activity
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric

private const val FEATURE_TEST = "testFeature"

@RunWith(AndroidJUnit4::class)
class SettingsTest {
    private lateinit var settings: Settings

    @Before
    fun setup() {
        val context = Robolectric.buildActivity(Activity::class.java).get()
        settings = Settings(context);
    }

    @Test
    fun verifyFeatureDiscovery() {
        assertFalse(settings.isFeatureDiscovered(FEATURE_TEST))
        settings.setFeatureDiscovered(FEATURE_TEST)
        assertTrue(settings.isFeatureDiscovered(FEATURE_TEST))
        settings.setFeatureDiscovered(FEATURE_TEST)
        assertTrue(settings.isFeatureDiscovered(FEATURE_TEST))
    }
}
