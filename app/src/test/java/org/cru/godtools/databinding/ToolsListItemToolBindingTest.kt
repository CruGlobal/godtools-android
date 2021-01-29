package org.cru.godtools.databinding

import android.app.Application
import android.view.LayoutInflater
import android.view.View
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.reset
import com.nhaarman.mockitokotlin2.verify
import java.util.Locale
import org.cru.godtools.model.Language
import org.cru.godtools.model.Tool
import org.cru.godtools.model.Translation
import org.cru.godtools.ui.tools.ToolsAdapterCallbacks
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.keynote.godtools.android.activity.MainActivity
import org.robolectric.Robolectric
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
class ToolsListItemToolBindingTest {
    private lateinit var binding: ToolsListItemToolBinding
    private lateinit var callbacks: ToolsAdapterCallbacks
    private val tool = Tool().apply {
        type = Tool.Type.TRACT
        code = "test"
        name = "toolName"
        description = "toolDescription"
        category = "gospel"
    }
    private val primaryTranslation = Translation().apply {
        languageCode = Locale("en")
        name = "primaryName"
        tagline = "primaryTagline"
    }
    private val parallelTranslation = Translation().apply {
        languageCode = Locale("fr")
        name = "parallelName"
        tagline = "parallelTagline"
    }

    @Before
    fun createBinding() {
        val activityController = Robolectric.buildActivity(MainActivity::class.java)
        callbacks = mock()

        binding = ToolsListItemToolBinding.inflate(LayoutInflater.from(activityController.get()), null, false)
        binding.lifecycleOwner = activityController.get()
        binding.callbacks = ObservableField(callbacks)
        binding.tool = tool
        binding.primaryTranslation = MutableLiveData(primaryTranslation)
        binding.parallelTranslation = MutableLiveData(parallelTranslation)
        binding.executePendingBindings()
    }

    // region Layout Direction
    @Test
    fun verifyLayoutDirectionWithoutTranslation() {
        binding.primaryTranslation = MutableLiveData(null)
        binding.executePendingBindings()

        assertTrue(binding.content.isLayoutDirectionInherit())
    }

    @Test
    fun verifyLayoutDirectionWithLtrTranslation() {
        binding.primaryTranslation = MutableLiveData(Translation().apply { languageCode = Locale.ENGLISH })
        binding.executePendingBindings()

        assertFalse(binding.content.isLayoutDirectionInherit())
        assertEquals(View.LAYOUT_DIRECTION_LTR, binding.content.layoutDirection)
    }

    @Test
    fun verifyLayoutDirectionWithRtlTranslation() {
        binding.primaryTranslation = MutableLiveData(Translation().apply { languageCode = Locale("ar") })
        binding.executePendingBindings()

        assertFalse(binding.content.isLayoutDirectionInherit())
        assertEquals(View.LAYOUT_DIRECTION_RTL, binding.content.layoutDirection)
    }

    /**
     * This indirectly tests layoutDirection = 'inherit'.
     * canResolveLayoutDirection() returns 'false' only if this view and all ancestor views specify inherit.
     */
    private fun View.isLayoutDirectionInherit() = !canResolveLayoutDirection()
    // endregion Layout Direction

    // region Title & Tagline
    @Test
    fun verifyTitleAndTaglineFromPrimaryTranslation() {
        binding.executePendingBindings()

        assertEquals("primaryName", binding.title.text)
        assertEquals("Gospel Invitation", binding.tagline.text)
    }

    @Test
    fun verifyTitleAndTaglineFromParallelTranslation() {
        binding.primaryTranslation = MutableLiveData(null)
        binding.executePendingBindings()

        assertEquals("parallelName", binding.title.text)
        assertEquals("Gospel Invitation", binding.tagline.text)
    }

    @Test
    fun verifyTitleAndTaglineFromTool() {
        binding.primaryTranslation = MutableLiveData(null)
        binding.parallelTranslation = MutableLiveData(null)
        binding.executePendingBindings()

        assertEquals("toolName", binding.title.text)
        assertEquals("Gospel Invitation", binding.tagline.text)
    }
    // endregion Title & Tagline

    // region Parallel Language Label
    @Test
    fun verifyParallelLanguageLabel() {
        binding.parallelLanguage = MutableLiveData(language(Locale.FRENCH))
        binding.executePendingBindings()

        assertEquals(View.VISIBLE, binding.languageParallel.visibility)
        assertEquals("French", binding.languageParallel.text)
    }

    @Test
    fun verifyParallelLanguageLabelHiddenIfNoParallelLanguage() {
        binding.parallelLanguage = MutableLiveData(null)
        binding.executePendingBindings()

        assertEquals(View.GONE, binding.languageParallel.visibility)
    }

    @Test
    fun verifyParallelLanguageLabelHiddenIfPrimaryTranslationIsTheSameAsParallelLanguage() {
        binding.primaryTranslation = MutableLiveData(Translation().apply { languageCode = Locale("es") })
        binding.parallelLanguage = MutableLiveData(language(Locale("es")))
        binding.executePendingBindings()

        assertEquals(View.GONE, binding.languageParallel.visibility)
    }

    @Test
    fun verifyParallelLanguageLabelHiddenIfArticleToolType() {
        tool.type = Tool.Type.ARTICLE
        binding.tool = tool
        binding.parallelLanguage = MutableLiveData(language(Locale.FRENCH))
        binding.executePendingBindings()

        assertEquals(View.GONE, binding.languageParallel.visibility)
    }
    // endregion Parallel Language Label

    // region Favorite Action
    @Test
    fun verifyActionFavoriteToolNotAdded() {
        tool.isAdded = false
        binding.tool = tool
        binding.executePendingBindings()
        reset(callbacks)

        assertFalse(binding.actionFavorite.isSelected)
        binding.actionFavorite.performClick()
        verify(callbacks).addTool(eq("test"))
        verify(callbacks, never()).removeTool(any(), any())
    }

    @Test
    fun verifyActionFavoriteToolAdded() {
        tool.isAdded = true
        binding.tool = tool
        binding.executePendingBindings()
        reset(callbacks)

        assertTrue(binding.actionFavorite.isSelected)
        binding.actionFavorite.performClick()
        verify(callbacks, never()).addTool(any())
        verify(callbacks).removeTool(eq(tool), eq(primaryTranslation))
    }

    @Test
    fun verifyActionFavoriteRemoveFavoritePrimaryTranslationOnly() {
        tool.isAdded = true
        binding.tool = tool
        binding.parallelTranslation = MutableLiveData(null)
        binding.executePendingBindings()
        reset(callbacks)

        binding.actionFavorite.performClick()
        verify(callbacks, never()).addTool(any())
        verify(callbacks).removeTool(eq(tool), eq(primaryTranslation))
    }

    @Test
    fun verifyActionFavoriteRemoveFavoriteParallelTranslationOnly() {
        tool.isAdded = true
        binding.tool = tool
        binding.primaryTranslation = MutableLiveData(null)
        binding.executePendingBindings()
        reset(callbacks)

        binding.actionFavorite.performClick()
        verify(callbacks, never()).addTool(any())
        verify(callbacks).removeTool(eq(tool), eq(parallelTranslation))
    }

    @Test
    fun verifyActionFavoriteRemoveFavoriteNoTranslations() {
        tool.isAdded = true
        binding.tool = tool
        binding.primaryTranslation = MutableLiveData(null)
        binding.parallelTranslation = MutableLiveData(null)
        binding.executePendingBindings()
        reset(callbacks)

        binding.actionFavorite.performClick()
        verify(callbacks, never()).addTool(any())
        verify(callbacks).removeTool(eq(tool), eq(null))
    }
    // endregion Favorite Action

    @Test
    fun verifyActionInfoTriggersOnToolInfoCallback() {
        reset(callbacks)

        binding.actionDetails.performClick()
        verify(callbacks).onToolInfo(eq("test"))
    }

    // region Select Action
    @Test
    fun verifyClickTriggersSelectCallbackWithBothTranslations() {
        reset(callbacks)

        binding.root.performClick()
        verify(callbacks).openTool(tool, primaryTranslation, parallelTranslation)
    }

    @Test
    fun verifyClickTriggersSelectCallbackWithOnlyPrimaryTranslation() {
        binding.parallelTranslation = MutableLiveData(null)
        reset(callbacks)

        binding.root.performClick()
        verify(callbacks).openTool(tool, primaryTranslation, null)
    }

    @Test
    fun verifyClickTriggersSelectCallbackWithOnlyParallelTranslation() {
        binding.primaryTranslation = MutableLiveData(null)
        reset(callbacks)

        binding.root.performClick()
        verify(callbacks).openTool(tool, null, parallelTranslation)
    }

    @Test
    fun verifyClickTriggersSelectCallbackWithNoTranslations() {
        binding.primaryTranslation = MutableLiveData(null)
        binding.parallelTranslation = MutableLiveData(null)
        reset(callbacks)

        binding.root.performClick()
        verify(callbacks).openTool(tool, null, null)
    }
    // endregion Select Action

    private fun language(code: Locale) = Language().apply { this.code = code }
}
