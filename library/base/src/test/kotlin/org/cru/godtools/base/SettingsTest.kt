package org.cru.godtools.base

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.okta.authfoundation.credential.Credential
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import org.cru.godtools.base.Settings.Companion.FEATURE_LOGIN
import org.cru.godtools.base.Settings.Companion.FEATURE_TUTORIAL_ONBOARDING
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

private const val FEATURE_TEST = "testFeature"

@RunWith(AndroidJUnit4::class)
class SettingsTest {
    private val credential = mockk<Credential>()
    private lateinit var settings: Settings

    @Before
    fun setup() {
        settings = Settings(
            context = ApplicationProvider.getApplicationContext(),
            oktaCredentials = { mockk { coEvery { defaultCredential() } returns credential } }
        )
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
        every { credential.token } returns mockk()
        assertTrue(settings.isFeatureDiscovered(FEATURE_LOGIN))
    }

    @Test
    fun verifyFeatureDiscoveryLoginNotLoggedIn() {
        every { credential.token } returns null
        assertFalse(settings.isFeatureDiscovered(FEATURE_LOGIN))
    }
}
