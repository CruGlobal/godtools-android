package org.cru.godtools.ui.dashboard

import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.net.Uri
import androidx.activity.viewModels
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import org.cru.godtools.BuildConfig.HOST_GODTOOLS_CUSTOM_URI
import org.cru.godtools.base.HOST_GODTOOLSAPP_COM
import org.cru.godtools.base.ui.createDashboardIntent
import org.cru.godtools.base.ui.dashboard.Page
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@Config(application = HiltTestApplication::class)
class DashboardActivityTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val context get() = ApplicationProvider.getApplicationContext<Context>()

    private fun <R> scenario(
        intent: Intent = context.createDashboardIntent(null),
        block: (ActivityScenario<DashboardActivity>) -> R
    ): R = ActivityScenario.launch<DashboardActivity>(intent).use(block)

    // region Intent Processing
    @Test
    fun `Intent Processing - Normal Launch`() {
        scenario(intent = context.createDashboardIntent(null)) {
            it.onActivity { assertEquals(Page.HOME, it.viewModel.currentPage.value) }
        }
    }

    @Test
    fun `Intent Processing - Normal Launch - Lessons`() {
        scenario(intent = context.createDashboardIntent(Page.LESSONS)) {
            it.onActivity { assertEquals(Page.LESSONS, it.viewModel.currentPage.value) }
        }
    }

    @Test
    fun `Intent Processing - Normal Launch - Tools`() {
        scenario(intent = context.createDashboardIntent(Page.ALL_TOOLS)) {
            it.onActivity { assertEquals(Page.ALL_TOOLS, it.viewModel.currentPage.value) }
        }
    }

    @Test
    fun `Intent Processing - Deep Link - Legacy Lessons`() {
        scenario(intent = Intent(ACTION_VIEW, Uri.parse("https://$HOST_GODTOOLSAPP_COM/lessons"))) {
            it.onActivity { assertEquals(Page.LESSONS, it.viewModel.currentPage.value) }
        }
    }

    @Test
    fun `Intent Processing - Deep Link - Custom Uri Scheme - Home`() {
        scenario(intent = Intent(ACTION_VIEW, Uri.parse("godtools://$HOST_GODTOOLS_CUSTOM_URI/dashboard/home"))) {
            it.onActivity { assertEquals(Page.HOME, it.viewModel.currentPage.value) }
        }
    }

    @Test
    fun `Intent Processing - Deep Link - Custom Uri Scheme - Lessons`() {
        scenario(intent = Intent(ACTION_VIEW, Uri.parse("godtools://$HOST_GODTOOLS_CUSTOM_URI/dashboard/lessons"))) {
            it.onActivity { assertEquals(Page.LESSONS, it.viewModel.currentPage.value) }
        }
    }

    @Test
    fun `Intent Processing - Deep Link - Custom Uri Scheme - Tools`() {
        scenario(intent = Intent(ACTION_VIEW, Uri.parse("godtools://$HOST_GODTOOLS_CUSTOM_URI/dashboard/tools"))) {
            it.onActivity { assertEquals(Page.ALL_TOOLS, it.viewModel.currentPage.value) }
        }
    }

    @Test
    fun `Intent Processing - Deep Link - godtoolsapp_com - Home`() {
        scenario(intent = Intent(ACTION_VIEW, Uri.parse("https://$HOST_GODTOOLSAPP_COM/deeplink/dashboard/home"))) {
            it.onActivity { assertEquals(Page.HOME, it.viewModel.currentPage.value) }
        }
    }

    @Test
    fun `Intent Processing - Deep Link - godtoolsapp_com - Lessons`() {
        scenario(intent = Intent(ACTION_VIEW, Uri.parse("https://$HOST_GODTOOLSAPP_COM/deeplink/dashboard/lessons"))) {
            it.onActivity { assertEquals(Page.LESSONS, it.viewModel.currentPage.value) }
        }
    }

    @Test
    fun `Intent Processing - Deep Link - godtoolsapp_com - Tools`() {
        scenario(intent = Intent(ACTION_VIEW, Uri.parse("https://$HOST_GODTOOLSAPP_COM/deeplink/dashboard/tools"))) {
            it.onActivity { assertEquals(Page.ALL_TOOLS, it.viewModel.currentPage.value) }
        }
    }
    // endregion Intent Processing

    private val DashboardActivity.viewModel get() = viewModels<DashboardViewModel>().value
}
