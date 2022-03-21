package org.cru.godtools.analytics.appsflyer

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.greenrobot.eventbus.EventBus
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.same
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@RunWith(AndroidJUnit4::class)
class AppsFlyerAnalyticsServiceTest {
    private lateinit var application: Application
    private lateinit var activity: Activity
    private lateinit var eventBus: EventBus
    private lateinit var deepLinkResolver: AppsFlyerDeepLinkResolver

    private lateinit var analyticsService: AppsFlyerAnalyticsService

    @Before
    fun setupMocks() {
        application = mock()
        activity = mock()
        eventBus = mock()
        deepLinkResolver = mock()

        analyticsService = AppsFlyerAnalyticsService(application, eventBus, setOf(deepLinkResolver), mock())
    }

    // region conversionListener.onAppOpenAttribution()
    @Test
    fun verifyOnAppOpenAttributionCallsResolverWithUri() {
        val uri = Uri.parse("https://example.com")
        val data = mapOf(AF_DP to uri.toString())

        analyticsService.conversionListener.onAppOpenAttribution(data)
        verify(deepLinkResolver).resolve(any(), eq(uri), same(data))
    }

    @Test
    fun verifyOnAppOpenAttributionCallsResolverWithoutUrl() {
        val data = emptyMap<String, String>()

        analyticsService.conversionListener.onAppOpenAttribution(data)
        verify(deepLinkResolver).resolve(any(), isNull(), same(data))
    }

    @Test
    fun verifyOnAppOpenAttributionOpensReturnedIntent() {
        val intent = mock<Intent>()
        analyticsService.onActivityResumed(activity)
        whenever(deepLinkResolver.resolve(any(), anyOrNull(), any())).thenReturn(intent)

        analyticsService.conversionListener.onAppOpenAttribution(emptyMap())
        verify(activity).startActivity(intent)
    }

    @Test
    fun verifyOnAppOpenAttributionDoesntOpenNullIntent() {
        analyticsService.onActivityResumed(activity)
        whenever(deepLinkResolver.resolve(any(), anyOrNull(), any())).thenReturn(null)

        analyticsService.conversionListener.onAppOpenAttribution(emptyMap())
        verify(activity, never()).startActivity(any())
    }
    // endregion conversionListener.onAppOpenAttribution()
}
