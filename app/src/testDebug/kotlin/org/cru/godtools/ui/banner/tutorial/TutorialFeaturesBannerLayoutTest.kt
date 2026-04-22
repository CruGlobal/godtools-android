package org.cru.godtools.ui.banner.tutorial

import android.app.Application
import android.content.Context
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.slack.circuit.test.TestEventSink
import kotlin.test.Test
import org.cru.godtools.tutorial.R
import org.cru.godtools.ui.banner.tutorial.TutorialFeaturesBannerPresenter.UiEvent
import org.cru.godtools.ui.banner.tutorial.TutorialFeaturesBannerPresenter.UiState
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
@OptIn(ExperimentalTestApi::class)
class TutorialFeaturesBannerLayoutTest {
    private val context: Context get() = ApplicationProvider.getApplicationContext()
    private val events = TestEventSink<UiEvent>()

    // region UiEvent.OpenTutorial
    @Test
    fun `Action - Open button - fires OpenTutorial event`() = runComposeUiTest {
        setContent { TutorialFeaturesBannerLayout(UiState(eventSink = events)) }

        onNodeWithText(context.getString(R.string.tutorial_features_banner_action_open))
            .performClick()

        events.assertEvent(UiEvent.OpenTutorial)
    }
    // endregion UiEvent.OpenTutorial

    // region UiEvent.Dismiss
    @Test
    fun `Action - Dismiss button - fires Dismiss event`() = runComposeUiTest {
        setContent { TutorialFeaturesBannerLayout(UiState(eventSink = events)) }

        onNodeWithText(context.getString(R.string.tutorial_features_banner_action_dismiss))
            .performClick()

        events.assertEvent(UiEvent.Dismiss)
    }
    // endregion UiEvent.Dismiss
}
