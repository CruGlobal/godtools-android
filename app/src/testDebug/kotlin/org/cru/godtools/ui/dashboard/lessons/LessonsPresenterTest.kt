package org.cru.godtools.ui.dashboard.lessons

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jeppeman.mockposable.mockk.everyComposable
import com.slack.circuit.test.FakeNavigator
import com.slack.circuit.test.test
import com.slack.circuitx.android.IntentScreen
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.Locale
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.ccci.gto.android.common.androidx.compose.ui.platform.AndroidUiDispatcherUtil
import org.ccci.gto.android.common.util.content.equalsIntent
import org.cru.godtools.analytics.model.OpenAnalyticsActionEvent
import org.cru.godtools.analytics.model.OpenAnalyticsActionEvent.Companion.ACTION_OPEN_LESSON
import org.cru.godtools.analytics.model.OpenAnalyticsActionEvent.Companion.SOURCE_LESSONS
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
import org.cru.godtools.ui.tools.ToolCard
import org.cru.godtools.ui.tools.ToolCardPresenter
import org.cru.godtools.ui.tools.customLocaleArg
import org.cru.godtools.ui.tools.eventSinkArg
import org.cru.godtools.ui.tools.toolArg
import org.cru.godtools.util.createToolIntent
import org.greenrobot.eventbus.EventBus
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
class LessonsPresenterTest {
    private val appLangFlow = MutableStateFlow(Locale.ENGLISH)
    private val lessonsFlow = MutableStateFlow(emptyList<Tool>())
    private val enLessonsFlow = MutableStateFlow(emptyList<Tool>())
    private val languagesFlow = MutableStateFlow(emptyList<Language>())
    private val translationsFlow = MutableStateFlow(emptyList<Translation>())

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val eventBus: EventBus = mockk(relaxUnitFun = true)
    private val languagesRepository: LanguagesRepository = mockk {
        every { findLanguageFlow(any()) } answers { flowOf(Language(firstArg())) }
        every { getLanguagesFlow() } returns languagesFlow
    }
    private val settings: Settings = mockk {
        every { appLanguageFlow } returns appLangFlow
    }
    private val toolCardPresenter: ToolCardPresenter = mockk {
        everyComposable { present(tool = any(), customLocale = any(), eventSink = any()) }.answers {
            ToolCard.State(
                toolCode = toolArg().code,
                translation = randomTranslation(languageCode = customLocaleArg()!!),
                eventSink = eventSinkArg()
            )
        }
    }
    private val toolsRepository: ToolsRepository = mockk {
        every { getLessonsFlow() } returns lessonsFlow
        every { getLessonsFlowByLanguage(Locale.ENGLISH) } returns enLessonsFlow
    }
    private val translationsRepository: TranslationsRepository = mockk {
        every { getTranslationsFlowForTools(any()) } returns translationsFlow
    }

    private val navigator = FakeNavigator(LessonsScreen)

    private val presenter = LessonsPresenter(
        context = context,
        eventBus = eventBus,
        languagesRepository = languagesRepository,
        settings = settings,
        toolCardPresenter = toolCardPresenter,
        toolsRepository = toolsRepository,
        translationsRepository = translationsRepository,
        navigator = navigator,
    )

    @AfterTest
    fun cleanup() {
        AndroidUiDispatcherUtil.runScheduledDispatches()

        navigator.assertGoToIsEmpty()
        navigator.assertPopIsEmpty()
        navigator.assertResetRootIsEmpty()
    }

    // region State.languageFilter.selectedItem
    @Test
    fun `State - languageFilter - selectedItem - default to app language`() = runTest {
        presenter.test {
            assertEquals(appLangFlow.value, expectMostRecentItem().languageFilter.selectedItem?.code)
        }
    }
    // endregion State.languageFilter.selectedItem

    // region State.languageFilter.items
    @Test
    fun `State - languageFilter - items`() = runTest {
        lessonsFlow.value = listOf(randomTool("lesson"))
        languagesFlow.value = listOf(
            Language(Locale.ENGLISH),
            Language(Locale.FRENCH)
        )
        translationsFlow.value = listOf(
            randomTranslation("lesson", languageCode = Locale.ENGLISH),
            randomTranslation("lesson", languageCode = Locale.FRENCH),
        )

        presenter.test {
            assertEquals(languagesFlow.value, expectMostRecentItem().languageFilter.items.map { it.item })
        }
    }

    @Test
    fun `State - languageFilter - items - Include languages with at least 1 translation`() = runTest {
        lessonsFlow.value = listOf(randomTool("lesson"))
        languagesFlow.value = listOf(
            Language(Locale.ENGLISH),
            Language(Locale.FRENCH)
        )
        translationsFlow.value = listOf(randomTranslation("lesson", languageCode = Locale.ENGLISH))

        presenter.test {
            assertEquals(listOf(Language(Locale.ENGLISH)), expectMostRecentItem().languageFilter.items.map { it.item })
        }
    }

    @Test
    fun `State - languageFilter - items - filtered by query`() = runTest {
        lessonsFlow.value = listOf(randomTool("lesson"))
        languagesFlow.value = listOf(
            Language(Locale.ENGLISH),
            Language(Locale.FRENCH)
        )
        translationsFlow.value = listOf(
            randomTranslation("lesson", languageCode = Locale.ENGLISH),
            randomTranslation("lesson", languageCode = Locale.FRENCH),
        )

        presenter.test {
            awaitItem().languageFilter.query.value = "english"

            assertEquals(listOf(Language(Locale.ENGLISH)), expectMostRecentItem().languageFilter.items.map { it.item })
        }
    }

    @Test
    fun `State - languageFilter - items - include count of lessons per language`() = runTest {
        lessonsFlow.value = listOf(randomTool("lesson"), randomTool("lesson2"))
        languagesFlow.value = listOf(
            Language(Locale.ENGLISH),
            Language(Locale.FRENCH)
        )
        translationsFlow.value = listOf(
            randomTranslation("lesson", languageCode = Locale.ENGLISH),
            randomTranslation("lesson2", languageCode = Locale.ENGLISH),
            randomTranslation("lesson", languageCode = Locale.FRENCH),

            // duplicates that should be ignored when counting
            randomTranslation("lesson", languageCode = Locale.ENGLISH),
            randomTranslation("lesson2", languageCode = Locale.ENGLISH),
            randomTranslation("lesson", languageCode = Locale.FRENCH),
        )

        presenter.test {
            assertEquals(
                listOf(
                    FilterMenu.UiState.Item(Language(Locale.ENGLISH), 2),
                    FilterMenu.UiState.Item(Language(Locale.FRENCH), 1)
                ),
                expectMostRecentItem().languageFilter.items
            )
        }
    }
    // endregion State.languageFilter.items

    // region State.languageFilter Event.SelectItem
    @Test
    fun `State - languageFilter - Event - SelectItem`() = runTest {
        every { toolsRepository.getLessonsFlowByLanguage(any()) } returns flowOf(emptyList())

        presenter.test {
            expectMostRecentItem().languageFilter
                .also { assertEquals(appLangFlow.value, it.selectedItem?.code) }
                .eventSink(FilterMenu.Event.SelectItem(Language(Locale.FRENCH)))

            assertEquals(Locale.FRENCH, expectMostRecentItem().languageFilter.selectedItem?.code)
        }
    }
    // endregion State.languageFilter Event.SelectItem

    // region State.lessons
    @Test
    fun `State - lessons`() = runTest {
        enLessonsFlow.value = listOf(
            randomTool("lesson1", isHidden = false, defaultOrder = 0),
            randomTool("lesson2", isHidden = false, defaultOrder = 1),
        )

        presenter.test {
            assertEquals(listOf("lesson1", "lesson2"), expectMostRecentItem().lessons.map { it.toolCode })
        }
    }

    @Test
    fun `State - lessons - hide hidden lessons`() = runTest {
        enLessonsFlow.value = listOf(
            randomTool("lesson1", isHidden = false, defaultOrder = 0),
            randomTool("lesson2", isHidden = true, defaultOrder = 1),
            randomTool("lesson3", isHidden = false, defaultOrder = 2),
        )

        presenter.test {
            assertEquals(listOf("lesson1", "lesson3"), expectMostRecentItem().lessons.map { it.toolCode })
        }
    }

    @Test
    fun `State - lessons - sorted by defaultOrder`() = runTest {
        enLessonsFlow.value = listOf(
            randomTool("lesson2", isHidden = false, defaultOrder = 1),
            randomTool("lesson1", isHidden = false, defaultOrder = 0),
        )

        presenter.test {
            assertEquals(listOf("lesson1", "lesson2"), expectMostRecentItem().lessons.map { it.toolCode })
        }
    }

    @Test
    fun `State - lessons - Filtered by selected language`() = runTest {
        every { toolsRepository.getLessonsFlowByLanguage(Locale.FRENCH) }
            .returns(flowOf(listOf(randomTool("lesson", isHidden = false))))

        presenter.test {
            with(expectMostRecentItem()) {
                assertEquals(emptyList(), lessons)
                verify(exactly = 0) { toolsRepository.getLessonsFlowByLanguage(Locale.FRENCH) }

                languageFilter.eventSink(FilterMenu.Event.SelectItem(Language(Locale.FRENCH)))
            }

            assertEquals(listOf("lesson"), expectMostRecentItem().lessons.map { it.toolCode })
            verify { toolsRepository.getLessonsFlowByLanguage(Locale.FRENCH) }
        }
    }

    @Test
    fun `State - lessons - Event - Click`() = runTest {
        enLessonsFlow.value = listOf(
            randomTool("lesson1", isHidden = false, defaultOrder = 0),
            randomTool("lesson2", type = Tool.Type.LESSON, isHidden = false, defaultOrder = 1),
        )

        presenter.test {
            expectMostRecentItem().lessons[1].eventSink(ToolCard.Event.Click)

            val expectedIntent = enLessonsFlow.value[1].createToolIntent(
                context,
                languages = listOf(Locale.ENGLISH),
                resumeProgress = true
            )
            assertTrue(assertIs<IntentScreen>(navigator.awaitNextScreen()).intent equalsIntent expectedIntent)
        }
        verify { eventBus.post(OpenAnalyticsActionEvent(ACTION_OPEN_LESSON, "lesson2", SOURCE_LESSONS)) }
    }
    // endregion State.lessons
}
