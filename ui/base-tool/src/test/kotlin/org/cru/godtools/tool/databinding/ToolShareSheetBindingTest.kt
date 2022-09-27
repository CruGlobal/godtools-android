package org.cru.godtools.tool.databinding

import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.material.R
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.robolectric.Robolectric

@RunWith(AndroidJUnit4::class)
class ToolShareSheetBindingTest {
    private lateinit var binding: ToolShareSheetBinding

    @Before
    fun createBinding() {
        val activity = Robolectric.buildActivity(AppCompatActivity::class.java).get()
        activity.setTheme(R.style.Theme_MaterialComponents)

        binding = ToolShareSheetBinding.inflate(LayoutInflater.from(activity), null, false)
        binding.lifecycleOwner = activity
        binding.executePendingBindings()
    }

    // region otherActions
    @Test
    fun `otherActions - hidden when otherShareItems is null`() {
        binding.otherShareItems = null
        binding.executePendingBindings()

        assertEquals(View.GONE, binding.otherActions.visibility)
    }

    @Test
    fun `otherActions - hidden when otherShareItems is empty`() {
        binding.otherShareItems = emptyList()
        binding.executePendingBindings()

        assertEquals(View.GONE, binding.otherActions.visibility)
    }

    @Test
    fun `otherActions - visible when otherShareItems contains items`() {
        binding.otherShareItems = listOf(mock())
        binding.executePendingBindings()

        assertEquals(View.VISIBLE, binding.otherActions.visibility)
    }
    // endregion otherActions
}
