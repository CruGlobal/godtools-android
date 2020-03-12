package org.cru.godtools.base.tool.model.view

import android.app.Activity
import android.graphics.Color
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
        val text = Text(Manifest(), "text", 2.0, Color.RED, Text.Align.CENTER)
        text.bindTo(view)
        assertEquals("text", view.text)
        assertEquals(Color.RED, view.textColors.defaultColor)
        assertEquals(Text.Align.CENTER.mGravity, view.gravity and Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK)
        assertEquals(2 * activity.resources.getDimension(R.dimen.text_size_base), view.textSize, 0.001f)
    }

    @Test
    fun verifyBindTextDefaults() {
        val text = Text(Manifest(), "text", 1.5, null, Text.Align.END)
        text.bindTo(view, R.dimen.text_size_header, Color.GREEN)
        assertEquals("text", view.text)
        assertEquals(Color.GREEN, view.textColors.defaultColor)
        assertEquals(Text.Align.END.mGravity, view.gravity and Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK)
        assertEquals(1.5f * activity.resources.getDimension(R.dimen.text_size_header), view.textSize, 0.001f)
    }
}
