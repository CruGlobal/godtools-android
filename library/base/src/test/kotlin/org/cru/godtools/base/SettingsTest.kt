package org.cru.godtools.base

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import java.util.Locale
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest
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

    // region Dashboard Settings
    @Test
    fun testDashboardFilterCategory() = runTest {
        settings.getDashboardFilterCategoryFlow().test {
            assertNull(awaitItem())

            settings.updateDashboardFilterCategory("test")
            assertEquals("test", awaitItem())

            settings.updateDashboardFilterCategory(null)
            assertNull(awaitItem())
        }
    }

    @Test
    fun testDashboardFilterLocale() = runTest {
        settings.getDashboardFilterLocaleFlow().test {
            assertNull(awaitItem())

            settings.updateDashboardFilterLocale(Locale.ENGLISH)
            assertEquals(Locale.ENGLISH, awaitItem())

            settings.updateDashboardFilterLocale(null)
            assertNull(awaitItem())
        }
    }
    // endregion Dashboard Settings
}
