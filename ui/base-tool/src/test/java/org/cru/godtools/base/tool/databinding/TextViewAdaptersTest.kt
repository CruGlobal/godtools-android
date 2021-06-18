package org.cru.godtools.base.tool.databinding

import android.app.Activity
import android.graphics.Color
import android.graphics.Paint.UNDERLINE_TEXT_FLAG
import android.graphics.Typeface.BOLD
import android.graphics.Typeface.BOLD_ITALIC
import android.graphics.Typeface.ITALIC
import android.graphics.Typeface.NORMAL
import android.view.Gravity
import android.widget.TextView
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cru.godtools.xml.R
import org.cru.godtools.xml.model.Manifest
import org.cru.godtools.xml.model.Text
import org.cru.godtools.xml.model.defaultTextColor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.android.controller.ActivityController
import splitties.bitflags.hasFlag

@RunWith(AndroidJUnit4::class)
class TextViewAdaptersTest {
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
    fun verifyBindTextNode() {
        val text = Text(
            Manifest(),
            text = "text",
            textScale = 2.0,
            textColor = Color.RED,
            textAlign = Text.Align.CENTER,
            textStyles = setOf(Text.Style.BOLD, Text.Style.ITALIC, Text.Style.UNDERLINE)
        )
        view.bindTextNode(text, null, null)
        assertEquals("text", view.text)
        assertEquals(Color.RED, view.textColors.defaultColor)
        assertEquals(Text.Align.CENTER.gravity, view.gravity and Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK)
        assertEquals(2 * activity.resources.getDimension(R.dimen.text_size_base), view.textSize, 0.001f)
        assertEquals(BOLD_ITALIC, view.typeface.style)
        assertTrue(view.paintFlags.hasFlag(UNDERLINE_TEXT_FLAG))
    }

    @Test
    fun verifyBindTextNodeDefaults() {
        val baseTextSize = activity.resources.getDimension(R.dimen.text_size_header)
        val text = Text(Manifest(), text = "text", textScale = 1.5, textColor = null, textAlign = Text.Align.END)
        view.bindTextNode(text, baseTextSize, Color.GREEN)
        assertEquals("text", view.text)
        assertEquals(Color.GREEN, view.textColors.defaultColor)
        assertEquals(Text.Align.END.gravity, view.gravity and Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK)
        assertEquals(1.5f * baseTextSize, view.textSize, 0.001f)
        assertEquals(NORMAL, view.typeface?.style ?: NORMAL)
        assertFalse(view.paintFlags.hasFlag(UNDERLINE_TEXT_FLAG))
    }

    @Test
    fun verifyBindTextNodeTextColor() {
        with(Text(Manifest())) {
            view.bindTextNode(this, null, null)
            assertEquals(defaultTextColor, view.textColors.defaultColor)
        }

        view.bindTextNode(Text(Manifest(), textColor = Color.GREEN), null, Color.RED)
        assertEquals(Color.GREEN, view.textColors.defaultColor)

        view.bindTextNode(null, null, Color.BLUE)
        assertEquals(Color.BLUE, view.textColors.defaultColor)

        view.bindTextNode(null, null, null)
        assertEquals((null as Text?).defaultTextColor, view.textColors.defaultColor)
    }

    @Test
    fun verifyBindTextNodeTextStyles() {
        view.bindTextNode(Text(Manifest()), null, null)
        assertEquals(NORMAL, view.typeface?.style ?: NORMAL)

        view.bindTextNode(Text(Manifest(), textStyles = setOf(Text.Style.UNDERLINE)), null, null)
        assertEquals(NORMAL, view.typeface?.style ?: NORMAL)

        view.bindTextNode(Text(Manifest(), textStyles = setOf(Text.Style.BOLD)), null, null)
        assertEquals(BOLD, view.typeface.style)

        view.bindTextNode(Text(Manifest(), textStyles = setOf(Text.Style.ITALIC)), null, null)
        assertEquals(ITALIC, view.typeface.style)

        view.bindTextNode(Text(Manifest(), textStyles = setOf(Text.Style.BOLD, Text.Style.ITALIC)), null, null)
        assertEquals(BOLD_ITALIC, view.typeface.style)
    }
}
