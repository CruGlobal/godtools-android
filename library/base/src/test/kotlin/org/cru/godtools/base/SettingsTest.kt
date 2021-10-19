package org.cru.godtools.base

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.okta.oidc.clients.sessions.SessionClient
import org.cru.godtools.base.Settings.Companion.FEATURE_LOGIN
import org.cru.godtools.base.Settings.Companion.FEATURE_TUTORIAL_ONBOARDING
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.RETURNS_DEEP_STUBS
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

private const val FEATURE_TEST = "testFeature"

@RunWith(AndroidJUnit4::class)
class SettingsTest {
    private lateinit var sessionClient: SessionClient
    private lateinit var settings: Settings

    @Before
    fun setup() {
        sessionClient = mock(defaultAnswer = RETURNS_DEEP_STUBS)
        settings = Settings(ApplicationProvider.getApplicationContext()) { sessionClient }
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
    fun verifyFeatureDiscoveryTutorialOnboardingUpgrade() {
        settings.firstLaunchVersion = 1
        assertTrue(settings.isFeatureDiscovered(FEATURE_TUTORIAL_ONBOARDING))
    }

    @Test
    fun verifyFeatureDiscoveryTutorialOnboardingNewInstall() {
        assertFalse(settings.isFeatureDiscovered(FEATURE_TUTORIAL_ONBOARDING))
    }

    @Test
    fun verifyFeatureDiscoveryLoginAlreadyLoggedIn() {
        // idToken: {}.{sub:"a"}.{}
        whenever(sessionClient.tokens.idToken).thenReturn("e30.e3N1YjoiYSJ9.e30")
        assertTrue(settings.isFeatureDiscovered(FEATURE_LOGIN))
    }

    @Test
    fun verifyFeatureDiscoveryLoginNotLoggedIn() {
        assertFalse(settings.isFeatureDiscovered(FEATURE_LOGIN))
    }
}
