package org.cru.godtools.ui.dashboard.personalization

import android.app.Application
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.cru.godtools.R
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
@OptIn(ExperimentalTestApi::class)
class PersonalizationToggleTest {
    private val selections = mutableListOf<Boolean>()

    private fun ComposeUiTest.setToggle(isPersonalized: Boolean) = setContent {
        PersonalizationToggle(
            isPersonalized = isPersonalized,
            personalizedLabel = R.string.dashboard_tools_toggle_personalized,
            allLabel = R.string.dashboard_tools_toggle_all,
            onSelect = { selections += it },
        )
    }

    @Test
    fun `PersonalizationToggle - personalized is selected`() = runComposeUiTest {
        setToggle(isPersonalized = true)

        onNodeWithText("Personalized").assertIsSelected()
    }

    @Test
    fun `PersonalizationToggle - all is selected`() = runComposeUiTest {
        setToggle(isPersonalized = false)

        onNodeWithText("All Tools").assertIsSelected()
    }

    @Test
    fun `PersonalizationToggle - click All fires onSelect(false)`() = runComposeUiTest {
        setToggle(isPersonalized = true)

        onNodeWithText("All Tools").performClick()
        assertEquals(listOf(false), selections)
    }

    @Test
    fun `PersonalizationToggle - click Personalized fires onSelect(true)`() = runComposeUiTest {
        setToggle(isPersonalized = false)

        onNodeWithText("Personalized").performClick()
        assertTrue(selections.single())
    }
}
