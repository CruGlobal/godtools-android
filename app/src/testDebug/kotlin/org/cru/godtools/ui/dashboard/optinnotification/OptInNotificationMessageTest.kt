package org.cru.godtools.ui.dashboard.optinnotification

import android.app.Application
import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlin.test.Test
import kotlin.test.assertEquals
import org.cru.godtools.R
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
@OptIn(ExperimentalTestApi::class)
class OptInNotificationMessageTest {
    private val context: Context get() = ApplicationProvider.getApplicationContext()

    private fun SemanticsNodeInteraction.fontSize(): TextUnit {
        val results = mutableListOf<TextLayoutResult>()
        fetchSemanticsNode().config[SemanticsActions.GetTextLayoutResult].action?.invoke(results)
        return results.single().layoutInput.style.fontSize
    }

    // region OptInNotificationMessage()
    @Test
    @Config(qualifiers = "mdpi")
    fun `OptInNotificationMessage() - Font size - phone - mdpi`() = assertFontSizes(
        width = 300.dp,
        isTablet = false,
        titleFontSize = 21.sp,
        bodyFontSize = 17.sp,
    )

    @Test
    @Config(qualifiers = "xxxhdpi")
    fun `OptInNotificationMessage() - Font size - phone - xxxhdpi`() = assertFontSizes(
        width = 300.dp,
        isTablet = false,
        titleFontSize = 21.sp,
        bodyFontSize = 17.sp,
    )

    @Test
    @Config(qualifiers = "mdpi")
    fun `OptInNotificationMessage() - Font size - small phone`() = assertFontSizes(
        width = 200.dp,
        isTablet = false,
        titleFontSize = 16.sp,
        bodyFontSize = 13.sp,
    )

    @Test
    @Config(qualifiers = "xhdpi")
    fun `OptInNotificationMessage() - Font size - tablet`() = assertFontSizes(
        width = 364.dp,
        isTablet = true,
        titleFontSize = 24.sp,
        bodyFontSize = 20.sp,
    )

    @Test
    @Config(qualifiers = "xhdpi")
    fun `OptInNotificationMessage() - Font size - large tablet`() = assertFontSizes(
        width = 424.dp,
        isTablet = true,
        titleFontSize = 28.sp,
        bodyFontSize = 22.sp,
    )

    private fun assertFontSizes(width: Dp, isTablet: Boolean, titleFontSize: TextUnit, bodyFontSize: TextUnit) =
        runComposeUiTest {
            setContent {
                Box(modifier = Modifier.requiredWidth(width)) {
                    OptInNotificationMessage(isTablet = isTablet)
                }
            }

            val title = onNodeWithText(context.getString(R.string.opt_in_notification_title))
            val body = onNodeWithText(context.getString(R.string.opt_in_notification_body))
            assertEquals(titleFontSize, title.fontSize())
            assertEquals(bodyFontSize, body.fontSize())
        }
    // endregion OptInNotificationMessage()
}
