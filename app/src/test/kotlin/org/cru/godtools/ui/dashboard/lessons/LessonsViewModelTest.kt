package org.cru.godtools.ui.dashboard.lessons

import app.cash.turbine.test
import io.mockk.every
import io.mockk.mockk
import java.util.Locale
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.cru.godtools.base.Settings
import org.cru.godtools.db.repository.LanguagesRepository
import org.cru.godtools.db.repository.ToolsRepository
import org.cru.godtools.db.repository.TranslationsRepository
import org.cru.godtools.model.Language
import org.cru.godtools.model.Tool
import org.cru.godtools.model.Translation
import org.cru.godtools.model.randomTool
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains

@OptIn(ExperimentalCoroutinesApi::class)
class LessonsViewModelTest {
    private val lessonsFlow = MutableStateFlow(emptyList<Tool>())
    private val appLanguagesFlow = MutableStateFlow(Locale.ENGLISH)
    private val languageFlow = MutableStateFlow(emptyList<Language>())
    private val translationsFlow = MutableStateFlow(emptyList<Translation>())

    private val toolsRepository: ToolsRepository = mockk {
        every { getLessonsFlow() } returns lessonsFlow
        every { getLessonsFlowByLanguage(any()) } returns lessonsFlow
    }

    private val languagesRepository: LanguagesRepository = mockk {
        every { getLanguagesFlow() } returns languageFlow
    }

    private val settings: Settings = mockk {
        every { appLanguage } returns Locale.ENGLISH
        every { appLanguageFlow } returns appLanguagesFlow
    }

    private val translationsRepository: TranslationsRepository = mockk{
        every { getTranslationsFlowForTools(any()) } returns translationsFlow
    }
    private val testScope = TestScope()

    private lateinit var viewModel: LessonsViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher(testScope.testScheduler))
        viewModel = LessonsViewModel(
            context = mockk(),
            eventBus = mockk(),
            toolsRepository = toolsRepository,
            languagesRepository = languagesRepository,
            translationsRepository = translationsRepository,
            settings = settings
        )
    }

    @AfterTest
    fun cleanup() {
        Dispatchers.resetMain()
    }

    @Test
    fun `Property lessons - Filter hidden lessons`() = testScope.runTest {
        val visible = randomTool("visible", Tool.Type.LESSON, isHidden = false)
        val hidden = randomTool("hidden", Tool.Type.LESSON, isHidden = true)

        viewModel.lessons.test {
            lessonsFlow.value = listOf(visible, hidden)
            runCurrent()
            assertThat(expectMostRecentItem(), contains("visible"))
        }
    }

    @Test
    fun `Property lessons - Sorted by defaultOrder`() = testScope.runTest {
        val first = randomTool("first", Tool.Type.LESSON, defaultOrder = 1, isHidden = false)
        val second = randomTool("second", Tool.Type.LESSON, defaultOrder = 2, isHidden = false)

        viewModel.lessons.test {
            lessonsFlow.value = listOf(second, first)
            runCurrent()
            assertEquals(listOf("first", "second"), expectMostRecentItem())
        }
    }
}
