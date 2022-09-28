package org.cru.godtools.tool.tract.databinding

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.R
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.widget.ImageViewCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cru.godtools.tool.model.Text
import org.cru.godtools.tool.model.tract.CallToAction
import org.cru.godtools.tool.model.tract.TractPage
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.Robolectric

@RunWith(AndroidJUnit4::class)
class TractPageCallToActionBindingTest {
    private lateinit var binding: TractPageCallToActionBinding

    private lateinit var page: TractPage
    private lateinit var callToAction: CallToAction

    @Before
    fun setup() {
        val activity = Robolectric.buildActivity(AppCompatActivity::class.java).get()
        val context = ContextThemeWrapper(activity, R.style.Theme_AppCompat)

        binding = TractPageCallToActionBinding.inflate(LayoutInflater.from(context), null, false)
        binding.lifecycleOwner = activity
        binding.callbacks = mock()
        binding.controller = mock()
        binding.executePendingBindings()

        page = mock()
        callToAction = mock()
    }

    // region Arrow Tests
    @Test
    fun verifyArrowVisibilityLastPage() {
        whenever(page.isLastPage).thenReturn(true)
        binding.page = page
        binding.executePendingBindings()
        assertEquals(View.GONE, binding.callToActionArrow.visibility)
    }

    @Test
    fun verifyArrowVisibilityNotLastPage() {
        whenever(page.isLastPage).thenReturn(false)
        binding.page = page
        binding.executePendingBindings()
        assertEquals(View.VISIBLE, binding.callToActionArrow.visibility)
    }

    @Test
    fun verifyArrowOnClickNoCallToAction() {
        binding.executePendingBindings()

        binding.callToActionArrow.performClick()
        verify(binding.callbacks!!).goToNextPage()
        verify(binding.controller!!, never()).sendEvents(any())
    }

    @Test
    @Ignore("AGP 7.3.0-alpha03 broke using Data Binding classes from library unit tests.")
    fun verifyArrowOnClickNoEvents() {
        binding.callToAction = callToAction
        binding.executePendingBindings()

        binding.callToActionArrow.performClick()
        verify(binding.callbacks!!).goToNextPage()
        verify(binding.controller!!, never()).sendEvents(any())
    }

    @Test
    @Ignore("AGP 7.3.0-alpha03 broke using Data Binding classes from library unit tests.")
    fun verifyArrowColor() {
        binding.callToAction = CallToAction(controlColor = Color.GREEN)
        binding.executePendingBindings()

        assertEquals(Color.GREEN, ImageViewCompat.getImageTintList(binding.callToActionArrow)!!.defaultColor)
    }
    // endregion Arrow Tests

    @Test
    @Ignore("AGP 7.3.0-alpha03 broke using Data Binding classes from library unit tests.")
    fun verifyLabel() {
        binding.callToAction = CallToAction(
            label = { Text(it, text = "Label Test", textAlign = Text.Align.START) }
        )
        binding.executePendingBindings()

        assertEquals("Label Test", binding.callToActionLabel.text.toString())
    }
}
