package org.cru.godtools.tract.ui.controller

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.R
import androidx.appcompat.app.AppCompatActivity
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.testing.TestLifecycleOwner
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import io.mockk.mockk
import javax.inject.Inject
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.ccci.gto.android.common.androidx.lifecycle.ConstrainedStateLifecycleOwner
import org.ccci.gto.android.common.androidx.lifecycle.ImmutableLiveData
import org.cru.godtools.base.Settings
import org.cru.godtools.shared.renderer.state.State
import org.cru.godtools.shared.tool.parser.model.tract.TractPage
import org.cru.godtools.tool.tract.databinding.TractPageBinding
import org.greenrobot.eventbus.EventBus
import org.junit.Rule
import org.junit.runner.RunWith
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
    lateinit var eventBus: EventBus
    @Inject
    lateinit var settings: Settings
    @Inject
    lateinit var heroControllerFactory: HeroController.Factory
    @Inject
    lateinit var cardControllerFactory: CardController.Factory
    private val baseLifecycleOwner = TestLifecycleOwner(Lifecycle.State.RESUMED)

    private lateinit var controller: PageController

    @BeforeTest
    fun setup() {
        hiltRule.inject()
        val activity = Robolectric.buildActivity(TestActivity::class.java).create().get()
        binding = TractPageBinding.inflate(LayoutInflater.from(activity))
        controller = PageController(
            binding,
            ConstrainedStateLifecycleOwner(baseLifecycleOwner, Lifecycle.State.STARTED),
            ImmutableLiveData(false),
            State(),
            eventBus,
            settings,
            mockk(),
            heroControllerFactory,
            cardControllerFactory
        )
    }

    @Test
    fun verifyOnToggleCard() {
        controller.model = TractPage(cards = { listOf(TractPage.Card(it)) })

        val cardController = controller.cardControllers.first()
        assertNull(binding.pageContentLayout.activeCard)
        controller.onToggleCard(cardController)
        assertEquals(cardController.root, binding.pageContentLayout.activeCard)
        controller.onToggleCard(cardController)
        assertNull(binding.pageContentLayout.activeCard)
    }

    @Test
    fun verifyUpdateChildrenLifecycles() {
        controller.lifecycleOwner.maxState = Lifecycle.State.RESUMED
        controller.model = TractPage(cards = { listOf(TractPage.Card(it), TractPage.Card(it)) })

        // initially hero is visible
        assertEquals(Lifecycle.State.RESUMED, controller.heroController.lifecycleOwner.lifecycle.currentState)
        assertEquals(Lifecycle.State.STARTED, controller.cardControllers[0].lifecycleOwner.lifecycle.currentState)
        assertEquals(Lifecycle.State.STARTED, controller.cardControllers[1].lifecycleOwner.lifecycle.currentState)

        // change to the first card
        binding.pageContentLayout.changeActiveCard(0, false)
        assertEquals(Lifecycle.State.STARTED, controller.heroController.lifecycleOwner.lifecycle.currentState)
        assertEquals(Lifecycle.State.RESUMED, controller.cardControllers[0].lifecycleOwner.lifecycle.currentState)
        assertEquals(Lifecycle.State.STARTED, controller.cardControllers[1].lifecycleOwner.lifecycle.currentState)

        // change to the second card
        binding.pageContentLayout.changeActiveCard(1, false)
        assertEquals(Lifecycle.State.STARTED, controller.heroController.lifecycleOwner.lifecycle.currentState)
        assertEquals(Lifecycle.State.STARTED, controller.cardControllers[0].lifecycleOwner.lifecycle.currentState)
        assertEquals(Lifecycle.State.RESUMED, controller.cardControllers[1].lifecycleOwner.lifecycle.currentState)

        // change to the hero
        binding.pageContentLayout.changeActiveCard(-1, false)
        assertEquals(Lifecycle.State.RESUMED, controller.heroController.lifecycleOwner.lifecycle.currentState)
        assertEquals(Lifecycle.State.STARTED, controller.cardControllers[0].lifecycleOwner.lifecycle.currentState)
        assertEquals(Lifecycle.State.STARTED, controller.cardControllers[1].lifecycleOwner.lifecycle.currentState)
    }

    @AndroidEntryPoint
    class TestActivity : AppCompatActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setTheme(R.style.Theme_AppCompat)
        }
    }
}
