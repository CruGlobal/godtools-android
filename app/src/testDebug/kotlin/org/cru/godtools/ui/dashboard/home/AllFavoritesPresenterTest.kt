package org.cru.godtools.ui.dashboard.home

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jeppeman.mockposable.mockk.everyComposable
import com.slack.circuit.test.FakeNavigator
import com.slack.circuit.test.test
import com.slack.circuitx.android.IntentScreen
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verifyAll
import java.util.Locale
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.ccci.gto.android.common.androidx.compose.ui.platform.AndroidUiDispatcherUtil
import org.ccci.gto.android.common.util.content.equalsIntent
import org.cru.godtools.analytics.model.OpenAnalyticsActionEvent
import org.cru.godtools.analytics.model.OpenAnalyticsActionEvent.Companion.ACTION_OPEN_TOOL
import org.cru.godtools.analytics.model.OpenAnalyticsActionEvent.Companion.ACTION_OPEN_TOOL_DETAILS
import org.cru.godtools.analytics.model.OpenAnalyticsActionEvent.Companion.SOURCE_FAVORITE
import org.cru.godtools.db.repository.ToolsRepository
import org.cru.godtools.model.Tool
import org.cru.godtools.model.randomTool
import org.cru.godtools.model.randomTranslation
import org.cru.godtools.ui.dashboard.home.AllFavoritesScreen.UiEvent
import org.cru.godtools.ui.tooldetails.ToolDetailsScreen
import org.cru.godtools.ui.tools.ToolCard
import org.cru.godtools.ui.tools.ToolCardPresenter
import org.cru.godtools.util.createToolIntent
import org.greenrobot.eventbus.EventBus
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
class AllFavoritesPresenterTest {
    private val toolsFlow = MutableStateFlow(emptyList<Tool>())

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val eventBus: EventBus = mockk(relaxUnitFun = true)
    private val toolsRepository: ToolsRepository = mockk {
        every { getFavoriteToolsFlow() } returns toolsFlow

        coEvery { storeToolOrder(any()) } just Runs
    }
    private val toolCardPresenter: ToolCardPresenter = mockk {
        everyComposable {
            present(tool = any(), eventSink = any())
        }.answers { ToolCard.State(toolCode = firstArg<Tool>().code, eventSink = arg(5)) }
    }

    private val navigator = FakeNavigator(AllFavoritesScreen)

    private val presenter = AllFavoritesPresenter(
        context = context,
        eventBus = eventBus,
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

    // region State.tools
    @Test
    fun `State - tools`() = runTest {
        toolsFlow.value = listOf(
            randomTool("tool1"),
            randomTool("tool2"),
        )

        presenter.test {
            assertEquals(listOf("tool1", "tool2"), expectMostRecentItem().tools.map { it.toolCode })
        }
    }

    @Test
    fun `State - tools - empty if no tools`() = runTest {
        toolsFlow.value = emptyList()
        presenter.test {
            assertEquals(emptyList(), expectMostRecentItem().tools)
        }
    }
    // endregion State.tools

    @Test
    fun `Event - MoveTool`() = runTest {
        toolsFlow.value = listOf(
            randomTool("tool1"),
            randomTool("tool2"),
            randomTool("tool3"),
            randomTool("tool4"),
        )

        presenter.test {
            expectMostRecentItem().let {
                assertEquals(listOf("tool1", "tool2", "tool3", "tool4"), it.tools.map { it.toolCode })
                it.eventSink(UiEvent.MoveTool(2, 1))
            }

            assertEquals(listOf("tool1", "tool3", "tool2", "tool4"), expectMostRecentItem().tools.map { it.toolCode })
        }
    }

    @Test
    fun `Event - CommitToolOrder`() = runTest {
        toolsFlow.value = listOf(
            randomTool("tool1"),
            randomTool("tool2"),
            randomTool("tool3"),
            randomTool("tool4"),
        )

        presenter.test {
            expectMostRecentItem().eventSink(UiEvent.MoveTool(2, 1))

            expectMostRecentItem().let {
                assertEquals(listOf("tool1", "tool3", "tool2", "tool4"), it.tools.map { it.toolCode })
                it.eventSink(UiEvent.CommitToolOrder)
            }
        }

        coVerify { toolsRepository.storeToolOrder(listOf("tool1", "tool3", "tool2", "tool4")) }
    }

    // region ToolCard.Event.Click
    @Test
    fun `ToolCard - Event - Click`() = runTest {
        val tool = randomTool("tool", Tool.Type.TRACT, primaryLocale = null, parallelLocale = null)
        toolsFlow.value = listOf(tool)
        everyComposable { toolCardPresenter.present(tool = tool, eventSink = any()) }.answers {
            ToolCard.State(
                toolCode = firstArg<Tool>().code,
                translation = randomTranslation(languageCode = Locale.ENGLISH),
                eventSink = arg(5)
            )
        }

        presenter.test {
            expectMostRecentItem().tools[0].eventSink(ToolCard.Event.Click)

            val expected = tool.createToolIntent(
                context,
                languages = listOf(Locale.ENGLISH),
                saveLanguageSettings = true
            )
            assertTrue(assertIs<IntentScreen>(navigator.awaitNextScreen()).intent equalsIntent expected)
        }

        verifyAll { eventBus.post(OpenAnalyticsActionEvent(ACTION_OPEN_TOOL, tool.code, SOURCE_FAVORITE)) }
    }

    @Test
    fun `ToolCard - Event - Click - Saved Languages`() = runTest {
        val tool = randomTool("tool", Tool.Type.TRACT, primaryLocale = Locale.FRENCH, parallelLocale = Locale.GERMAN)
        toolsFlow.value = listOf(tool)
        everyComposable { toolCardPresenter.present(tool = tool, eventSink = any()) }.answers {
            ToolCard.State(
                toolCode = firstArg<Tool>().code,
                translation = randomTranslation(languageCode = Locale.ENGLISH),
                eventSink = arg(5)
            )
        }

        presenter.test {
            expectMostRecentItem().tools[0].eventSink(ToolCard.Event.Click)

            val expected = tool.createToolIntent(
                context,
                languages = listOf(Locale.FRENCH, Locale.GERMAN),
                saveLanguageSettings = true
            )
            assertTrue(assertIs<IntentScreen>(navigator.awaitNextScreen()).intent equalsIntent expected)
        }

        verifyAll { eventBus.post(OpenAnalyticsActionEvent(ACTION_OPEN_TOOL, tool.code, SOURCE_FAVORITE)) }
    }
    // endregion ToolCard.Event.Click

    @Test
    fun `ToolCard - Event - OpenToolDetails`() = runTest {
        val tool = randomTool("tool", Tool.Type.TRACT)
        toolsFlow.value = listOf(tool)

        presenter.test {
            expectMostRecentItem().tools[0].eventSink(ToolCard.Event.OpenToolDetails)

            assertEquals(ToolDetailsScreen(tool.code!!), navigator.awaitNextScreen())
        }

        verifyAll { eventBus.post(OpenAnalyticsActionEvent(ACTION_OPEN_TOOL_DETAILS, tool.code, SOURCE_FAVORITE)) }
    }
}
