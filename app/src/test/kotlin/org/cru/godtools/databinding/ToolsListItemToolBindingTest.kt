package org.cru.godtools.databinding

import android.view.LayoutInflater
import android.view.View
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.databinding.ObservableField
import androidx.lifecycle.testing.TestLifecycleOwner
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import io.mockk.every
import io.mockk.mockk
import io.mockk.verifyAll
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
    private val callbacks = mockk<ToolsAdapterCallbacks>(relaxUnitFun = true)
    private val toolFlow = MutableStateFlow<Tool?>(tool())
    private val firstTranslation = MutableStateFlow<Translation?>(
        Translation().apply {
            languageCode = Locale("en")
            name = "primaryName"
            tagline = "primaryTagline"
        }
    )
    private val secondTranslation = MutableStateFlow<Translation?>(
        Translation().apply {
            languageCode = Locale("fr")
            name = "parallelName"
            tagline = "parallelTagline"
        }
    )
    private val parallelLanguage = MutableStateFlow<Language?>(null)
    private val toolViewModel = mockk<ToolsAdapterViewModel.ToolViewModel> {
        every { tool } returns toolFlow
        every { banner } returns MutableStateFlow(null)
        every { downloadProgress } returns emptyLiveData()
        every { firstTranslation } returns this@ToolsListItemToolBindingTest.firstTranslation
        every { secondTranslation } returns this@ToolsListItemToolBindingTest.secondTranslation
        every { secondLanguage } returns this@ToolsListItemToolBindingTest.parallelLanguage
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

        binding = ToolsListItemToolBinding.inflate(LayoutInflater.from(activity), null, false)
        binding.lifecycleOwner = TestLifecycleOwner()
        binding.callbacks = ObservableField(callbacks)
        binding.toolViewModel = toolViewModel
        binding.executePendingBindings()
    }

    // region Layout Direction
    @Test
    fun verifyLayoutDirectionWithoutTranslation() {
        firstTranslation.value = null
        binding.executePendingBindings()

        assertTrue(binding.content.isLayoutDirectionInherit())
    }

    @Test
    fun verifyLayoutDirectionWithLtrTranslation() {
        firstTranslation.value = Translation().apply { languageCode = Locale.ENGLISH }
        binding.executePendingBindings()

        assertFalse(binding.content.isLayoutDirectionInherit())
        assertEquals(View.LAYOUT_DIRECTION_LTR, binding.content.layoutDirection)
    }

    @Test
    fun verifyLayoutDirectionWithRtlTranslation() {
        firstTranslation.value = Translation().apply { languageCode = Locale("ar") }
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
        firstTranslation.value = null
        binding.executePendingBindings()

        assertEquals("parallelName", binding.title.text)
        assertEquals("Gospel Invitation", binding.tagline.text)
    }

    @Test
    fun verifyTitleAndTaglineFromTool() {
        firstTranslation.value = null
        secondTranslation.value = null
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
    fun `language_parallel - Hidden - No Second Language`() {
        parallelLanguage.value = null
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

        assertFalse(binding.actionFavorite.isSelected)
        binding.actionFavorite.performClick()
        verifyAll { callbacks.pinTool("test") }
    }

    @Test
    fun verifyActionFavoriteToolAdded() {
        toolFlow.value = tool().apply { isAdded = true }
        binding.executePendingBindings()

        assertTrue(binding.actionFavorite.isSelected)
        binding.actionFavorite.performClick()
        verifyAll { callbacks.unpinTool(toolFlow.value, firstTranslation.value) }
    }

    @Test
    fun verifyActionFavoriteRemoveFavoritePrimaryTranslationOnly() {
        toolFlow.value = tool().apply { isAdded = true }
        secondTranslation.value = null
        binding.executePendingBindings()

        binding.actionFavorite.performClick()
        verifyAll { callbacks.unpinTool(toolFlow.value, firstTranslation.value) }
    }

    @Test
    fun verifyActionFavoriteRemoveFavoriteParallelTranslationOnly() {
        toolFlow.value = tool().apply { isAdded = true }
        firstTranslation.value = null
        binding.executePendingBindings()

        binding.actionFavorite.performClick()
        verifyAll { callbacks.unpinTool(toolFlow.value, secondTranslation.value) }
    }

    @Test
    fun verifyActionFavoriteRemoveFavoriteNoTranslations() {
        toolFlow.value = tool().apply { isAdded = true }
        firstTranslation.value = null
        secondTranslation.value = null
        binding.executePendingBindings()

        binding.actionFavorite.performClick()
        verifyAll { callbacks.unpinTool(toolFlow.value, null) }
    }
    // endregion Favorite Action

    @Test
    fun verifyActionInfoTriggersOnToolInfoCallback() {
        binding.actionDetails.performClick()
        verifyAll { callbacks.showToolDetails("test") }
    }

    // region Click Action
    @Test
    fun `root view - onClick - Triggers Callback With Both Translations`() {
        binding.root.performClick()
        verifyAll { callbacks.onToolClicked(toolFlow.value, firstTranslation.value, secondTranslation.value) }
    }

    @Test
    fun `root view - onClick - Triggers Callback With Only Primary Translation`() {
        secondTranslation.value = null

        binding.root.performClick()
        verifyAll { callbacks.onToolClicked(toolFlow.value, firstTranslation.value, null) }
    }

    @Test
    fun `root view - onClick - Triggers Callback With Only Parallel Translation`() {
        firstTranslation.value = null

        binding.root.performClick()
        verifyAll { callbacks.onToolClicked(toolFlow.value, null, secondTranslation.value) }
    }

    @Test
    fun `root view - onClick -  Triggers Callback With No Translations`() {
        firstTranslation.value = null
        secondTranslation.value = null

        binding.root.performClick()
        verifyAll { callbacks.onToolClicked(toolFlow.value, null, null) }
    }
    // endregion Click Action

    private fun language(code: Locale) = Language().apply { this.code = code }
}
