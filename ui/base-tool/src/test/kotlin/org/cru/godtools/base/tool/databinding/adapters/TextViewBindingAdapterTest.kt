package org.cru.godtools.base.tool.databinding.adapters

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
import com.github.ajalt.colormath.extensions.android.colorint.toColorInt
import com.github.ajalt.colormath.model.RGB
import kotlin.random.Random
import org.cru.godtools.shared.tool.parser.model.Manifest
import org.cru.godtools.shared.tool.parser.model.Text
import org.cru.godtools.shared.tool.parser.model.gravity
import org.cru.godtools.shared.tool.parser.model.textColor
import org.cru.godtools.tool.R
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
class TextViewBindingAdapterTest {
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
            textColor = RGB(1, 0, 0),
            textAlign = Text.Align.CENTER,
            textStyles = setOf(Text.Style.BOLD, Text.Style.ITALIC, Text.Style.UNDERLINE)
        )
        view.bindTextNode(text, null)
        assertEquals("text", view.text)
        assertEquals(Color.RED, view.textColors.defaultColor)
        assertEquals(Text.Align.CENTER.gravity, view.gravity and Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK)
        assertEquals(2 * activity.resources.getDimension(R.dimen.tool_content_text_size_base), view.textSize, 0.001f)
        assertEquals(BOLD_ITALIC, view.typeface.style)
        assertTrue(view.paintFlags.hasFlag(UNDERLINE_TEXT_FLAG))
    }

    @Test
    fun verifyBindTextNodeDefaults() {
        val baseTextSize = Random.nextFloat() * 30
        val text = Text(Manifest(), text = "text", textScale = 1.5, textColor = null, textAlign = Text.Align.END)
        view.bindTextNode(text, baseTextSize)
        assertEquals("text", view.text)
        assertEquals(text.textColor.toColorInt(), view.textColors.defaultColor)
        assertEquals(Text.Align.END.gravity, view.gravity and Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK)
        assertEquals(1.5f * baseTextSize, view.textSize, 0.001f)
        assertEquals(NORMAL, view.typeface?.style ?: NORMAL)
        assertFalse(view.paintFlags.hasFlag(UNDERLINE_TEXT_FLAG))
    }

    @Test
    fun verifyBindTextNodeTextColor() {
        with(Text(Manifest())) {
            view.bindTextNode(this, null)
            assertEquals(textColor.toColorInt(), view.textColors.defaultColor)
        }

        view.bindTextNode(Text(Manifest(), textColor = RGB(0, 1, 0)), null)
        assertEquals(Color.GREEN, view.textColors.defaultColor)

        view.bindTextNode(null, null)
        assertEquals((null as Text?).textColor.toColorInt(), view.textColors.defaultColor)
    }

    @Test
    fun verifyBindTextNodeTextStyles() {
        view.bindTextNode(Text(Manifest()), null)
        assertEquals(NORMAL, view.typeface?.style ?: NORMAL)

        view.bindTextNode(Text(Manifest(), textStyles = setOf(Text.Style.UNDERLINE)), null)
        assertEquals(NORMAL, view.typeface?.style ?: NORMAL)

        view.bindTextNode(Text(Manifest(), textStyles = setOf(Text.Style.BOLD)), null)
        assertEquals(BOLD, view.typeface.style)

        view.bindTextNode(Text(Manifest(), textStyles = setOf(Text.Style.ITALIC)), null)
        assertEquals(ITALIC, view.typeface.style)

        view.bindTextNode(Text(Manifest(), textStyles = setOf(Text.Style.BOLD, Text.Style.ITALIC)), null)
        assertEquals(BOLD_ITALIC, view.typeface.style)
    }
}
