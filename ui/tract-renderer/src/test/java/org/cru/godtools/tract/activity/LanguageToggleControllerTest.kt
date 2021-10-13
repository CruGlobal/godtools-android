package org.cru.godtools.tract.activity

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.material.tabs.TabLayout
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.robolectric.Robolectric

@RunWith(AndroidJUnit4::class)
class LanguageToggleControllerTest {
    private lateinit var context: Context
    private lateinit var tabLayout: TabLayout
    private lateinit var tabListener: TabLayout.OnTabSelectedListener

    private lateinit var controller: LanguageToggleController

    @Before
    fun setupController() {
        context = Robolectric.buildActivity(TractActivity::class.java).get()
        tabListener = mock()
        tabLayout = TabLayout(context)
        controller = LanguageToggleController(tabLayout)
    }

    @Test
    fun verifyCreateTabs() {
        controller.activeLocale = Locale.FRENCH
        controller.locales = listOf(Locale.ENGLISH, Locale.FRENCH)

        assertEquals(2, tabLayout.tabCount)
        assertEquals(Locale.ENGLISH, tabLayout.getTabAt(0)!!.tag)
        assertEquals(Locale.FRENCH, tabLayout.getTabAt(1)!!.tag)
        assertEquals(1, tabLayout.selectedTabPosition)
    }

    @Test
    fun verifyCreateTabsChangeLocales() {
        controller.activeLocale = Locale.FRENCH
        controller.locales = listOf(Locale.ENGLISH, Locale.FRENCH, Locale.GERMAN)
        tabLayout.addOnTabSelectedListener(tabListener)

        assertEquals(3, tabLayout.tabCount)
        assertEquals(1, tabLayout.selectedTabPosition)
        assertEquals(Locale.FRENCH, tabLayout.getTabAt(1)!!.tag)

        controller.locales = listOf(Locale.FRENCH, Locale.ENGLISH)
        assertEquals(2, tabLayout.tabCount)
        assertEquals(0, tabLayout.selectedTabPosition)
        assertEquals(Locale.FRENCH, tabLayout.getTabAt(0)!!.tag)
        verify(tabListener, never()).onTabSelected(any())

        controller.locales = listOf(Locale.ENGLISH, Locale.GERMAN)
        assertEquals(2, tabLayout.tabCount)
        assertEquals(-1, tabLayout.selectedTabPosition)
        verify(tabListener, never()).onTabSelected(any())
    }

    @Test
    fun testIsUpdatingTabsValueWhenMakingStructuralChanges() {
        tabListener = spy(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) = assertTrue(controller.isUpdatingTabs)
            override fun onTabUnselected(tab: TabLayout.Tab?) = assertTrue(controller.isUpdatingTabs)
            override fun onTabReselected(tab: TabLayout.Tab?) = assertTrue(controller.isUpdatingTabs)
        })
        tabLayout.addOnTabSelectedListener(tabListener)

        // initial tabs
        assertFalse(controller.isUpdatingTabs)
        controller.activeLocale = Locale.ENGLISH
        controller.locales = listOf(Locale.ENGLISH, Locale.FRENCH)
        assertFalse(controller.isUpdatingTabs)
        verify(tabListener).onTabSelected(any())
        verifyNoMoreInteractions(tabListener)
        clearInvocations(tabListener)

        // change activeLocale
        assertFalse(controller.isUpdatingTabs)
        controller.activeLocale = Locale.FRENCH
        assertFalse(controller.isUpdatingTabs)
        verify(tabListener).onTabSelected(any())
        verify(tabListener).onTabUnselected(any())
        verifyNoMoreInteractions(tabListener)
        clearInvocations(tabListener)

        // change locales
        assertFalse(controller.isUpdatingTabs)
        controller.locales = listOf(Locale.ENGLISH)
        assertFalse(controller.isUpdatingTabs)
        verify(tabListener, never()).onTabSelected(any())
        verify(tabListener).onTabUnselected(any())
        verifyNoMoreInteractions(tabListener)
    }
}
