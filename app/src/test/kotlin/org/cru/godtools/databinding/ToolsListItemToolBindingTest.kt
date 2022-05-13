package org.cru.godtools.databinding

import android.view.LayoutInflater
import android.view.View
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.testing.TestLifecycleOwner
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import io.mockk.every
import io.mockk.mockk
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import org.ccci.gto.android.common.androidx.lifecycle.emptyLiveData
import org.ccci.gto.android.common.testing.dagger.hilt.HiltTestActivity
import org.cru.godtools.model.Language
import org.cru.godtools.model.Tool
import org.cru.godtools.model.Translation
import org.cru.godtools.ui.tools.ToolsAdapterCallbacks
import org.cru.godtools.ui.tools.ToolsAdapterViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify
import org.robolectric.Robolectric
import org.robolectric.annotation.Config

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@Config(application = HiltTestApplication::class)
class ToolsListItemToolBindingTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)
    @get:Rule
    val instantTaskRule = InstantTaskExecutorRule()

    private lateinit var binding: ToolsListItemToolBinding
    private lateinit var callbacks: ToolsAdapterCallbacks
    private val toolFlow = MutableStateFlow<Tool?>(tool())
    private val primaryTranslation = MutableLiveData(
        Translation().apply {
            languageCode = Locale("en")
            name = "primaryName"
            tagline = "primaryTagline"
        }
    )
    private val parallelTranslation = MutableLiveData(
        Translation().apply {
            languageCode = Locale("fr")
            name = "parallelName"
            tagline = "parallelTagline"
        }
    )
    private val parallelLanguage = MutableLiveData<Language?>(null)
    private val toolViewModel = mockk<ToolsAdapterViewModel.ToolViewModel> {
        every { tool } returns toolFlow
        every { banner } returns MutableStateFlow(null)
        every { downloadProgress } returns emptyLiveData()
        every { firstTranslation } returns primaryTranslation
        every { parallelTranslation } returns this@ToolsListItemToolBindingTest.parallelTranslation
        every { parallelLanguage } returns this@ToolsListItemToolBindingTest.parallelLanguage
    }

    private fun tool() = Tool().apply {
        type = Tool.Type.TRACT
        code = "test"
        name = "toolName"
        description = "toolDescription"
        category = "gospel"
    }

    @Before
    fun createBinding() {
        val activity = Robolectric.buildActivity(HiltTestActivity::class.java).get()
        callbacks = mock()

        binding = ToolsListItemToolBinding.inflate(LayoutInflater.from(activity), null, false)
        binding.lifecycleOwner = TestLifecycleOwner()
        binding.callbacks = ObservableField(callbacks)
        binding.toolViewModel = toolViewModel
        binding.executePendingBindings()
    }

    // region Layout Direction
    @Test
    fun verifyLayoutDirectionWithoutTranslation() {
        primaryTranslation.value = null
        binding.executePendingBindings()

        assertTrue(binding.content.isLayoutDirectionInherit())
    }

    @Test
    fun verifyLayoutDirectionWithLtrTranslation() {
        primaryTranslation.value = Translation().apply { languageCode = Locale.ENGLISH }
        binding.executePendingBindings()

        assertFalse(binding.content.isLayoutDirectionInherit())
        assertEquals(View.LAYOUT_DIRECTION_LTR, binding.content.layoutDirection)
    }

    @Test
    fun verifyLayoutDirectionWithRtlTranslation() {
        primaryTranslation.value = Translation().apply { languageCode = Locale("ar") }
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
        primaryTranslation.value = null
        binding.executePendingBindings()

        assertEquals("parallelName", binding.title.text)
        assertEquals("Gospel Invitation", binding.tagline.text)
    }

    @Test
    fun verifyTitleAndTaglineFromTool() {
        primaryTranslation.value = null
        parallelTranslation.value = null
        binding.executePendingBindings()

        assertEquals("toolName", binding.title.text)
        assertEquals("Gospel Invitation", binding.tagline.text)
    }
    // endregion Title & Tagline

    // region Parallel Language Label
    @Test
    fun `language_parallel - Content`() {
        parallelLanguage.value = language(Locale.FRENCH)
        binding.executePendingBindings()

        assertEquals(View.VISIBLE, binding.languageParallel.visibility)
        assertEquals("French", binding.languageParallel.text)
    }

    @Test
    fun `language_parallel - Hidden - No Parallel Language`() {
        parallelLanguage.value = null
        binding.executePendingBindings()

        assertEquals(View.GONE, binding.languageParallel.visibility)
    }

    @Test
    fun `language_parallel - Hidden - Primary Translation === Parallel Language`() {
        primaryTranslation.value = Translation().apply { languageCode = Locale("es") }
        parallelLanguage.value = language(Locale("es"))
        binding.executePendingBindings()

        assertEquals(View.GONE, binding.languageParallel.visibility)
    }

    @Test
    fun `language_parallel - Hidden - Article Tool Type`() {
        toolFlow.value = tool().apply { type = Tool.Type.ARTICLE }
        parallelLanguage.value = language(Locale.FRENCH)
        binding.executePendingBindings()

        assertEquals(View.GONE, binding.languageParallel.visibility)
    }

    @Test
    fun `language_parallel - Visible - Cyoa Tool Type`() {
        toolFlow.value = tool().apply { type = Tool.Type.CYOA }
        parallelLanguage.value = language(Locale.FRENCH)
        binding.executePendingBindings()

        assertEquals(View.VISIBLE, binding.languageParallel.visibility)
        assertEquals("French", binding.languageParallel.text)
    }
    // endregion Parallel Language Label

    // region Favorite Action
    @Test
    fun verifyActionFavoriteToolNotAdded() {
        toolFlow.value = tool().apply { isAdded = false }
        binding.executePendingBindings()
        reset(callbacks)

        assertFalse(binding.actionFavorite.isSelected)
        binding.actionFavorite.performClick()
        verify(callbacks).pinTool(eq("test"))
        verify(callbacks, never()).unpinTool(any(), any())
    }

    @Test
    fun verifyActionFavoriteToolAdded() {
        toolFlow.value = tool().apply { isAdded = true }
        binding.executePendingBindings()
        reset(callbacks)

        assertTrue(binding.actionFavorite.isSelected)
        binding.actionFavorite.performClick()
        verify(callbacks, never()).pinTool(any())
        verify(callbacks).unpinTool(eq(toolFlow.value), eq(primaryTranslation.value))
    }

    @Test
    fun verifyActionFavoriteRemoveFavoritePrimaryTranslationOnly() {
        toolFlow.value = tool().apply { isAdded = true }
        parallelTranslation.value = null
        binding.executePendingBindings()
        reset(callbacks)

        binding.actionFavorite.performClick()
        verify(callbacks, never()).pinTool(any())
        verify(callbacks).unpinTool(eq(toolFlow.value), eq(primaryTranslation.value))
    }

    @Test
    fun verifyActionFavoriteRemoveFavoriteParallelTranslationOnly() {
        toolFlow.value = tool().apply { isAdded = true }
        primaryTranslation.value = null
        binding.executePendingBindings()
        reset(callbacks)

        binding.actionFavorite.performClick()
        verify(callbacks, never()).pinTool(any())
        verify(callbacks).unpinTool(eq(toolFlow.value), eq(parallelTranslation.value))
    }

    @Test
    fun verifyActionFavoriteRemoveFavoriteNoTranslations() {
        toolFlow.value = tool().apply { isAdded = true }
        primaryTranslation.value = null
        parallelTranslation.value = null
        binding.executePendingBindings()
        reset(callbacks)

        binding.actionFavorite.performClick()
        verify(callbacks, never()).pinTool(any())
        verify(callbacks).unpinTool(eq(toolFlow.value), eq(null))
    }
    // endregion Favorite Action

    @Test
    fun verifyActionInfoTriggersOnToolInfoCallback() {
        reset(callbacks)

        binding.actionDetails.performClick()
        verify(callbacks).showToolDetails(eq("test"))
    }

    // region Click Action
    @Test
    fun `root view - onClick - Triggers Callback With Both Translations`() {
        reset(callbacks)

        binding.root.performClick()
        verify(callbacks).onToolClicked(toolFlow.value, primaryTranslation.value, parallelTranslation.value)
    }

    @Test
    fun `root view - onClick - Triggers Callback With Only Primary Translation`() {
        parallelTranslation.value = null
        reset(callbacks)

        binding.root.performClick()
        verify(callbacks).onToolClicked(toolFlow.value, primaryTranslation.value, null)
    }

    @Test
    fun `root view - onClick - Triggers Callback With Only Parallel Translation`() {
        primaryTranslation.value = null
        reset(callbacks)

        binding.root.performClick()
        verify(callbacks).onToolClicked(toolFlow.value, null, parallelTranslation.value)
    }

    @Test
    fun `root view - onClick -  Triggers Callback With No Translations`() {
        primaryTranslation.value = null
        parallelTranslation.value = null
        reset(callbacks)

        binding.root.performClick()
        verify(callbacks).onToolClicked(toolFlow.value, null, null)
    }
    // endregion Click Action

    private fun language(code: Locale) = Language().apply { this.code = code }
}
