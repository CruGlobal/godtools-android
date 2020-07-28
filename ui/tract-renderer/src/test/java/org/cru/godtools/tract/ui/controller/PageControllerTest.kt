package org.cru.godtools.tract.ui.controller

import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.mock
import org.cru.godtools.base.Settings
import org.cru.godtools.tract.R
import org.cru.godtools.tract.databinding.TractPageBinding
import org.cru.godtools.xml.model.Card
import org.cru.godtools.xml.model.Manifest
import org.cru.godtools.xml.model.Page
import org.greenrobot.eventbus.EventBus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric

@RunWith(AndroidJUnit4::class)
class PageControllerTest {
    private lateinit var binding: TractPageBinding
    private lateinit var eventBus: EventBus
    private lateinit var settings: Settings
    private lateinit var controller: PageController

    @Before
    fun setup() {
        val activity = Robolectric.buildActivity(AppCompatActivity::class.java).get()
        val context = ContextThemeWrapper(activity, R.style.Theme_AppCompat)
        binding = TractPageBinding.inflate(LayoutInflater.from(context))
        eventBus = mock()
        settings = mock()
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
}
