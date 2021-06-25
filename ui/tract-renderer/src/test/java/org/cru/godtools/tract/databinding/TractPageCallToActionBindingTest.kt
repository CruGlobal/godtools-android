package org.cru.godtools.tract.databinding

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.widget.ImageViewCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.cru.godtools.tool.model.EventId
import org.cru.godtools.tool.model.Text
import org.cru.godtools.tool.model.tract.CallToAction
import org.cru.godtools.tool.model.tract.TractPage
import org.cru.godtools.tract.R
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
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
    fun verifyArrowVisibilityLastPageWithEvents() {
        whenever(page.isLastPage) doReturn true
        whenever(callToAction.events) doReturn listOf(EventId.FOLLOWUP)
        binding.page = page
        binding.callToAction = callToAction
        binding.executePendingBindings()
        assertEquals(View.VISIBLE, binding.callToActionArrow.visibility)
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
    fun verifyArrowOnClickNoEvents() {
        binding.callToAction = callToAction
        binding.executePendingBindings()

        binding.callToActionArrow.performClick()
        verify(binding.callbacks!!).goToNextPage()
        verify(binding.controller!!, never()).sendEvents(any())
    }

    @Test
    fun verifyArrowOnClickEvents() {
        whenever(callToAction.events) doReturn listOf(EventId.FOLLOWUP)
        binding.callToAction = callToAction
        binding.executePendingBindings()

        binding.callToActionArrow.performClick()
        verify(binding.callbacks!!, never()).goToNextPage()
        verify(binding.controller!!).sendEvents(any())
    }

    @Test
    fun verifyArrowColor() {
        whenever(callToAction.controlColor) doReturn Color.GREEN
        binding.callToAction = callToAction
        binding.executePendingBindings()

        assertEquals(Color.GREEN, ImageViewCompat.getImageTintList(binding.callToActionArrow)!!.defaultColor)
    }
    // endregion Arrow Tests

    @Test
    fun verifyLabel() {
        val label = mock<Text> {
            on { text } doReturn "Label Test"
            on { textAlign } doReturn Text.Align.START
        }
        whenever(callToAction.label) doReturn label
        binding.callToAction = callToAction
        binding.executePendingBindings()

        assertEquals("Label Test", binding.callToActionLabel.text.toString())
    }
}
