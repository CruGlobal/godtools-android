package org.cru.godtools.base.ui.util

import android.content.Context
import android.content.res.Resources
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.util.Locale
import org.cru.godtools.model.Tool
import org.cru.godtools.ui.BuildConfig
import org.cru.godtools.ui.R
import org.junit.Assert.assertEquals
import org.junit.Assume.assumeFalse
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ModelUtilsRobolectricTest {
    private val context: Context get() = ApplicationProvider.getApplicationContext()

    @Test
    fun `getCategory() - Null Tool`() {
        val tool: Tool? = null
        assertEquals("", tool.getCategory(context, null))
        assertEquals("", tool.getCategory(context, Locale.FRENCH))
    }

    @Test
    fun `getCategory() - Valid Category`() {
        val tool = Tool("tool", category = "gospel")
        assertEquals("Gospel Invitation", tool.getCategory(context, null))
        assertEquals("Invitation à Évangile", tool.getCategory(context, Locale.FRENCH))
    }

    @Test(expected = Resources.NotFoundException::class)
    fun `getCategory(unknown) - Debug`() {
        assumeTrue(BuildConfig.DEBUG)
        Tool("tool", category = "unknown").getCategory(context)
    }

    @Test
    fun `getCategory(unknown) - Release`() {
        assumeFalse(BuildConfig.DEBUG)
        val tool = Tool("tool", category = "unknown")
        assertEquals("unknown", tool.getCategory(context, null))
        assertEquals("unknown", tool.getCategory(context, Locale.FRENCH))
    }

    @Test
    fun `getToolCategoryName()`() {
        assertEquals(context.getString(R.string.tool_category_gospel), getToolCategoryName("gospel", context))
        assertEquals(context.getString(R.string.tool_category_articles), getToolCategoryName("articles", context))
        assertEquals(
            context.getString(R.string.tool_category_conversation_starter),
            getToolCategoryName("conversation_starter", context)
        )
        assertEquals(context.getString(R.string.tool_category_growth), getToolCategoryName("growth", context))
        assertEquals(context.getString(R.string.tool_category_training), getToolCategoryName("training", context))
    }
}
