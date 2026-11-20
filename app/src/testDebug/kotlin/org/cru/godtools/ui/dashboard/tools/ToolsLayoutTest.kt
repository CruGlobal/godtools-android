package org.cru.godtools.ui.dashboard.tools

import android.app.Application
import android.content.Context
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.slack.circuit.test.TestEventSink
import kotlin.test.Test
import org.cru.godtools.R
import org.cru.godtools.ui.dashboard.tools.ToolsPresenter.UiEvent
import org.cru.godtools.ui.dashboard.tools.ToolsPresenter.UiState
import org.cru.godtools.ui.tools.ToolCardPresenter
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
@OptIn(ExperimentalTestApi::class)
class ToolsLayoutTest {
    private val context: Context get() = ApplicationProvider.getApplicationContext()
    private val events = TestEventSink<UiEvent>()

    // region PersonalizationUnavailable
    @Test
    fun `PersonalizationUnavailable - change localization settings fires OpenLocalizationSettings`() =
        runComposeUiTest {
            setContent {
                ToolsLayout(UiState(mode = UiState.Mode.PERSONALIZATION, tools = emptyList(), eventSink = events))
            }

            onNodeWithText(context.getString(R.string.dashboard_section_localization_box_button)).performClick()
            events.assertEvent(UiEvent.OpenLocalizationSettings)
        }

    @Test
    fun `PersonalizationUnavailable - go to all tools fires ChangeMode(ALL_TOOLS)`() = runComposeUiTest {
        setContent {
            ToolsLayout(UiState(mode = UiState.Mode.PERSONALIZATION, tools = emptyList(), eventSink = events))
        }

        onNodeWithText(context.getString(R.string.dashboard_tools_section_personalized_no_tools_action_all_tools))
            .performScrollTo()
            .performClick()
        events.assertEvent(UiEvent.ChangeMode(UiState.Mode.ALL_TOOLS))
    }

    @Test
    fun `PersonalizationUnavailable - hides the localization settings box`() = runComposeUiTest {
        setContent {
            ToolsLayout(UiState(mode = UiState.Mode.PERSONALIZATION, tools = emptyList(), eventSink = events))
        }

        onNodeWithText(context.getString(R.string.dashboard_tools_section_personalized_no_tools_title)).assertExists()
        onNodeWithText(context.getString(R.string.dashboard_tools_section_personalized_localization_title))
            .assertDoesNotExist()
    }

    @Test
    fun `PersonalizationUnavailable - not shown while data is loading`() = runComposeUiTest {
        setContent {
            ToolsLayout(
                UiState(
                    mode = UiState.Mode.PERSONALIZATION,
                    dataLoaded = false,
                    tools = emptyList(),
                    eventSink = events,
                )
            )
        }

        onNodeWithText(context.getString(R.string.dashboard_tools_section_personalized_no_tools_title))
            .assertDoesNotExist()
    }

    @Test
    fun `PersonalizationUnavailable - not shown in All Tools mode`() = runComposeUiTest {
        setContent { ToolsLayout(UiState(mode = UiState.Mode.ALL_TOOLS, tools = emptyList(), eventSink = events)) }

        onNodeWithText(context.getString(R.string.dashboard_tools_section_personalized_no_tools_title))
            .assertDoesNotExist()
    }

    @Test
    fun `LocalizationSettingsBox - shown in Personalized mode when tools are available`() = runComposeUiTest {
        setContent {
            ToolsLayout(
                UiState(
                    mode = UiState.Mode.PERSONALIZATION,
                    tools = listOf(ToolCardPresenter.UiState(toolCode = "tool1")),
                    eventSink = events,
                )
            )
        }

        onNodeWithText(context.getString(R.string.dashboard_tools_section_personalized_localization_title))
            .assertExists()
        onNodeWithText(context.getString(R.string.dashboard_tools_section_personalized_no_tools_title))
            .assertDoesNotExist()
    }
    // endregion PersonalizationUnavailable
}
