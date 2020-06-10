package org.cru.godtools.base.tool.databinding

import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cru.godtools.base.tool.activity.BaseToolActivity
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import java.util.EnumSet

@RunWith(AndroidJUnit4::class)
class ActivityToolLoadingBindingTest {
    private lateinit var binding: ActivityToolLoadingBinding

    @Before
    fun createBinding() {
        val activity = Robolectric.buildActivity(AppCompatActivity::class.java).get()

        binding = ActivityToolLoadingBinding.inflate(LayoutInflater.from(activity), null, false)
        binding.lifecycleOwner = activity
        binding.executePendingBindings()
    }

    @Test
    fun verifyContentLoadingVisibility() {
        val visible = setOf(BaseToolActivity.ToolState.LOADING)
        val notVisible = EnumSet.allOf(BaseToolActivity.ToolState::class.java) - visible
        visible.forEach {
            binding.toolState = MutableLiveData(it)
            binding.executePendingBindings()
            assertEquals(
                "contentLoading should be visible when tool state is $it",
                View.VISIBLE, binding.contentLoading.visibility
            )
        }

        notVisible.forEach {
            binding.toolState = MutableLiveData(it)
            binding.executePendingBindings()
            assertEquals(
                "contentLoading should be hidden when tool state is $it",
                View.GONE, binding.contentLoading.visibility
            )
        }
    }
}
