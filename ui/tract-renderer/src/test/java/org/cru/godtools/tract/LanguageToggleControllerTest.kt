package org.cru.godtools.tract

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.material.tabs.TabLayout
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import org.cru.godtools.tract.activity.TractActivity
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import java.util.Locale

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
}
