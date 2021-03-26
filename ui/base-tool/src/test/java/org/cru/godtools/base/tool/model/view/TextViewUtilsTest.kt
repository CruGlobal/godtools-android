package org.cru.godtools.base.tool.model.view

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface.BOLD_ITALIC
import android.graphics.Typeface.NORMAL
import android.view.Gravity
import android.widget.TextView
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cru.godtools.xml.R
import org.cru.godtools.xml.model.Manifest
import org.cru.godtools.xml.model.Text
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.android.controller.ActivityController

@RunWith(AndroidJUnit4::class)
class TextViewUtilsTest {
    private lateinit var activityController: ActivityController<Activity>
    private lateinit var activity: Activity
    private lateinit var view: TextView

    @Before
    fun setup() {
        activityController = Robolectric.buildActivity(Activity::class.java)
        activity = activityController.get()

        view = TextView(activity)
    }

    @Test
    fun verifyBindText() {
        val text = Text(
            Manifest(),
            text = "text",
            textScale = 2.0,
            textColor = Color.RED,
            textAlign = Text.Align.CENTER,
            textStyles = setOf(Text.Style.BOLD, Text.Style.ITALIC, Text.Style.UNDERLINE)
        )
        text.bindTo(view)
        assertEquals("text", view.text)
        assertEquals(Color.RED, view.textColors.defaultColor)
        assertEquals(Text.Align.CENTER.gravity, view.gravity and Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK)
        assertEquals(2 * activity.resources.getDimension(R.dimen.text_size_base), view.textSize, 0.001f)
        assertEquals(BOLD_ITALIC, view.typeface.style)
    }

    @Test
    fun verifyBindTextDefaults() {
        val baseTextSize = activity.resources.getDimension(R.dimen.text_size_header)
        val text = Text(Manifest(), text = "text", textScale = 1.5, textColor = null, textAlign = Text.Align.END)
        text.bindTo(view, baseTextSize, Color.GREEN)
        assertEquals("text", view.text)
        assertEquals(Color.GREEN, view.textColors.defaultColor)
        assertEquals(Text.Align.END.gravity, view.gravity and Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK)
        assertEquals(1.5f * baseTextSize, view.textSize, 0.001f)
        assertEquals(NORMAL, view.typeface?.style ?: NORMAL)
    }
}
