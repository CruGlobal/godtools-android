package org.cru.godtools.base.tool.ui.controller

import android.widget.LinearLayout
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.testing.TestLifecycleOwner
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.ccci.gto.android.common.util.getDeclaredFieldOrNull
import org.cru.godtools.base.tool.databinding.ToolContentAccordionSectionBinding
import org.cru.godtools.tool.model.Accordion
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AccordionSectionControllerTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val baseLifecycleOwner = TestLifecycleOwner()
    private val accordionController = AccordionController(
        mockkToolContentAccordionBinding(),
        mockk { every { lifecycleOwner } returns baseLifecycleOwner },
        mockk()
    )

    private lateinit var controller: AccordionController.SectionController

    @Before
    fun setup() {
        controller = AccordionController.SectionController(
            mockk<ToolContentAccordionSectionBinding>(relaxed = true) {
                every { root } returns mockk(relaxed = true)
                getDeclaredFieldOrNull<ToolContentAccordionSectionBinding>("content")
                    ?.set(this, mockk<LinearLayout>())
            },
            accordionController,
            mockk()
        )
    }

    @After
    fun cleanup() {
        Dispatchers.resetMain()
    }

    @Test
    fun testLifecycleState() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        baseLifecycleOwner.lifecycle.currentState = Lifecycle.State.RESUMED
        assertEquals(Lifecycle.State.CREATED, controller.lifecycleOwner!!.lifecycle.currentState)

        // active section
        controller.model = createSection("section1")
        assertEquals(Lifecycle.State.STARTED, controller.lifecycleOwner!!.lifecycle.currentState)
        accordionController.activeSection.value = "section1"
        assertEquals(Lifecycle.State.RESUMED, controller.lifecycleOwner!!.lifecycle.currentState)

        // stop & restart accordion
        baseLifecycleOwner.lifecycle.currentState = Lifecycle.State.CREATED
        assertEquals(Lifecycle.State.CREATED, controller.lifecycleOwner!!.lifecycle.currentState)
        baseLifecycleOwner.lifecycle.currentState = Lifecycle.State.RESUMED
        assertEquals(Lifecycle.State.RESUMED, controller.lifecycleOwner!!.lifecycle.currentState)

        // inactive section
        controller.model = createSection("section2")
        assertEquals(Lifecycle.State.STARTED, controller.lifecycleOwner!!.lifecycle.currentState)
        accordionController.activeSection.value = null
        assertEquals(Lifecycle.State.STARTED, controller.lifecycleOwner!!.lifecycle.currentState)

        // reset controller
        controller.model = null
        assertEquals(Lifecycle.State.CREATED, controller.lifecycleOwner!!.lifecycle.currentState)
    }

    private fun createSection(id: String): Accordion.Section = mockk(relaxed = true) {
        every { manifest } returns mockk(relaxed = true) { every { locale } returns null }
        every { this@mockk.id } returns id
    }
}
