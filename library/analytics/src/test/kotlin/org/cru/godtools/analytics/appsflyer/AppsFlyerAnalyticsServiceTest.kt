package org.cru.godtools.analytics.appsflyer

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppsFlyerAnalyticsServiceTest {
    private val activity = mockk<Activity>(relaxUnitFun = true)
    private lateinit var deepLinkResolver: AppsFlyerDeepLinkResolver

    private lateinit var analyticsService: AppsFlyerAnalyticsService

    @Before
    fun setupMocks() {
        deepLinkResolver = mockk {
            every { resolve(any(), any(), any()) } returns null
        }

        analyticsService = AppsFlyerAnalyticsService(
            mockk(relaxUnitFun = true),
            eventBus = mockk(relaxUnitFun = true),
            deepLinkResolvers = setOf(deepLinkResolver),
            appsFlyer = mockk(relaxed = true)
        )
    }

    // region conversionListener.onAppOpenAttribution()
    @Test
    fun verifyOnAppOpenAttributionCallsResolverWithUri() {
        val uri = Uri.parse("https://example.com")
        val data = mapOf(AF_DP to uri.toString())

        analyticsService.conversionListener.onAppOpenAttribution(data)
        verify { deepLinkResolver.resolve(any(), uri, data) }
    }

    @Test
    fun verifyOnAppOpenAttributionCallsResolverWithoutUrl() {
        val data = emptyMap<String, String>()

        analyticsService.conversionListener.onAppOpenAttribution(data)
        verify { deepLinkResolver.resolve(any(), null, data) }
    }

    @Test
    fun verifyOnAppOpenAttributionOpensReturnedIntent() {
        val intent = mockk<Intent>()
        analyticsService.onActivityResumed(activity)
        every { deepLinkResolver.resolve(any(), any(), any()) } returns intent

        analyticsService.conversionListener.onAppOpenAttribution(emptyMap())
        verify { activity.startActivity(intent) }
        confirmVerified(activity)
    }

    @Test
    fun verifyOnAppOpenAttributionDoesntOpenNullIntent() {
        analyticsService.onActivityResumed(activity)
        every { deepLinkResolver.resolve(any(), any(), any()) } returns null

        analyticsService.conversionListener.onAppOpenAttribution(emptyMap())
        verify(inverse = true) { activity.startActivity(any()) }
        confirmVerified(activity)
    }
    // endregion conversionListener.onAppOpenAttribution()
}
