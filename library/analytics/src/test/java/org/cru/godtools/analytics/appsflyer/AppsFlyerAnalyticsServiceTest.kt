package org.cru.godtools.analytics.appsflyer

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.appsflyer.AppsFlyerLibCore
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.isNull
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.same
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.greenrobot.eventbus.EventBus
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppsFlyerAnalyticsServiceTest {
    private lateinit var application: Application
    private lateinit var activity: Activity
    private lateinit var eventBus: EventBus
    private lateinit var deepLinkResolver: AppsFlyerDeepLinkResolver

    private lateinit var analyticsService: AppsFlyerAnalyticsService

    @Before
    fun setupMocks() {
        AppsFlyerLibCore.instance = mock()
        application = mock()
        activity = mock()
        eventBus = mock()
        deepLinkResolver = mock()

        analyticsService = AppsFlyerAnalyticsService(application, eventBus, setOf(deepLinkResolver))
    }

    // region onAppOpenAttribution()
    @Test
    fun verifyOnAppOpenAttributionCallsResolverWithUri() {
        val uri = Uri.parse("https://example.com")
        val data = mapOf(AF_DP to uri.toString())

        analyticsService.onAppOpenAttribution(data)
        verify(deepLinkResolver).resolve(any(), eq(uri), same(data))
    }

    @Test
    fun verifyOnAppOpenAttributionCallsResolverWithoutUrl() {
        val data = emptyMap<String, String>()

        analyticsService.onAppOpenAttribution(data)
        verify(deepLinkResolver).resolve(any(), isNull(), same(data))
    }

    @Test
    fun verifyOnAppOpenAttributionOpensReturnedIntent() {
        val intent = mock<Intent>()
        analyticsService.onActivityResumed(activity)
        whenever(deepLinkResolver.resolve(any(), anyOrNull(), any())).thenReturn(intent)

        analyticsService.onAppOpenAttribution(emptyMap())
        verify(activity).startActivity(intent)
    }

    @Test
    fun verifyOnAppOpenAttributionDoesntOpenNullIntent() {
        analyticsService.onActivityResumed(activity)
        whenever(deepLinkResolver.resolve(any(), anyOrNull(), any())).thenReturn(null)

        analyticsService.onAppOpenAttribution(emptyMap())
        verify(activity, never()).startActivity(any())
    }
    // endregion onAppOpenAttribution()
}
