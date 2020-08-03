package org.cru.godtools.base

import android.app.Activity
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import me.thekey.android.TheKey
import org.cru.godtools.base.Settings.Companion.FEATURE_LOGIN
import org.cru.godtools.base.Settings.Companion.FEATURE_TUTORIAL_ONBOARDING
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import java.util.UUID

private const val FEATURE_TEST = "testFeature"

@RunWith(AndroidJUnit4::class)
class SettingsTest {
    private lateinit var theKey: TheKey
    private lateinit var settings: Settings

    @Before
    fun setup() {
        val context = Robolectric.buildActivity(Activity::class.java).get()
        theKey = mock()
        settings = Settings(context, theKey)
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
        whenever(theKey.defaultSessionGuid).thenReturn(UUID.randomUUID().toString())
        assertTrue(settings.isFeatureDiscovered(FEATURE_LOGIN))
    }

    @Test
    fun verifyFeatureDiscoveryLoginNotLoggedIn() {
        assertFalse(settings.isFeatureDiscovered(FEATURE_LOGIN))
    }
}
