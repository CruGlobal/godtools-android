package org.cru.godtools.ui.dashboard.lessons

import android.app.Application
import android.content.Context
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.Turbine
import com.jeppeman.mockposable.mockk.everyComposable
import com.slack.circuit.backstack.SaveableBackStack
import com.slack.circuit.foundation.Circuit
import com.slack.circuit.foundation.CircuitCompositionLocals
import com.slack.circuit.foundation.NavigableCircuitContent
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
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
@OptIn(ExperimentalCoroutinesApi::class)
class LessonsPresenterTest {
    private val appLangFlow = MutableStateFlow(Locale.ENGLISH)
    private val lessonsFlow = MutableStateFlow(emptyList<Tool>())
    private val enLessonsFlow = MutableStateFlow(emptyList<Tool>())
    private val languagesFlow = MutableStateFlow(emptyList<Language>())
    private val translationsFlow = MutableStateFlow(emptyList<Translation>())

    private val testScope = TestScope()

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

        every { getLessonsFlowByLanguage(any()) } returns flowOf(emptyList())
        every { getLessonsFlowByLanguage(Locale.ENGLISH) } returns enLessonsFlow
    }
    private val translationsRepository: TranslationsRepository = mockk {
        every { getTranslationsFlowForTools(any()) } returns translationsFlow
    }

    private val backStack = SaveableBackStack(LessonsScreen)
    private val navigator = FakeNavigator(backStack)

    private val presenter = LessonsPresenter(
        context = context,
        eventBus = eventBus,
        languagesRepository = languagesRepository,
        settings = settings,
        toolCardPresenter = toolCardPresenter,
        toolsRepository = toolsRepository,
        translationsRepository = translationsRepository,
        ioDispatcher = UnconfinedTestDispatcher(testScope.testScheduler),
        navigator = navigator,
    )

    // region StateRestorationTester Support
    @get:Rule
    val composeTestRule = createComposeRule()

    private val stateRestorationTester = StateRestorationTester(composeTestRule)

    // This logic is based on the Sample AnsweringNavigatorTest in the circuit library.
    // see: https://github.com/slackhq/circuit/blob/main/circuit-foundation/src/jvmTest/kotlin/com/slack/circuit/foundation/AnsweringNavigatorTest.kt
    private fun testPresenterWithStateRestoration(): ReceiveTurbine<LessonsScreen.UiState> {
        val presenterState = Turbine<LessonsScreen.UiState>()

        val circuit = Circuit.Builder()
            .addPresenter<LessonsScreen, LessonsScreen.UiState> { s, n, _ -> presenter }
            .addUi<LessonsScreen, LessonsScreen.UiState> { state, _ -> SideEffect { presenterState.add(state) } }
            .build()

        stateRestorationTester.setContent {
            CircuitCompositionLocals(circuit) {
                NavigableCircuitContent(navigator, backStack = backStack)
            }
        }
        composeTestRule.waitForIdle()

        return presenterState
    }

    private suspend fun <T> ReceiveTurbine<T>.test(validate: suspend ReceiveTurbine<T>.() -> Unit) = validate()
    // endregion StateRestorationTester Support

    @AfterTest
    fun cleanup() {
        AndroidUiDispatcherUtil.runScheduledDispatches()

        navigator.assertGoToIsEmpty()
        navigator.assertPopIsEmpty()
        navigator.assertResetRootIsEmpty()
    }

    // region State.languageFilter.selectedItem
    @Test
    fun `State - languageFilter - selectedItem - default to app language`() = testScope.runTest {
        presenter.test {
            assertEquals(appLangFlow.value, expectMostRecentItem().languageFilter.selectedItem?.code)
        }
    }

    @Test
    fun `State - languageFilter - selectedItem - reset to app locale when app locale changes`() = testScope.runTest {
        presenter.test {
            assertNotNull(expectMostRecentItem().languageFilter) {
                assertEquals(Locale.ENGLISH, it.selectedItem?.code)
                it.eventSink(FilterMenu.Event.SelectItem(Language(Locale.FRENCH)))
            }
            assertEquals(Locale.FRENCH, expectMostRecentItem().languageFilter.selectedItem?.code)

            appLangFlow.value = Locale.GERMAN
            assertEquals(Locale.GERMAN, expectMostRecentItem().languageFilter.selectedItem?.code)
        }
    }

    @Test
    fun `State - languageFilter - selectedItem - persisted through state save & restore`() = testScope.runTest {
        testPresenterWithStateRestoration().test {
            assertNotNull(expectMostRecentItem().languageFilter) {
                assertEquals(Locale.ENGLISH, it.selectedItem?.code)
                it.eventSink(FilterMenu.Event.SelectItem(Language(Locale.FRENCH)))
            }
            composeTestRule.waitForIdle()
            assertEquals(Locale.FRENCH, expectMostRecentItem().languageFilter.selectedItem?.code)

            stateRestorationTester.emulateSavedInstanceStateRestore()
            assertEquals(Locale.FRENCH, awaitItem().languageFilter.selectedItem?.code)
        }
    }
    // endregion State.languageFilter.selectedItem

    // region State.languageFilter.items
    @Test
    fun `State - languageFilter - items`() = testScope.runTest {
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
    fun `State - languageFilter - items - Sorted by app language display name`() = testScope.runTest {
        lessonsFlow.value = listOf(randomTool("lesson"))
        languagesFlow.value = listOf(
            Language(Locale("es")),
            Language(Locale.FRENCH),
        )
        translationsFlow.value = listOf(
            randomTranslation("lesson", languageCode = Locale("es")),
            randomTranslation("lesson", languageCode = Locale.FRENCH),
        )

        presenter.test {
            assertEquals(
                listOf(Language(Locale.FRENCH), Language(Locale("es"))),
                expectMostRecentItem().languageFilter.items.map { it.item }
            )
        }
    }

    @Test
    fun `State - languageFilter - items - Include languages with at least 1 translation`() = testScope.runTest {
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
    fun `State - languageFilter - items - filtered by query`() = testScope.runTest {
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
    fun `State - languageFilter - items - include count of lessons per language`() = testScope.runTest {
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

    // region State.languageFilter.query
    @Test
    fun `State - languageFilter - query - persisted through state save & restore`() = testScope.runTest {
        testPresenterWithStateRestoration().test {
            expectMostRecentItem().languageFilter.query.value = "test"

            stateRestorationTester.emulateSavedInstanceStateRestore()
            assertEquals("test", expectMostRecentItem().languageFilter.query.value)
        }
    }
    // endregion State.languageFilter.query

    // region State.languageFilter Event.SelectItem
    @Test
    fun `State - languageFilter - Event - SelectItem`() = testScope.runTest {
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
    fun `State - lessons`() = testScope.runTest {
        enLessonsFlow.value = listOf(
            randomTool("lesson1", isHidden = false, defaultOrder = 0),
            randomTool("lesson2", isHidden = false, defaultOrder = 1),
        )

        presenter.test {
            assertEquals(listOf("lesson1", "lesson2"), expectMostRecentItem().lessons.map { it.toolCode })
        }
    }

    @Test
    fun `State - lessons - hide hidden lessons`() = testScope.runTest {
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
    fun `State - lessons - sorted by defaultOrder`() = testScope.runTest {
        enLessonsFlow.value = listOf(
            randomTool("lesson2", isHidden = false, defaultOrder = 1),
            randomTool("lesson1", isHidden = false, defaultOrder = 0),
        )

        presenter.test {
            assertEquals(listOf("lesson1", "lesson2"), expectMostRecentItem().lessons.map { it.toolCode })
        }
    }

    @Test
    fun `State - lessons - Filtered by selected language`() = testScope.runTest {
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
    fun `State - lessons - Event - Click`() = testScope.runTest {
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
