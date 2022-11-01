package org.cru.godtools.tool.tract.databinding

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.R
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.widget.ImageViewCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verifyAll
import org.cru.godtools.shared.tool.parser.model.Text
import org.cru.godtools.shared.tool.parser.model.tract.CallToAction
import org.cru.godtools.shared.tool.parser.model.tract.TractPage
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric

@RunWith(AndroidJUnit4::class)
class TractPageCallToActionBindingTest {
    private lateinit var binding: TractPageCallToActionBinding

    @Before
    fun setup() {
        val activity = Robolectric.buildActivity(AppCompatActivity::class.java).get()
        val context = ContextThemeWrapper(activity, R.style.Theme_AppCompat)

        binding = TractPageCallToActionBinding.inflate(LayoutInflater.from(context), null, false)
        binding.lifecycleOwner = activity
        binding.callbacks = mockk(relaxUnitFun = true)
        binding.executePendingBindings()
    }

    // region Arrow Tests
    @Test
    fun verifyArrowVisibilityLastPage() {
        binding.page = spyk(TractPage()) { every { isLastPage } returns true }
        binding.executePendingBindings()
        assertEquals(View.GONE, binding.callToActionArrow.visibility)
    }

    @Test
    fun verifyArrowVisibilityNotLastPage() {
        binding.page = spyk(TractPage()) { every { isLastPage } returns false }
        binding.executePendingBindings()
        assertEquals(View.VISIBLE, binding.callToActionArrow.visibility)
    }

    @Test
    fun verifyArrowOnClickNoCallToAction() {
        binding.executePendingBindings()

        binding.callToActionArrow.performClick()
        verifyAll { binding.callbacks!!.goToNextPage() }
    }

    @Test
    fun verifyArrowOnClickNoEvents() {
        binding.callToAction = CallToAction()
        binding.executePendingBindings()

        binding.callToActionArrow.performClick()
        verifyAll { binding.callbacks!!.goToNextPage() }
    }

    @Test
    fun verifyArrowColor() {
        binding.callToAction = CallToAction(controlColor = Color.GREEN)
        binding.executePendingBindings()

        assertEquals(Color.GREEN, ImageViewCompat.getImageTintList(binding.callToActionArrow)!!.defaultColor)
    }
    // endregion Arrow Tests

    @Test
    fun verifyLabel() {
        binding.callToAction = CallToAction(
            label = { Text(it, text = "Label Test", textAlign = Text.Align.START) }
        )
        binding.executePendingBindings()

        assertEquals("Label Test", binding.callToActionLabel.text.toString())
    }
}
