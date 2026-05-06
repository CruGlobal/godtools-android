package org.cru.godtools.ui.dashboard.lessons

import app.cash.turbine.test
import io.mockk.every
import io.mockk.mockk
import io.mockk.verifyAll
import java.util.Locale
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.cru.godtools.base.Settings
import org.cru.godtools.db.repository.ToolsRepository
import org.cru.godtools.model.Tool
import org.cru.godtools.model.randomTool
import org.cru.godtools.ui.dashboard.lessons.LessonsPresenter.UiState.Mode

@Suppress("UnusedFlow")
class LessonsFlowProducerTest {
    private val countryFlow = MutableStateFlow<String?>(null)

    private val settings: Settings = mockk {
        every { getPersonalizationCountryFlow() } returns countryFlow
    }
    private val toolsRepository: ToolsRepository = mockk {
        every { getLessonsFlowByLanguage(any()) } returns flowOf(emptyList())
        every { getPersonalizedLessonsFlow(any(), any()) } returns flowOf(emptyList())
    }

    private val producer = LessonsFlowProducer(settings = settings, toolsRepository = toolsRepository)

    // region ALL_LESSONS mode
    @Test
    fun `getFlow - All Lessons - uses getLessonsFlowByLanguage`() = runTest {
        producer.getFlow(mode = Mode.ALL_LESSONS, locale = Locale.ENGLISH).first()
        verifyAll {
            toolsRepository.getLessonsFlowByLanguage(Locale.ENGLISH)
        }
    }

    @Test
    fun `getFlow - All Lessons - excludes hidden lessons`() = runTest {
        val hidden = createLesson(isHidden = true)
        val visible = createLesson(isHidden = false)
        every { toolsRepository.getLessonsFlowByLanguage(any()) } returns flowOf(listOf(hidden, visible))

        assertEquals(listOf(visible), producer.getFlow(mode = Mode.ALL_LESSONS, locale = Locale.ENGLISH).first())
    }

    @Test
    fun `getFlow - All Lessons - sorted by defaultOrder`() = runTest {
        val lessons = List(5) { createLesson(defaultOrder = it) }
        every { toolsRepository.getLessonsFlowByLanguage(any()) } returns flowOf(lessons.shuffled())

        assertEquals(lessons, producer.getFlow(mode = Mode.ALL_LESSONS, locale = Locale.ENGLISH).first())
    }
    // endregion ALL_LESSONS mode

    // region PERSONALIZATION mode
    @Test
    fun `getFlow - Personalization - uses getPersonalizedLessonsFlow`() = runTest {
        countryFlow.value = "US"
        producer.getFlow(mode = Mode.PERSONALIZATION, locale = Locale.GERMAN).first()
        verifyAll {
            toolsRepository.getPersonalizedLessonsFlow(Locale.GERMAN, "US")
            toolsRepository.getPersonalizedLessonsFlow(Locale.GERMAN, null)
        }
    }

    @Test
    fun `getFlow - Personalization - returns personalized lessons when non-empty`() = runTest {
        val lesson = createLesson()
        val fallback = createLesson()
        countryFlow.value = "US"
        every { toolsRepository.getPersonalizedLessonsFlow(Locale.ENGLISH, "US") } returns flowOf(listOf(lesson))
        every { toolsRepository.getPersonalizedLessonsFlow(Locale.ENGLISH, null) } returns flowOf(listOf(fallback))

        assertEquals(listOf(lesson), producer.getFlow(mode = Mode.PERSONALIZATION, locale = Locale.ENGLISH).first())
    }

    @Test
    fun `getFlow - Personalization - falls back to language only when no country-specific lessons`() = runTest {
        val fallback = createLesson()
        countryFlow.value = "US"
        every { toolsRepository.getPersonalizedLessonsFlow(Locale.ENGLISH, "US") } returns flowOf(emptyList())
        every { toolsRepository.getPersonalizedLessonsFlow(Locale.ENGLISH, null) } returns flowOf(listOf(fallback))

        assertEquals(listOf(fallback), producer.getFlow(mode = Mode.PERSONALIZATION, locale = Locale.ENGLISH).first())
    }

    @Test
    fun `getFlow - Personalization - excludes hidden lessons`() = runTest {
        val hidden = createLesson(isHidden = true)
        val visible = createLesson(isHidden = false)
        every { toolsRepository.getPersonalizedLessonsFlow(any(), any()) } returns flowOf(listOf(hidden, visible))

        assertEquals(listOf(visible), producer.getFlow(mode = Mode.PERSONALIZATION, locale = Locale.ENGLISH).first())
    }

    @Test
    fun `getFlow - Personalization - updates when country changes`() = runTest {
        val usLesson = createLesson()
        every { toolsRepository.getPersonalizedLessonsFlow(Locale.ENGLISH, "US") } returns flowOf(listOf(usLesson))

        producer.getFlow(mode = Mode.PERSONALIZATION, locale = Locale.ENGLISH).test {
            assertEquals(emptyList(), awaitItem())

            countryFlow.value = "US"
            assertEquals(listOf(usLesson), awaitItem())
        }
    }
    // endregion PERSONALIZATION mode

    private fun createLesson(defaultOrder: Int = 0, isHidden: Boolean = false) =
        randomTool(type = Tool.Type.LESSON, defaultOrder = defaultOrder, isHidden = isHidden)
}
