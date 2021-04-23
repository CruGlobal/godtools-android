package org.cru.godtools.tract.ui.controller

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.testing.TestLifecycleOwner
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import javax.inject.Inject
import org.cru.godtools.base.Settings
import org.cru.godtools.tract.R
import org.cru.godtools.tract.databinding.TractPageBinding
import org.cru.godtools.xml.model.Manifest
import org.cru.godtools.xml.model.tract.Card
import org.cru.godtools.xml.model.tract.TractPage
import org.greenrobot.eventbus.EventBus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.keynote.godtools.android.db.GodToolsDao
import org.robolectric.Robolectric
import org.robolectric.annotation.Config

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@Config(application = HiltTestApplication::class)
class PageControllerTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var binding: TractPageBinding
    @Inject
    lateinit var dao: GodToolsDao
    @Inject
    lateinit var eventBus: EventBus
    @Inject
    lateinit var settings: Settings
    @Inject
    lateinit var heroControllerFactory: HeroController.Factory
    @Inject
    lateinit var cardControllerFactory: CardController.Factory
    private val baseLifecycleOwner = TestLifecycleOwner(Lifecycle.State.RESUMED)

    private lateinit var controller: PageController

    @Before
    fun setup() {
        hiltRule.inject()
        val activity = Robolectric.buildActivity(TestActivity::class.java).create().get()
        binding = TractPageBinding.inflate(LayoutInflater.from(activity))
        controller = PageController(
            binding,
            baseLifecycleOwner,
            dao,
            eventBus,
            settings,
            heroControllerFactory,
            cardControllerFactory
        )
    }

    @Test
    fun verifyOnToggleCard() {
        controller.model = TractPage(Manifest(), cards = { listOf(Card(it)) })

        val cardController = controller.cardControllers.first()
        assertNull(binding.pageContentLayout.activeCard)
        controller.onToggleCard(cardController)
        assertEquals(cardController.root, binding.pageContentLayout.activeCard)
        controller.onToggleCard(cardController)
        assertNull(binding.pageContentLayout.activeCard)
    }

    @Test
    fun verifyUpdateChildrenLifecycles() {
        controller.lifecycleOwner!!.maxState = Lifecycle.State.RESUMED
        controller.model = TractPage(Manifest(), cards = { listOf(Card(it), Card(it)) })

        // initially hero is visible
        assertEquals(Lifecycle.State.RESUMED, controller.heroController.lifecycleOwner!!.lifecycle.currentState)
        assertEquals(Lifecycle.State.STARTED, controller.cardControllers[0].lifecycleOwner!!.lifecycle.currentState)
        assertEquals(Lifecycle.State.STARTED, controller.cardControllers[1].lifecycleOwner!!.lifecycle.currentState)

        // change to the first card
        binding.pageContentLayout.changeActiveCard(0, false)
        assertEquals(Lifecycle.State.STARTED, controller.heroController.lifecycleOwner!!.lifecycle.currentState)
        assertEquals(Lifecycle.State.RESUMED, controller.cardControllers[0].lifecycleOwner!!.lifecycle.currentState)
        assertEquals(Lifecycle.State.STARTED, controller.cardControllers[1].lifecycleOwner!!.lifecycle.currentState)

        // change to the second card
        binding.pageContentLayout.changeActiveCard(1, false)
        assertEquals(Lifecycle.State.STARTED, controller.heroController.lifecycleOwner!!.lifecycle.currentState)
        assertEquals(Lifecycle.State.STARTED, controller.cardControllers[0].lifecycleOwner!!.lifecycle.currentState)
        assertEquals(Lifecycle.State.RESUMED, controller.cardControllers[1].lifecycleOwner!!.lifecycle.currentState)

        // change to the hero
        binding.pageContentLayout.changeActiveCard(-1, false)
        assertEquals(Lifecycle.State.RESUMED, controller.heroController.lifecycleOwner!!.lifecycle.currentState)
        assertEquals(Lifecycle.State.STARTED, controller.cardControllers[0].lifecycleOwner!!.lifecycle.currentState)
        assertEquals(Lifecycle.State.STARTED, controller.cardControllers[1].lifecycleOwner!!.lifecycle.currentState)
    }

    @AndroidEntryPoint
    class TestActivity : AppCompatActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setTheme(R.style.Theme_AppCompat)
        }
    }
}
