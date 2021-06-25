package org.cru.godtools.base.tool.databinding

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Space
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cru.godtools.tool.model.Manifest
import org.cru.godtools.tool.model.Spacer
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ToolContentSpacerBindingTest {
    private val context get() = ApplicationProvider.getApplicationContext<Context>()

    private lateinit var binding: ToolContentSpacerBinding
    private val view get() = binding.root as Space

    @Test
    fun `LinearLayout - Auto Spacer`() {
        createBinding(LinearLayout(context))
        binding.model = Spacer(Manifest())
        binding.executePendingBindings()
        assertEquals(1f, (view.layoutParams as LinearLayout.LayoutParams).weight, 0.0001f)
        assertEquals(0, view.layoutParams.height)
    }

    @Test
    fun `LinearLayout - Fixed Spacer`() {
        context.resources.displayMetrics.density = 2f
        createBinding(LinearLayout(context))
        binding.model = Spacer(Manifest(), mode = Spacer.Mode.FIXED, height = 10)
        binding.executePendingBindings()
        assertEquals(0f, (view.layoutParams as LinearLayout.LayoutParams).weight, 0.0001f)
        assertEquals(20, view.layoutParams.height)
    }

    @Test
    fun `FrameLayout - Auto Spacer`() {
        createBinding(FrameLayout(context))
        binding.model = Spacer(Manifest())
        binding.executePendingBindings()
        assertEquals(0, view.layoutParams.height)
    }

    @Test
    fun `FrameLayout - Fixed Spacer`() {
        context.resources.displayMetrics.density = 2f
        createBinding(FrameLayout(context))
        binding.model = Spacer(Manifest(), mode = Spacer.Mode.FIXED, height = 10)
        binding.executePendingBindings()
        assertEquals(20, view.layoutParams.height)
    }

    private fun createBinding(parent: ViewGroup) {
        binding = ToolContentSpacerBinding.inflate(LayoutInflater.from(parent.context), parent, true)
    }
}
