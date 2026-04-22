package org.cru.godtools.ui.banner.favoritetools

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
import org.cru.godtools.R
import org.cru.godtools.ui.banner.favoritetools.FavoriteToolsBannerPresenter.UiEvent
import org.cru.godtools.ui.banner.favoritetools.FavoriteToolsBannerPresenter.UiState
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
@OptIn(ExperimentalTestApi::class)
class FavoriteToolsBannerLayoutTest {
    private val context: Context get() = ApplicationProvider.getApplicationContext()
    private val events = TestEventSink<UiEvent>()

    // region UiEvent.Dismiss
    @Test
    fun `Action - Dismiss button - fires Dismiss event`() = runComposeUiTest {
        setContent { FavoriteToolsBannerLayout(UiState(eventSink = events)) }

        onNodeWithText(context.getString(R.string.tools_list_favorites_banner_action_dismiss))
            .performClick()

        events.assertEvent(UiEvent.Dismiss)
    }
    // endregion UiEvent.Dismiss
}
