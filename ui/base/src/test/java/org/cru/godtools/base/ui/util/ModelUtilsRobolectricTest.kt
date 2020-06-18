package org.cru.godtools.base.ui.util

import android.app.Activity
import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cru.godtools.model.Tool
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import java.util.Locale

@RunWith(AndroidJUnit4::class)
class ModelUtilsRobolectricTest {
    private lateinit var context: Context
    private val tool = Tool().apply {
        category = "gospel"
    }
    private val toolNull: Tool? = null

    @Before
    fun setup() {
        context = Robolectric.buildActivity(Activity::class.java).get()
    }

    @Test
    fun testToolGetCategoryNullTool() {
        assertEquals("", toolNull.getCategory(context, null))
        assertEquals("", toolNull.getCategory(context, Locale.FRENCH))
    }

    @Test
    fun testToolGetCategoryValidCategory() {
        assertEquals("Gospel Invitation", tool.getCategory(context, null))
        assertEquals("Invitation à Évangile", tool.getCategory(context, Locale.FRENCH))
    }

    @Test
    fun testToolGetCategoryUnknownCategory() {
        tool.category = "unknown"
        assertEquals("unknown", tool.getCategory(context, null))
        assertEquals("unknown", tool.getCategory(context, Locale.FRENCH))
    }
}
