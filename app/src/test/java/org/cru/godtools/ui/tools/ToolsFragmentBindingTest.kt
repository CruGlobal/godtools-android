package org.cru.godtools.ui.tools

import android.app.Application
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cru.godtools.databinding.ToolsFragmentBinding
import org.cru.godtools.model.Tool
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.keynote.godtools.android.activity.MainActivity
import org.robolectric.Robolectric
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
class ToolsFragmentBindingTest {
    private lateinit var binding: ToolsFragmentBinding

    @Before
    fun createBinding() {
        val activityController = Robolectric.buildActivity(MainActivity::class.java)

        binding = ToolsFragmentBinding.inflate(LayoutInflater.from(activityController.get()), null, false)
        binding.lifecycleOwner = activityController.get()
    }

    @Test
    fun verifyEmptyListUiHideBeforeToolsSet() {
        binding.setTools(null)
        binding.executePendingBindings()

        // we don't want the empty list UI visible before the list of tools has a chance to load
        assertEquals(View.GONE, binding.emptyListUi.visibility)
    }

    @Test
    fun verifyEmptyListUiHideBeforeToolsLoaded() {
        binding.setTools(MutableLiveData(null))
        binding.executePendingBindings()

        // we don't want the empty list UI visible before the list of tools has a chance to load
        assertEquals(View.GONE, binding.emptyListUi.visibility)
    }

    @Test
    fun verifyUiWithTools() {
        binding.setTools(MutableLiveData(listOf(Tool())))
        binding.executePendingBindings()

        assertEquals(View.GONE, binding.emptyListUi.visibility)
        assertEquals(ViewGroup.LayoutParams.MATCH_PARENT, binding.tools.layoutParams.height)
    }

    @Test
    fun verifyUiWithEmptyToolsList() {
        binding.setTools(MutableLiveData(emptyList()))
        binding.executePendingBindings()

        assertEquals(View.VISIBLE, binding.emptyListUi.visibility)
        assertEquals(ViewGroup.LayoutParams.WRAP_CONTENT, binding.tools.layoutParams.height)
    }
}
