package org.cru.godtools.ui.tools

import android.app.Application
import android.view.LayoutInflater
import android.view.View
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.reset
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.cru.godtools.adapter.ToolsAdapter
import org.cru.godtools.databinding.ToolsListItemToolBinding
import org.cru.godtools.model.Tool
import org.cru.godtools.model.Translation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.keynote.godtools.android.activity.MainActivity
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import java.util.Locale

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class, sdk = [28])
class ToolsListItemToolBindingTest {
    private lateinit var binding: ToolsListItemToolBinding
    private lateinit var callbacks: ToolsAdapter.Callbacks
    private lateinit var viewModel: ToolsAdapterToolViewModel
    private val tool = Tool().apply {
        code = "test"
    }

    @Before
    fun createBinding() {
        val activityController = Robolectric.buildActivity(MainActivity::class.java)
        callbacks = mock()
        viewModel = mock()
        whenever(viewModel.tool).thenReturn(MutableLiveData(tool))

        binding = ToolsListItemToolBinding.inflate(LayoutInflater.from(activityController.get()), null, false)
        binding.lifecycleOwner = activityController.get()
        binding.callbacks = ObservableField(callbacks)
        binding.viewModel = viewModel
        binding.executePendingBindings()
    }

    // region Layout Direction
    @Test
    fun verifyLayoutDirectionWithoutTranslation() {
        assertTrue(binding.content.isLayoutDirectionInherit())
    }

    @Test
    fun verifyLayoutDirectionWithLtrTranslation() {
        val translation = Translation().apply {
            languageCode = Locale.ENGLISH
        }
        whenever(viewModel.firstTranslation).thenReturn(MutableLiveData(translation))
        binding.invalidateAll()
        binding.executePendingBindings()

        assertFalse(binding.content.isLayoutDirectionInherit())
        assertEquals(View.LAYOUT_DIRECTION_LTR, binding.content.layoutDirection)
    }

    @Test
    fun verifyLayoutDirectionWithRtlTranslation() {
        val translation = Translation().apply {
            languageCode = Locale("ar")
        }
        whenever(viewModel.firstTranslation).thenReturn(MutableLiveData(translation))
        binding.invalidateAll()
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

    // region Add Action
    @Test
    fun verifyActionAddIsVisibleAndEnabledWhenToolHasntBeenAdded() {
        tool.isAdded = false
        binding.invalidateAll()
        binding.executePendingBindings()

        assertTrue(binding.actionAdd.isEnabled)
        assertEquals(View.VISIBLE, binding.actionAdd.visibility)
    }

    @Test
    fun verifyActionAddIsGoneWhenToolHasBeenAdded() {
        tool.isAdded = true
        binding.invalidateAll()
        binding.executePendingBindings()

        assertEquals(View.GONE, binding.actionAdd.visibility)
        assertFalse(binding.actionAdd.isEnabled)
    }

    @Test
    fun verifyActionAddTriggersOnToolAddCallback() {
        reset(callbacks)

        binding.actionAdd.performClick()
        verify(callbacks).onToolAdd(eq("test"))
    }
    // endregion Add Action

    @Test
    fun verifyActionInfoTriggersOnToolInfoCallback() {
        reset(callbacks)

        binding.actionInfo.performClick()
        verify(callbacks).onToolInfo(eq("test"))
    }
}
