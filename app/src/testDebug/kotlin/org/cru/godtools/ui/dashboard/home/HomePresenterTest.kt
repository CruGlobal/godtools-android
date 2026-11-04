package org.cru.godtools.ui.dashboard.home

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.test.FakeNavigator
import com.slack.circuit.test.test
import com.slack.circuitx.android.IntentScreen
import io.mockk.every
import io.mockk.mockk
import java.util.Locale
import kotlin.random.Random
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest
import org.ccci.gto.android.common.androidx.compose.ui.platform.AndroidUiDispatcherUtil
import org.ccci.gto.android.common.util.content.equalsIntent
import org.cru.godtools.base.CONFIG_UI_DASHBOARD_HOME_FAVORITE_TOOLS
import org.cru.godtools.base.ui.circuit.screen.dashboard.page.AllFavoritesScreen
import org.cru.godtools.base.ui.circuit.screen.dashboard.page.HomeScreen
import org.cru.godtools.base.ui.circuit.screen.dashboard.page.ToolsScreen
import org.cru.godtools.db.repository.ToolsRepository
import org.cru.godtools.model.Tool
import org.cru.godtools.model.randomTool
import org.cru.godtools.ui.banner.FakeBannerPresenter
import org.cru.godtools.ui.banner.tutorial.TutorialFeaturesBannerPresenter
import org.cru.godtools.ui.dashboard.home.HomePresenter.UiEvent
import org.cru.godtools.ui.tooldetails.ToolDetailsScreen
import org.cru.godtools.ui.tools.FakeToolCardPresenter
import org.cru.godtools.ui.tools.ToolCardPresenter.ToolCardEvent
import org.cru.godtools.util.createToolIntent
import org.greenrobot.eventbus.EventBus
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
class HomePresenterTest {
    private val toolsFlow = MutableSharedFlow<List<Tool>>(replay = 1)

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val eventBus: EventBus = mockk(relaxUnitFun = true)
    private val remoteConfig: FirebaseRemoteConfig = mockk {
        every { getLong(CONFIG_UI_DASHBOARD_HOME_FAVORITE_TOOLS) } returns 5
    }
    private val toolsRepository: ToolsRepository = mockk {
        every { getFavoriteToolsFlow() } returns toolsFlow
    }
    private val toolCardPresenter = FakeToolCardPresenter()
    private val tutorialFeaturesBannerPresenter = FakeBannerPresenter<TutorialFeaturesBannerPresenter.UiState>()

    private val navigator = FakeNavigator(HomeScreen)

    private val presenter = HomePresenter(
        context = context,
        eventBus = eventBus,
        remoteConfig = remoteConfig,
        toolCardPresenter = toolCardPresenter,
        toolsRepository = toolsRepository,
        tutorialFeaturesBannerPresenter = tutorialFeaturesBannerPresenter,
        navigator = navigator,
    )

    @AfterTest
    fun cleanup() {
        AndroidUiDispatcherUtil.runScheduledDispatches()

        navigator.assertGoToIsEmpty()
        navigator.assertPopIsEmpty()
        navigator.assertResetRootIsEmpty()
    }

    // region State.dataLoaded
    @Test
    fun `State - dataLoaded - Dependent on favoriteTools`() = runTest {
        presenter.test {
            assertFalse(expectMostRecentItem().dataLoaded)

            toolsFlow.emit(emptyList())
            assertTrue(expectMostRecentItem().dataLoaded)
        }
    }
    // endregion State.dataLoaded

    // region State.banner
    @Test
    fun `State - banner - Features Tutorial`() = runTest {
        val bannerState = TutorialFeaturesBannerPresenter.UiState()

        presenter.test {
            assertNull(expectMostRecentItem().banner)

            tutorialFeaturesBannerPresenter.updateState(bannerState)
            assertEquals(bannerState, awaitItem().banner)

            tutorialFeaturesBannerPresenter.updateState(null)
            assertNull(awaitItem().banner)
        }
    }
    // endregion State.banner

    // region State.favoriteTools
    @Test
    fun `State - favoriteTools`() = runTest {
        val tools = List(3) { randomTool(type = Tool.Type.TRACT, isHidden = false) }

        presenter.test {
            toolsFlow.emit(tools)
            assertEquals(tools.map { it.code }, expectMostRecentItem().favoriteTools.map { it.toolCode })
        }
    }

    @Test
    fun `State - favoriteTools - limit to configured number of tools`() = runTest {
        val tools = List(10) { randomTool(type = Tool.Type.TRACT, isHidden = false) }
        val limit = Random.nextLong(1, 10)
        every { remoteConfig.getLong(CONFIG_UI_DASHBOARD_HOME_FAVORITE_TOOLS) } returns limit

        presenter.test {
            toolsFlow.emit(tools)
            expectMostRecentItem().favoriteTools.let {
                assertEquals(limit.toInt(), it.size)
                assertEquals(tools.take(limit.toInt()).map { it.code }, it.map { it.toolCode })
            }
        }
    }

    @Test
    fun `State - favoriteTools - Event - Click`() = runTest {
        val tool = randomTool(type = Tool.Type.TRACT, primaryLocale = null, parallelLocale = null)

        presenter.test {
            toolsFlow.emit(listOf(tool))
            assertNotNull(expectMostRecentItem().favoriteTools[0]) { toolState ->
                toolState.eventSink(ToolCardEvent.Click)

                val expected = context.createToolIntent(
                    tool,
                    listOf(toolState.translation!!.languageCode),
                    saveLanguageSettings = true
                )
                assertTrue(assertIs<IntentScreen>(navigator.awaitNextScreen()).intent equalsIntent expected)
            }
        }
    }

    @Test
    fun `State - favoriteTools - Event - OpenTool`() = runTest {
        val tool = randomTool(type = Tool.Type.TRACT, primaryLocale = null, parallelLocale = null)

        presenter.test {
            toolsFlow.emit(listOf(tool))
            assertNotNull(expectMostRecentItem().favoriteTools[0]) { toolState ->
                toolState.eventSink(ToolCardEvent.OpenTool)

                val expected = context.createToolIntent(
                    tool,
                    listOf(toolState.translation!!.languageCode),
                    saveLanguageSettings = true
                )
                assertTrue(assertIs<IntentScreen>(navigator.awaitNextScreen()).intent equalsIntent expected)
            }
        }
    }

    @Test
    fun `State - favoriteTools - Event - OpenTool - Saved Languages`() = runTest {
        val tool = randomTool(type = Tool.Type.TRACT, primaryLocale = Locale.GERMAN, parallelLocale = Locale.FRENCH)

        presenter.test {
            toolsFlow.emit(listOf(tool))
            assertNotNull(expectMostRecentItem().favoriteTools[0]) { toolState ->
                toolState.eventSink(ToolCardEvent.OpenTool)

                val expected = context.createToolIntent(
                    tool,
                    listOf(Locale.GERMAN, Locale.FRENCH),
                    saveLanguageSettings = true
                )
                assertTrue(assertIs<IntentScreen>(navigator.awaitNextScreen()).intent equalsIntent expected)
            }
        }
    }

    @Test
    fun `State - favoriteTools - Event - OpenToolDetails`() = runTest {
        val tool = randomTool(type = Tool.Type.TRACT)

        presenter.test {
            toolsFlow.emit(listOf(tool))
            expectMostRecentItem().favoriteTools[0].eventSink(ToolCardEvent.OpenToolDetails)

            assertEquals(ToolDetailsScreen(tool.code!!), navigator.awaitNextScreen())
        }
    }
    // endregion State.favoriteTools

    @Test
    fun `Event - ViewAllFavorites`() = runTest {
        presenter.test {
            awaitItem().eventSink(UiEvent.ViewAllFavorites)

            assertEquals(AllFavoritesScreen, navigator.awaitNextScreen())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Event - ViewAllTools`() = runTest {
        presenter.test {
            awaitItem().eventSink(UiEvent.ViewAllTools)

            navigator.awaitResetRoot().let {
                assertEquals(ToolsScreen, it.newRoot)
                assertEquals(Navigator.StateOptions.SaveAndRestore, it.options)
            }

            cancelAndIgnoreRemainingEvents()
        }
    }
}
