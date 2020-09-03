package org.cru.godtools.tract.ui.controller

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import dagger.hilt.android.testing.UninstallModules
import javax.inject.Inject
import org.cru.godtools.analytics.AnalyticsModule
import org.cru.godtools.api.ApiModule
import org.cru.godtools.base.Settings
import org.cru.godtools.download.manager.DownloadManagerModule
import org.cru.godtools.sync.SyncModule
import org.cru.godtools.sync.task.SyncTaskModule
import org.cru.godtools.tract.R
import org.cru.godtools.tract.databinding.TractPageBinding
import org.cru.godtools.xml.model.Card
import org.cru.godtools.xml.model.Manifest
import org.cru.godtools.xml.model.Page
import org.greenrobot.eventbus.EventBus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config

@HiltAndroidTest
@UninstallModules(
    AnalyticsModule::class,
    ApiModule::class,
    DownloadManagerModule::class,
    SyncModule::class,
    SyncTaskModule::class
)
@RunWith(AndroidJUnit4::class)
@Config(application = HiltTestApplication::class)
class PageControllerTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private lateinit var binding: TractPageBinding
    @Inject
    lateinit var eventBus: EventBus
    @Inject
    lateinit var settings: Settings
    private lateinit var controller: PageController

    @Before
    fun setup() {
        hiltRule.inject()
        val activity = Robolectric.buildActivity(TestActivity::class.java).create().get()
        binding = TractPageBinding.inflate(LayoutInflater.from(activity))
        controller = PageController(binding, eventBus, settings)
    }

    @Test
    fun verifyOnToggleCard() {
        controller.model = Page(Manifest(), cards = { listOf(Card(it)) })

        val cardController = controller.cardControllers.first()
        assertNull(binding.pageContentLayout.activeCard)
        controller.onToggleCard(cardController)
        assertEquals(cardController.root, binding.pageContentLayout.activeCard)
        controller.onToggleCard(cardController)
        assertNull(binding.pageContentLayout.activeCard)
    }

    @AndroidEntryPoint
    class TestActivity : AppCompatActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setTheme(R.style.Theme_AppCompat)
        }
    }
}
