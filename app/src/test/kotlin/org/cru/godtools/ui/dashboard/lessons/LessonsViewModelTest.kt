package org.cru.godtools.ui.dashboard.lessons

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import io.mockk.coVerify
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
import org.cru.godtools.model.randomTranslation
import org.cru.godtools.ui.dashboard.filters.FilterMenu
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains

@OptIn(ExperimentalCoroutinesApi::class)
class LessonsViewModelTest {
    private val lessonsFlow = MutableStateFlow(emptyList<Tool>())
    private val lessonsByLanguageFlow = MutableStateFlow(emptyList<Tool>())
    private val appLanguagesFlow = MutableStateFlow(Locale.ENGLISH)
    private val languagesFlow = MutableStateFlow(emptyList<Language>())
    private val languageFlow: MutableStateFlow<Language?> = MutableStateFlow(null)
    private val translationsFlow = MutableStateFlow(emptyList<Translation>())
    private var savedLessonLocale = MutableStateFlow(Locale.ENGLISH)

    private val toolsRepository: ToolsRepository = mockk {
        every { getLessonsFlow() } returns lessonsFlow
        every { getLessonsFlowByLanguage(any()) } returns lessonsByLanguageFlow
    }

    private val languagesRepository: LanguagesRepository = mockk {
        every { getLanguagesFlow() } returns languagesFlow
        every { findLanguageFlow(any()) } returns languageFlow
    }

    private val savedStateHandle: SavedStateHandle = SavedStateHandle()

    private val settings: Settings = mockk {
        every { appLanguage } returns Locale.ENGLISH
        every { appLanguageFlow } returns appLanguagesFlow
    }

    private val translationsRepository: TranslationsRepository = mockk {
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
            savedStateHandle = savedStateHandle,
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

        languageFlow.value = Language(Locale.ENGLISH)
        lessonsByLanguageFlow.value = listOf(visible, hidden)
        viewModel.lessons.test {
            runCurrent()
            assertThat(expectMostRecentItem(), contains("visible"))
        }
    }

    @Test
    fun `Property lessons - Sorted by defaultOrder`() = testScope.runTest {
        val first = randomTool("first", Tool.Type.LESSON, defaultOrder = 1, isHidden = false)
        val second = randomTool("second", Tool.Type.LESSON, defaultOrder = 2, isHidden = false)

        languageFlow.value = Language(Locale.ENGLISH)
        lessonsByLanguageFlow.value = listOf(second, first)
        viewModel.lessons.test {
            runCurrent()
            coVerify { toolsRepository.getLessonsFlowByLanguage(any()) }
            assertEquals(listOf("first", "second"), expectMostRecentItem())
        }
    }

    @Test
    fun `Property lessons - Filter by language`() = testScope.runTest {
        val first = randomTool("first", Tool.Type.LESSON, defaultOrder = 1, isHidden = false)
        val second = randomTool("second", Tool.Type.LESSON, defaultOrder = 2, isHidden = false)

        languageFlow.value = Language(Locale.ENGLISH)
        savedLessonLocale.value = Locale.ENGLISH
        viewModel.updateSelectedLanguage(Language(Locale.ENGLISH))
        lessonsFlow.value = listOf(first, second)
        lessonsByLanguageFlow.value = listOf(first)
        appLanguagesFlow.value = Locale.ENGLISH
        languagesFlow.value = listOf(Language(Locale.ENGLISH))

        viewModel.lessons.test {
            runCurrent()
            coVerify { toolsRepository.getLessonsFlowByLanguage(Locale.ENGLISH) }
            assertEquals(listOf("first"), expectMostRecentItem())
        }
    }

    @Test
    fun `Property filteredLanguages - Filter languages`() = testScope.runTest {
        val first = Language(Locale.ENGLISH)
        val second = Language(Locale.FRENCH)
        val english = randomTranslation(toolCode = "lesson", languageCode = Locale.ENGLISH)
        val french = randomTranslation(toolCode = "lesson", languageCode = Locale.FRENCH)
        val lesson = randomTool("lesson", Tool.Type.LESSON, isHidden = false)

        translationsFlow.value = listOf(english, french)
        lessonsFlow.value = listOf(lesson)
        languagesFlow.value = listOf(first, second)

        viewModel.filteredLanguages.test {
            coVerify { translationsRepository.getTranslationsFlowForTools(setOf("lesson")) }
            assertEquals(listOf(FilterMenu.UiState.Item(first, 1), FilterMenu.UiState.Item(second, 1)), awaitItem())
        }
    }

    @Test
    fun `Property filteredLanguages - Query Filter languages`() = testScope.runTest {
        val first = Language(Locale.ENGLISH)
        val second = Language(Locale.FRENCH)
        val english = randomTranslation(toolCode = "lesson", languageCode = Locale.ENGLISH)
        val french = randomTranslation(toolCode = "lesson", languageCode = Locale.FRENCH)
        val lesson = randomTool("lesson", Tool.Type.LESSON, isHidden = false)

        translationsFlow.value = listOf(english, french)
        lessonsFlow.value = listOf(lesson)
        languagesFlow.value = listOf(first, second)

        viewModel.filteredLanguages.test {
            viewModel.query.value = "Eng"
            runCurrent()
            coVerify { languagesRepository.getLanguagesFlow() }
            assertEquals(listOf(FilterMenu.UiState.Item(first, 1)), expectMostRecentItem())
        }
    }

    @Test
    fun `Property selectedLanguage - Change with AppLanguage Change`() = testScope.runTest {
        appLanguagesFlow.value = Locale.ENGLISH

        viewModel.selectedLanguage.test {
            assertEquals(Locale.ENGLISH, expectMostRecentItem().code)
            appLanguagesFlow.value = Locale.FRENCH
            runCurrent()
            assertEquals(Locale.FRENCH, expectMostRecentItem().code)
        }
    }
}
