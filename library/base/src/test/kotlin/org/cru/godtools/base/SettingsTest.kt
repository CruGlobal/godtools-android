package org.cru.godtools.base

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.okta.oidc.clients.sessions.SessionClient
import io.mockk.every
import io.mockk.mockk
import org.cru.godtools.base.Settings.Companion.FEATURE_LOGIN
import org.cru.godtools.base.Settings.Companion.FEATURE_TUTORIAL_ONBOARDING
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith

private const val FEATURE_TEST = "testFeature"

@RunWith(AndroidJUnit4::class)
@Category(AndroidJUnit4::class)
class SettingsTest {
    private val sessionClient = mockk<SessionClient> { every { tokens } returns null }
    private lateinit var settings: Settings

    @Before
    fun setup() {
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
    fun verifyFeatureDiscoveryTutorialOnboardingNewInstall() {
        assertFalse(settings.isFeatureDiscovered(FEATURE_TUTORIAL_ONBOARDING))
    }

    @Test
    fun verifyFeatureDiscoveryLoginAlreadyLoggedIn() {
        // idToken: {}.{sub:"a"}.{}
        every { sessionClient.tokens } returns mockk {
            every { idToken } returns "e30.e3N1YjoiYSJ9.e30"
        }
        assertTrue(settings.isFeatureDiscovered(FEATURE_LOGIN))
    }

    @Test
    fun verifyFeatureDiscoveryLoginNotLoggedIn() {
        assertFalse(settings.isFeatureDiscovered(FEATURE_LOGIN))
    }
}
