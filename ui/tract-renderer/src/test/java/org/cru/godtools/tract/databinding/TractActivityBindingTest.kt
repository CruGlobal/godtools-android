package org.cru.godtools.tract.databinding

import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.lifecycle.MutableLiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cru.godtools.base.tool.activity.BaseToolActivity
import org.cru.godtools.tract.R
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import java.util.EnumSet

@RunWith(AndroidJUnit4::class)
class TractActivityBindingTest {
    private lateinit var binding: TractActivityBinding

    @Before
    fun createBinding() {
        val activity = Robolectric.buildActivity(AppCompatActivity::class.java).get()
        val context = ContextThemeWrapper(activity, R.style.Theme_AppCompat)

        binding = TractActivityBinding.inflate(LayoutInflater.from(context), null, false)
        binding.lifecycleOwner = activity
        binding.executePendingBindings()
    }

    @Test
    fun verifyMainContentVisibility() {
        val visible = setOf(BaseToolActivity.ToolState.LOADED)
        val notVisible = EnumSet.allOf(BaseToolActivity.ToolState::class.java) - visible
        visible.forEach {
            binding.toolState = MutableLiveData(it)
            binding.executePendingBindings()
            assertEquals(
                "mainContent should be visible when tool state is $it",
                View.VISIBLE, binding.mainContent.visibility
            )
        }

        notVisible.forEach {
            binding.toolState = MutableLiveData(it)
            binding.executePendingBindings()
            assertEquals(
                "mainContent should be hidden when tool state is $it",
                View.GONE, binding.mainContent.visibility
            )
        }
    }
}
