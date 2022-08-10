package org.cru.godtools.base.tool.ui.controller

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.mockk.every
import io.mockk.mockk
import org.cru.godtools.base.tool.databinding.ToolContentAccordionBinding
import org.cru.godtools.tool.model.Accordion
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AccordionControllerTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var controller: AccordionController

    @Before
    fun setup() {
        controller = AccordionController(
            mockkToolContentAccordionBinding(),
            mockk(),
            mockk()
        )
    }

    @Test
    fun testIsActiveSection() {
        val activeSection = mockk<Accordion.Section> { every { id } returns "active" }
        val inactiveSection = mockk<Accordion.Section> { every { id } returns "inactive" }

        assertFalse(controller.isActiveSection(null))
        assertFalse(controller.isActiveSection(activeSection))
        assertFalse(controller.isActiveSection(inactiveSection))

        controller.activeSection.value = "active"
        assertFalse(controller.isActiveSection(null))
        assertTrue(controller.isActiveSection(activeSection))
        assertFalse(controller.isActiveSection(inactiveSection))
    }
}

internal fun mockkToolContentAccordionBinding() = mockk<ToolContentAccordionBinding> { every { root } returns mockk() }
