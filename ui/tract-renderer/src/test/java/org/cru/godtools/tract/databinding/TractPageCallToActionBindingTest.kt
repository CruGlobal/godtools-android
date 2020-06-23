package org.cru.godtools.tract.databinding

import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.cru.godtools.base.model.Event
import org.cru.godtools.tract.R
import org.cru.godtools.xml.model.CallToAction
import org.cru.godtools.xml.model.Manifest
import org.cru.godtools.xml.model.Page
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric

@RunWith(AndroidJUnit4::class)
class TractPageCallToActionBindingTest {
    private lateinit var binding: TractPageCallToActionBinding

    private lateinit var page: Page

    @Before
    fun setup() {
        val activity = Robolectric.buildActivity(AppCompatActivity::class.java).get()
        val context = ContextThemeWrapper(activity, R.style.Theme_AppCompat)

        binding = TractPageCallToActionBinding.inflate(LayoutInflater.from(context), null, false)
        binding.lifecycleOwner = activity
        binding.callbacks = mock()
        binding.controller = mock()
        binding.executePendingBindings()

        page = spy(Page(Manifest(), 0))
    }

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
}
