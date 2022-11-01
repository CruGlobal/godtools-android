package org.cru.godtools.base.tool.ui.controller

import android.widget.LinearLayout
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.testing.TestLifecycleOwner
import io.mockk.Called
import io.mockk.clearMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.ccci.gto.android.common.util.getDeclaredFieldOrNull
import org.cru.godtools.base.tool.analytics.model.ContentAnalyticsEventAnalyticsActionEvent
import org.cru.godtools.shared.tool.parser.model.Accordion
import org.cru.godtools.shared.tool.parser.model.AnalyticsEvent
import org.cru.godtools.tool.databinding.ToolContentAccordionSectionBinding
import org.greenrobot.eventbus.EventBus
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
    private val eventBus = mockk<EventBus>(relaxUnitFun = true)
    private val accordionController = AccordionController(
        mockkToolContentAccordionBinding(),
        mockk {
            every { eventBus } returns this@AccordionSectionControllerTest.eventBus
            every { lifecycleOwner } returns baseLifecycleOwner
        },
        mockk()
    )

    private lateinit var controller: AccordionController.SectionController

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
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

    @Test
    fun testVisibleAnalyticsEvents() = runTest {
        baseLifecycleOwner.lifecycle.currentState = Lifecycle.State.STARTED
        val event = AnalyticsEvent()
        val delayedEvent1 = AnalyticsEvent(delay = 1)
        val delayedEvent2 = AnalyticsEvent(delay = 2)
        val section = createSection("section")
        every { section.getAnalyticsEvents(any()) } returns listOf(event, delayedEvent1, delayedEvent2)
        controller.model = section
        accordionController.activeSection.value = "section"
        advanceUntilIdle()

        // no analytics events should have been triggered since we haven't been resumed yet
        verify(exactly = 0) { section.getAnalyticsEvents(AnalyticsEvent.Trigger.VISIBLE) }
        verify { eventBus wasNot Called }

        // trigger events by entering resumed state
        baseLifecycleOwner.lifecycle.currentState = Lifecycle.State.RESUMED
        verify(exactly = 1) { section.getAnalyticsEvents(AnalyticsEvent.Trigger.VISIBLE) }
        verifyAll {
            eventBus.post(match<ContentAnalyticsEventAnalyticsActionEvent> { it.event === event })
        }
        confirmVerified(eventBus)

        // check delayed event executes
        advanceTimeBy(1000)
        runCurrent()
        verifyAll {
            eventBus.post(match<ContentAnalyticsEventAnalyticsActionEvent> { it.event === event })
            eventBus.post(match<ContentAnalyticsEventAnalyticsActionEvent> { it.event === delayedEvent1 })
        }
        confirmVerified(eventBus)
        clearMocks(eventBus)

        // check cancelling delayedEvent2
        advanceTimeBy(999)
        baseLifecycleOwner.lifecycle.currentState = Lifecycle.State.STARTED
        advanceUntilIdle()
        verify { eventBus wasNot Called }
    }

    private fun createSection(id: String): Accordion.Section = mockk(relaxed = true) {
        every { manifest } returns mockk(relaxed = true) { every { locale } returns null }
        every { this@mockk.id } returns id
    }
}
