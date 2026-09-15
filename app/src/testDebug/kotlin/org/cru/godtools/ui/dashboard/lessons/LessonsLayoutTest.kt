package org.cru.godtools.ui.dashboard.lessons

import android.app.Application
import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.slack.circuit.test.TestEventSink
import java.util.Locale
import kotlin.test.Test
import kotlinx.collections.immutable.persistentListOf
import org.cru.godtools.R
import org.cru.godtools.model.Language
import org.cru.godtools.model.randomTranslation
import org.cru.godtools.ui.dashboard.filters.FilterMenu
import org.cru.godtools.ui.dashboard.lessons.LessonsPresenter.UiEvent
import org.cru.godtools.ui.dashboard.lessons.LessonsPresenter.UiState
import org.cru.godtools.ui.tools.ToolCardPresenter
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
@OptIn(ExperimentalTestApi::class)
class LessonsLayoutTest {
    private val context: Context get() = ApplicationProvider.getApplicationContext()
    private val events = TestEventSink<UiEvent>()

    // region PersonalizationToggle
    @Test
    fun `PersonalizationToggle - not shown when personalization is disabled`() = runComposeUiTest {
        setContent { LessonsLayout(UiState(isPersonalizationEnabled = false, eventSink = events)) }

        onNodeWithText(context.getString(R.string.dashboard_lessons_toggle_personalized)).assertDoesNotExist()
        onNodeWithText(context.getString(R.string.dashboard_lessons_toggle_all)).assertDoesNotExist()
    }

    @Test
    fun `PersonalizationToggle - shown when personalization is enabled`() = runComposeUiTest {
        setContent { LessonsLayout(UiState(isPersonalizationEnabled = true, eventSink = events)) }

        onNodeWithText(context.getString(R.string.dashboard_lessons_toggle_personalized)).assertExists()
        onNodeWithText(context.getString(R.string.dashboard_lessons_toggle_all)).assertExists()
    }

    @Test
    fun `PersonalizationToggle - click Personalized fires ChangeMode(PERSONALIZATION)`() = runComposeUiTest {
        setContent {
            LessonsLayout(UiState(isPersonalizationEnabled = true, mode = UiState.Mode.ALL_LESSONS, eventSink = events))
        }

        onNodeWithText(context.getString(R.string.dashboard_lessons_toggle_personalized)).performClick()
        events.assertEvent(UiEvent.ChangeMode(UiState.Mode.PERSONALIZATION))
    }

    @Test
    fun `PersonalizationToggle - click All Lessons fires ChangeMode(ALL_LESSONS)`() = runComposeUiTest {
        setContent {
            LessonsLayout(
                UiState(isPersonalizationEnabled = true, mode = UiState.Mode.PERSONALIZATION, eventSink = events)
            )
        }

        onNodeWithText(context.getString(R.string.dashboard_lessons_toggle_all)).performClick()
        events.assertEvent(UiEvent.ChangeMode(UiState.Mode.ALL_LESSONS))
    }
    // endregion PersonalizationToggle

    // region LessonFilters
    @Test
    fun `LessonFilters - language dropdown - hides lesson counts in Personalized mode`() = runComposeUiTest {
        setContent {
            LessonsLayout(
                UiState(
                    mode = UiState.Mode.PERSONALIZATION,
                    languageFilter = FilterMenu.UiState(
                        menuExpanded = remember { mutableStateOf(true) },
                        items = persistentListOf(FilterMenu.UiState.Item(Language(Locale.FRENCH), 3)),
                    ),
                    eventSink = events,
                )
            )
        }

        onNodeWithText("French", substring = true).assertExists()
        onNodeWithText("Lessons available", substring = true, ignoreCase = true).assertDoesNotExist()
    }

    @Test
    fun `LessonFilters - language dropdown - shows lesson counts in All Lessons mode`() = runComposeUiTest {
        setContent {
            LessonsLayout(
                UiState(
                    mode = UiState.Mode.ALL_LESSONS,
                    languageFilter = FilterMenu.UiState(
                        menuExpanded = remember { mutableStateOf(true) },
                        items = persistentListOf(FilterMenu.UiState.Item(Language(Locale.FRENCH), 3)),
                    ),
                    eventSink = events,
                )
            )
        }

        onNodeWithText("3 Lessons available", substring = true, ignoreCase = true).assertExists()
    }
    // endregion LessonFilters

    // region LessonToolCard
    @Test
    fun `LessonToolCard - click fires ToolCardEvent_Click`() = runComposeUiTest {
        val cardEvents = TestEventSink<ToolCardPresenter.UiEvent>()
        setContent {
            LessonsLayout(
                UiState(
                    lessons = listOf(
                        ToolCardPresenter.UiState(
                            toolCode = "lesson1",
                            translation = randomTranslation(toolCode = "lesson1", name = "Test Lesson"),
                            eventSink = cardEvents,
                        )
                    ),
                    eventSink = events,
                )
            )
        }

        onNodeWithText("Test Lesson").performClick()
        cardEvents.assertEvent(ToolCardPresenter.ToolCardEvent.Click)
        events.assertNoEvents()
    }
    // endregion LessonToolCard
}
