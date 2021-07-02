package org.cru.godtools.base.tool.databinding

import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.util.EnumSet
import org.cru.godtools.base.tool.activity.BaseToolActivity
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric

@RunWith(AndroidJUnit4::class)
class ActivityToolMissingBindingTest {
    private lateinit var binding: ActivityToolMissingBinding

    @Before
    fun createBinding() {
        val activity = Robolectric.buildActivity(AppCompatActivity::class.java).get()

        binding = ActivityToolMissingBinding.inflate(LayoutInflater.from(activity), null, false)
        binding.lifecycleOwner = activity
        binding.executePendingBindings()
    }

    @Test
    fun verifyNoContentVisibility() {
        val visible = setOf(BaseToolActivity.LoadingState.NOT_FOUND, BaseToolActivity.LoadingState.INVALID_TYPE)
        val notVisible = EnumSet.allOf(BaseToolActivity.LoadingState::class.java) - visible
        visible.forEach {
            binding.loadingState = MutableLiveData(it)
            binding.executePendingBindings()
            assertEquals(
                "noContent should be visible when tool state is $it",
                View.VISIBLE, binding.noContent.visibility
            )
        }

        notVisible.forEach {
            binding.loadingState = MutableLiveData(it)
            binding.executePendingBindings()
            assertEquals("noContent should be hidden when tool state is $it", View.GONE, binding.noContent.visibility)
        }
    }
}
