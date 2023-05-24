package org.cru.godtools.base

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import java.util.Locale
import kotlinx.coroutines.test.runTest
import org.cru.godtools.base.Settings.Companion.FEATURE_TUTORIAL_ONBOARDING
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

private const val FEATURE_TEST = "testFeature"

@RunWith(AndroidJUnit4::class)
class SettingsTest {
    private lateinit var settings: Settings

    @Before
    fun setup() {
        settings = Settings(ApplicationProvider.getApplicationContext())
    }

    // region primaryLanguage
    @Test
    fun `Property primaryLanguage`() {
        assertFalse(settings.isPrimaryLanguageSet)

        settings.primaryLanguage = Locale.FRENCH
        assertEquals(Locale.FRENCH, settings.primaryLanguage)
        assertTrue(settings.isPrimaryLanguageSet)

        settings.primaryLanguage = Locale.GERMAN
        assertEquals(Locale.GERMAN, settings.primaryLanguage)
        assertTrue(settings.isPrimaryLanguageSet)
    }

    @Test
    fun `Property primaryLanguage - Defaults to defaultLanguage`() {
        assertEquals(Settings.defaultLanguage, settings.primaryLanguage)
        assertFalse(settings.isPrimaryLanguageSet)
    }

    @Test
    fun `Property primaryLanguage - Clears parallelLanguage if its the same`() {
        settings.parallelLanguage = Locale.FRENCH
        assertEquals(Locale.FRENCH, settings.parallelLanguage)
        settings.primaryLanguage = Locale.FRENCH
        assertEquals(Locale.FRENCH, settings.primaryLanguage)
        assertNull(settings.parallelLanguage)
    }

    @Test
    fun `Property primaryLanguageFlow`() = runTest {
        settings.primaryLanguageFlow.test {
            assertEquals(Settings.defaultLanguage, awaitItem())

            settings.primaryLanguage = Locale.FRENCH
            assertEquals(Locale.FRENCH, awaitItem())

            settings.primaryLanguage = Locale.GERMAN
            assertEquals(Locale.GERMAN, awaitItem())
        }
    }
    // endregion primaryLanguage

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
