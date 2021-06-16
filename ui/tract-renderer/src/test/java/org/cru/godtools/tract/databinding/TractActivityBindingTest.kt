package org.cru.godtools.tract.databinding

import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import java.util.EnumSet
import org.ccci.gto.android.common.testing.dagger.hilt.HiltTestActivity
import org.cru.godtools.base.tool.activity.BaseToolActivity
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@Config(application = HiltTestApplication::class)
class TractActivityBindingTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    private lateinit var binding: TractActivityBinding

    @Before
    fun createBinding() {
        val activity = Robolectric.buildActivity(HiltTestActivity::class.java).get()

        binding = TractActivityBinding.inflate(LayoutInflater.from(activity), null, false)
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
