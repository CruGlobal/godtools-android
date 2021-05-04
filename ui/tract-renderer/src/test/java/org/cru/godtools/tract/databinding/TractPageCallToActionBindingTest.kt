package org.cru.godtools.tract.databinding

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.widget.ImageViewCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.cru.godtools.tract.R
import org.cru.godtools.xml.model.Event
import org.cru.godtools.xml.model.Manifest
import org.cru.godtools.xml.model.Text
import org.cru.godtools.xml.model.tract.CallToAction
import org.cru.godtools.xml.model.tract.TractPage
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric

@RunWith(AndroidJUnit4::class)
class TractPageCallToActionBindingTest {
    private lateinit var binding: TractPageCallToActionBinding

    private lateinit var page: TractPage

    @Before
    fun setup() {
        val activity = Robolectric.buildActivity(AppCompatActivity::class.java).get()
        val context = ContextThemeWrapper(activity, R.style.Theme_AppCompat)

        binding = TractPageCallToActionBinding.inflate(LayoutInflater.from(context), null, false)
        binding.lifecycleOwner = activity
        binding.callbacks = mock()
        binding.controller = mock()
        binding.executePendingBindings()

        page = spy(TractPage(Manifest(), 0))
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
        whenever(page.isLastPage).thenReturn(true)
        binding.page = page
        binding.callToAction = CallToAction(page, events = setOf(Event.Id.FOLLOWUP_EVENT))
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
        binding.callToAction = CallToAction(page)
        binding.executePendingBindings()

        binding.callToActionArrow.performClick()
        verify(binding.callbacks!!).goToNextPage()
        verify(binding.controller!!, never()).sendEvents(any())
    }

    @Test
    fun verifyArrowOnClickEvents() {
        binding.callToAction = CallToAction(page, events = setOf(Event.Id.FOLLOWUP_EVENT))
        binding.executePendingBindings()

        binding.callToActionArrow.performClick()
        verify(binding.callbacks!!, never()).goToNextPage()
        verify(binding.controller!!).sendEvents(any())
    }

    @Test
    fun verifyArrowColor() {
        binding.callToAction = CallToAction(page, controlColor = Color.GREEN)
        binding.executePendingBindings()

        assertEquals(Color.GREEN, ImageViewCompat.getImageTintList(binding.callToActionArrow)!!.defaultColor)
    }
    // endregion Arrow Tests

    @Test
    fun verifyLabel() {
        binding.callToAction = CallToAction(page, label = { Text(it, text = "Label Test") })
        binding.executePendingBindings()

        assertEquals("Label Test", binding.callToActionLabel.text.toString())
    }
}
