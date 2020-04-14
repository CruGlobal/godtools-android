package org.cru.godtools.ui.tools

import android.app.Application
import android.view.LayoutInflater
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
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.keynote.godtools.android.activity.MainActivity
import org.robolectric.Robolectric
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class, sdk = [28])
class ToolsListItemToolBindingTest {
    private lateinit var binding: ToolsListItemToolBinding
    private lateinit var callbacks: ToolsAdapter.Callbacks
    private lateinit var viewModel: ToolsAdapterToolViewModel
    val tool = Tool().apply {
        code = "test"
    }

    @Before
    fun createBinding() {
        val activityController = Robolectric.buildActivity(MainActivity::class.java)
        callbacks = mock()
        viewModel = mock()
        whenever(viewModel.tool).thenReturn(MutableLiveData(tool))

        binding = ToolsListItemToolBinding.inflate(LayoutInflater.from(activityController.get()), null, false)
        binding.callbacks = ObservableField(callbacks)
        binding.viewModel = viewModel
        binding.executePendingBindings()
    }

    @Test
    fun verifyActionAddTriggersOnToolAddCallback() {
        reset(callbacks)

        binding.actionAdd.performClick()
        verify(callbacks).onToolAdd(eq("test"))
    }

    @Test
    fun verifyActionInfoTriggersOnToolInfoCallback() {
        reset(callbacks)

        binding.actionInfo.performClick()
        verify(callbacks).onToolInfo(eq("test"))
    }
}
