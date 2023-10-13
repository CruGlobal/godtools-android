package org.cru.godtools.base

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlin.test.BeforeTest
import kotlin.test.Test
import org.cru.godtools.base.Settings.Companion.FEATURE_TUTORIAL_ONBOARDING
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.runner.RunWith

private const val FEATURE_TEST = "testFeature"

@RunWith(AndroidJUnit4::class)
class SettingsTest {
    private lateinit var settings: Settings

    @BeforeTest
    fun setup() {
        settings = Settings(ApplicationProvider.getApplicationContext())
    }

    @Test
    fun verifyFeatureDiscovery() {
        assertFalse(settings.isFeatureDiscovered(FEATURE_TEST))
        assertEquals(0, settings.getFeatureDiscoveredCount(FEATURE_TEST))
        settings.setFeatureDiscovered(FEATURE_TEST)
        assertTrue(settings.isFeatureDiscovered(FEATURE_TEST))
        assertEquals(1, settings.getFeatureDiscoveredCount(FEATURE_TEST))
        settings.setFeatureDiscovered(FEATURE_TEST)
        assertTrue(settings.isFeatureDiscovered(FEATURE_TEST))
        assertEquals(2, settings.getFeatureDiscoveredCount(FEATURE_TEST))
    }

    @Test
    fun verifyFeatureDiscoveryTutorialOnboardingNewInstall() {
        assertFalse(settings.isFeatureDiscovered(FEATURE_TUTORIAL_ONBOARDING))
    }
}
