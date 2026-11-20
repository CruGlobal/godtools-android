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

    // region NoPersonalizedLessons
    @Test
    fun `NoPersonalizedLessons - change localization settings fires OpenLocalizationSettings`() = runComposeUiTest {
        setContent {
            LessonsLayout(UiState(mode = UiState.Mode.PERSONALIZATION, lessons = emptyList(), eventSink = events))
        }

        onNodeWithText(context.getString(R.string.dashboard_section_localization_box_button)).performClick()
        events.assertEvent(UiEvent.OpenLocalizationSettings)
    }

    @Test
    fun `NoPersonalizedLessons - go to all lessons fires ChangeMode(ALL_LESSONS)`() = runComposeUiTest {
        setContent {
            LessonsLayout(UiState(mode = UiState.Mode.PERSONALIZATION, lessons = emptyList(), eventSink = events))
        }

        onNodeWithText(
            context.getString(R.string.dashboard_lessons_section_personalized_no_lessons_action_all_lessons)
        ).performClick()
        events.assertEvent(UiEvent.ChangeMode(UiState.Mode.ALL_LESSONS))
    }

    @Test
    fun `NoPersonalizedLessons - hides the localization settings box`() = runComposeUiTest {
        setContent {
            LessonsLayout(UiState(mode = UiState.Mode.PERSONALIZATION, lessons = emptyList(), eventSink = events))
        }

        onNodeWithText(context.getString(R.string.dashboard_lessons_section_personalized_no_lessons_title))
            .assertExists()
        onNodeWithText(context.getString(R.string.dashboard_lessons_section_personalized_localization_title))
            .assertDoesNotExist()
    }

    @Test
    fun `NoPersonalizedLessons - not shown while data is loading`() = runComposeUiTest {
        setContent {
            LessonsLayout(
                UiState(
                    mode = UiState.Mode.PERSONALIZATION,
                    dataLoaded = false,
                    lessons = emptyList(),
                    eventSink = events,
                )
            )
        }

        onNodeWithText(context.getString(R.string.dashboard_lessons_section_personalized_no_lessons_title))
            .assertDoesNotExist()
    }

    @Test
    fun `LocalizationSettingsBox - shown in Personalized mode when lessons are available`() = runComposeUiTest {
        setContent {
            LessonsLayout(
                UiState(
                    mode = UiState.Mode.PERSONALIZATION,
                    lessons = listOf(ToolCardPresenter.UiState(toolCode = "lesson1")),
                    eventSink = events,
                )
            )
        }

        onNodeWithText(context.getString(R.string.dashboard_lessons_section_personalized_localization_title))
            .assertExists()
        onNodeWithText(context.getString(R.string.dashboard_lessons_section_personalized_no_lessons_title))
            .assertDoesNotExist()
    }
    // endregion NoPersonalizedLessons

    // region FeaturedLessons
    @Test
    fun `FeaturedLessons - not shown when there are no featured lessons`() = runComposeUiTest {
        setContent { LessonsLayout(UiState(featuredLessons = emptyList(), eventSink = events)) }

        onNodeWithText(context.getString(R.string.dashboard_lessons_section_featured_label)).assertDoesNotExist()
        onNodeWithText(context.getString(R.string.dashboard_lessons_section_featured_description)).assertDoesNotExist()
    }

    @Test
    fun `FeaturedLessons - shown when there are featured lessons`() = runComposeUiTest {
        setContent {
            LessonsLayout(
                UiState(
                    featuredLessons = listOf(
                        ToolCardPresenter.UiState(
                            toolCode = "featured",
                            translation = randomTranslation(toolCode = "featured", name = "Featured Lesson"),
                        )
                    ),
                    eventSink = events,
                )
            )
        }

        onNodeWithText(context.getString(R.string.dashboard_lessons_section_featured_label)).assertExists()
        onNodeWithText(context.getString(R.string.dashboard_lessons_section_featured_description)).assertExists()
        onNodeWithText("Featured Lesson").assertExists()
    }

    @Test
    fun `FeaturedLessons - click fires ToolCardEvent_Click`() = runComposeUiTest {
        val cardEvents = TestEventSink<ToolCardPresenter.UiEvent>()
        setContent {
            LessonsLayout(
                UiState(
                    featuredLessons = listOf(
                        ToolCardPresenter.UiState(
                            toolCode = "featured",
                            translation = randomTranslation(toolCode = "featured", name = "Featured Lesson"),
                            eventSink = cardEvents,
                        )
                    ),
                    eventSink = events,
                )
            )
        }

        onNodeWithText("Featured Lesson").performClick()
        cardEvents.assertEvent(ToolCardPresenter.ToolCardEvent.Click)
        events.assertNoEvents()
    }
    // endregion FeaturedLessons
}
