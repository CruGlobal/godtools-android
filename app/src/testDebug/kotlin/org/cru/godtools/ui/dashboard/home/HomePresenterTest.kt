package org.cru.godtools.ui.dashboard.home

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
import java.util.Locale
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.ccci.gto.android.common.androidx.compose.ui.platform.AndroidUiDispatcherUtil
import org.ccci.gto.android.common.util.content.equalsIntent
import org.cru.godtools.base.Settings
import org.cru.godtools.base.Settings.Companion.FEATURE_TUTORIAL_FEATURES
import org.cru.godtools.db.repository.ToolsRepository
import org.cru.godtools.model.Tool
import org.cru.godtools.model.randomTool
import org.cru.godtools.model.randomTranslation
import org.cru.godtools.ui.banner.BannerType
import org.cru.godtools.ui.dashboard.home.HomeScreen.UiEvent
import org.cru.godtools.ui.dashboard.tools.ToolsScreen
import org.cru.godtools.ui.tooldetails.ToolDetailsScreen
import org.cru.godtools.ui.tools.ToolCard
import org.cru.godtools.ui.tools.ToolCardPresenter
import org.cru.godtools.util.createToolIntent
import org.greenrobot.eventbus.EventBus
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
class HomePresenterTest {
    private val appLanguageFlow = MutableStateFlow(Locale.ENGLISH)
    private val lessonsFlow = MutableStateFlow(emptyList<Tool>())
    private val toolsFlow = MutableSharedFlow<List<Tool>>(replay = 1)

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val eventBus: EventBus = mockk(relaxUnitFun = true)
    private val settings: Settings = mockk {
        every { appLanguageFlow } returns this@HomePresenterTest.appLanguageFlow
        every { isFeatureDiscoveredFlow(any()) } returns flowOf(true)
    }
    private val toolsRepository: ToolsRepository = mockk {
        every { getLessonsFlow() } returns lessonsFlow
        every { getFavoriteToolsFlow() } returns toolsFlow
    }
    private val toolCardPresenter: ToolCardPresenter = mockk {
        everyComposable { present(tool = any(), eventSink = any()) }.answers {
            ToolCard.State(toolCode = firstArg<Tool>().code, eventSink = arg(4))
        }
    }

    private val navigator = FakeNavigator(HomeScreen)

    private val presenter = HomePresenter(
        context = context,
        eventBus = eventBus,
        settings = settings,
        toolCardPresenter = toolCardPresenter,
        toolsRepository = toolsRepository,
        navigator = navigator,
    )

    @AfterTest
    fun cleanup() {
        AndroidUiDispatcherUtil.runScheduledDispatches()

        navigator.assertGoToIsEmpty()
        navigator.assertPopIsEmpty()
        navigator.assertResetRootIsEmpty()
    }

    // region State.banner
    @Test
    fun `State - banner - Features Tutorial`() = runTest {
        val featuresTutorialDiscovered = MutableStateFlow(true)
        every { settings.isFeatureDiscoveredFlow(FEATURE_TUTORIAL_FEATURES) } returns featuresTutorialDiscovered

        presenter.test {
            assertNull(expectMostRecentItem().banner)

            featuresTutorialDiscovered.value = false
            assertEquals(BannerType.TUTORIAL_FEATURES, awaitItem().banner)

            featuresTutorialDiscovered.value = true
            assertNull(awaitItem().banner)
        }
    }

    @Test
    fun `State - banner - Features Tutorial - Only visible for supported languages`() = runTest {
        every { settings.isFeatureDiscoveredFlow(FEATURE_TUTORIAL_FEATURES) } returns flowOf(false)

        presenter.test {
            assertEquals(BannerType.TUTORIAL_FEATURES, expectMostRecentItem().banner)

            appLanguageFlow.value = Locale.forLanguageTag("x-test")
            assertNull(awaitItem().banner)

            appLanguageFlow.value = Locale.ENGLISH
            assertEquals(BannerType.TUTORIAL_FEATURES, expectMostRecentItem().banner)
        }
    }
    // endregion State.banner

    // region State.spotlightLessons
    @Test
    fun `State - spotlightLessons`() = runTest {
        lessonsFlow.value = emptyList()

        presenter.test {
            assertEquals(emptyList(), expectMostRecentItem().spotlightLessons)

            lessonsFlow.value = List(3) { randomTool(type = Tool.Type.LESSON, isHidden = false, isSpotlight = true) }
            assertEquals(lessonsFlow.value.map { it.code }, awaitItem().spotlightLessons.map { it.toolCode })

            lessonsFlow.value = emptyList()
            assertEquals(emptyList(), awaitItem().spotlightLessons)
        }
    }

    @Test
    fun `State - spotlightLessons - Only Spotlight Lessons`() = runTest {
        lessonsFlow.value = listOf(
            randomTool(code = "valid", type = Tool.Type.LESSON, isHidden = false, isSpotlight = true),
            randomTool(code = "invalid", type = Tool.Type.LESSON, isHidden = false, isSpotlight = false),
        )

        presenter.test {
            assertEquals(listOf("valid"), expectMostRecentItem().spotlightLessons.map { it.toolCode })
        }
    }

    @Test
    fun `State - spotlightLessons - Exclude hidden Lessons`() = runTest {
        lessonsFlow.value = listOf(
            randomTool(code = "valid", type = Tool.Type.LESSON, isHidden = false, isSpotlight = true),
            randomTool(code = "invalid", type = Tool.Type.LESSON, isHidden = true, isSpotlight = true),
        )

        presenter.test {
            assertEquals(listOf("valid"), expectMostRecentItem().spotlightLessons.map { it.toolCode })
        }
    }

    @Test
    fun `State - spotlightLessons - Event - Click`() = runTest {
        val lesson = randomTool(type = Tool.Type.LESSON, isHidden = false, isSpotlight = true)
        val translation = randomTranslation(lesson.code, languageCode = Locale.FRENCH)
        everyComposable { toolCardPresenter.present(tool = lesson, eventSink = any()) }.answers {
            ToolCard.State(toolCode = lesson.code, translation = translation, eventSink = arg(4))
        }
        lessonsFlow.value = listOf(lesson)

        presenter.test {
            expectMostRecentItem().spotlightLessons[0].eventSink(ToolCard.Event.Click)

            assertIs<IntentScreen>(navigator.awaitNextScreen()).let {
                val expected = lesson.createToolIntent(context, listOf(translation.languageCode))
                assertTrue(expected equalsIntent it.intent)
            }
        }
    }
    // endregion State.spotlightLessons

    // region State.favoriteTools
    private val favoriteTool = randomTool(type = Tool.Type.TRACT, isHidden = false)
    private val favoriteToolTranslation = randomTranslation(favoriteTool.code, languageCode = Locale.FRENCH)
    init {
        everyComposable { toolCardPresenter.present(tool = favoriteTool, eventSink = any()) }.answers {
            ToolCard.State(toolCode = favoriteTool.code, translation = favoriteToolTranslation, eventSink = arg(4))
        }
    }

    @Test
    fun `State - favoriteTools`() = runTest {
        val tools = List(3) { randomTool(type = Tool.Type.TRACT, isHidden = false) }

        presenter.test {
            toolsFlow.emit(tools)
            assertEquals(tools.map { it.code }, expectMostRecentItem().favoriteTools.map { it.toolCode })
        }
    }

    @Test
    fun `State - favoriteTools - limit to 5 tools`() = runTest {
        val tools = List(10) { randomTool(type = Tool.Type.TRACT, isHidden = false) }

        presenter.test {
            toolsFlow.emit(tools)
            expectMostRecentItem().favoriteTools.let {
                assertEquals(5, it.size)
                assertEquals(tools.take(5).map { it.code }, it.map { it.toolCode })
            }
        }
    }

    @Test
    fun `State - favoriteTools - Event - Click`() = runTest {
        presenter.test {
            toolsFlow.emit(listOf(favoriteTool))
            expectMostRecentItem().favoriteTools[0].eventSink(ToolCard.Event.Click)

            assertIs<IntentScreen>(navigator.awaitNextScreen()).let {
                val expected = favoriteTool.createToolIntent(context, listOf(favoriteToolTranslation.languageCode))
                assertTrue(expected equalsIntent it.intent)
            }
        }
    }

    @Test
    fun `State - favoriteTools - Event - OpenTool`() = runTest {
        presenter.test {
            toolsFlow.emit(listOf(favoriteTool))
            expectMostRecentItem().favoriteTools[0].eventSink(ToolCard.Event.OpenTool)

            assertIs<IntentScreen>(navigator.awaitNextScreen()).let {
                val expected = favoriteTool.createToolIntent(context, listOf(favoriteToolTranslation.languageCode))
                assertTrue(expected equalsIntent it.intent)
            }
        }
    }

    @Test
    fun `State - favoriteTools - Event - OpenToolDetails`() = runTest {
        presenter.test {
            toolsFlow.emit(listOf(favoriteTool))
            expectMostRecentItem().favoriteTools[0].eventSink(ToolCard.Event.OpenToolDetails)

            assertEquals(ToolDetailsScreen(favoriteTool.code!!), navigator.awaitNextScreen())
        }
    }
    // endregion State.favoriteTools

    @Test
    fun `State - favoriteToolsLoaded`() = runTest {
        presenter.test {
            assertFalse(expectMostRecentItem().favoriteToolsLoaded)

            toolsFlow.emit(emptyList())
            assertTrue(expectMostRecentItem().favoriteToolsLoaded)
        }
    }

    @Test
    fun `Event - ViewAllFavorites`() = runTest {
        presenter.test {
            awaitItem().eventSink(UiEvent.ViewAllFavorites)

            assertEquals(AllFavoritesScreen, navigator.awaitNextScreen())
        }
    }

    @Test
    fun `Event - ViewAllTools`() = runTest {
        presenter.test {
            awaitItem().eventSink(UiEvent.ViewAllTools)

            navigator.awaitResetRoot().let {
                assertEquals(ToolsScreen, it.newRoot)
                assertTrue(it.saveState)
                assertTrue(it.restoreState)
            }
        }
    }
}
