package org.cru.godtools.tool.databinding

import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.R
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
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
class ToolGenericFragmentActivityBindingTest {
    private lateinit var binding: ToolGenericFragmentActivityBinding

    @Before
    fun createBinding() {
        val activity = Robolectric.buildActivity(AppCompatActivity::class.java).get()
        val context = ContextThemeWrapper(activity, R.style.Theme_AppCompat)

        binding = ToolGenericFragmentActivityBinding.inflate(LayoutInflater.from(context), null, false)
        binding.lifecycleOwner = activity
        binding.executePendingBindings()
    }

    @Test
    fun verifyMainContentVisibility() {
        val visible = setOf(BaseToolActivity.LoadingState.LOADED)
        val notVisible = EnumSet.allOf(BaseToolActivity.LoadingState::class.java) - visible
        visible.forEach {
            binding.loadingState = MutableLiveData(it)
            binding.executePendingBindings()
            assertEquals(
                "mainContent should be visible when tool state is $it",
                View.VISIBLE,
                binding.mainContent.visibility
            )
        }

        notVisible.forEach {
            binding.loadingState = MutableLiveData(it)
            binding.executePendingBindings()
            assertEquals(
                "mainContent should be hidden when tool state is $it",
                View.GONE,
                binding.mainContent.visibility
            )
        }
    }
}
