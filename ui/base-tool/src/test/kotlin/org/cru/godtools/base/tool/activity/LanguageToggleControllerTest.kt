package org.cru.godtools.base.tool.activity

import android.content.Context
import androidx.appcompat.R
import androidx.appcompat.view.ContextThemeWrapper
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.material.tabs.TabLayout
import io.mockk.clearMocks
import io.mockk.confirmVerified
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@Category(AndroidJUnit4::class)
class LanguageToggleControllerTest {
    private lateinit var context: Context
    private lateinit var tabLayout: TabLayout

    private lateinit var controller: LanguageToggleController

    @Before
    fun setupController() {
        context = ContextThemeWrapper(ApplicationProvider.getApplicationContext(), R.style.Theme_AppCompat)
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
        val tabListener: TabLayout.OnTabSelectedListener = mockk(relaxUnitFun = true)
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
        verify(inverse = true) { tabListener.onTabSelected(any()) }

        controller.locales = listOf(Locale.ENGLISH, Locale.GERMAN)
        assertEquals(2, tabLayout.tabCount)
        assertEquals(-1, tabLayout.selectedTabPosition)
        verify(inverse = true) { tabListener.onTabSelected(any()) }
    }

    @Test
    fun testIsUpdatingTabsValueWhenMakingStructuralChanges() {
        val tabListener = spyk(object : TabLayout.OnTabSelectedListener {
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
        verify(exactly = 1) { tabListener.onTabSelected(any()) }
        confirmVerified(tabListener)
        clearMocks(tabListener)

        // change activeLocale
        assertFalse(controller.isUpdatingTabs)
        controller.activeLocale = Locale.FRENCH
        assertFalse(controller.isUpdatingTabs)
        verify(exactly = 1) {
            tabListener.onTabSelected(any())
            tabListener.onTabUnselected(any())
        }
        confirmVerified(tabListener)
        clearMocks(tabListener)

        // change locales
        assertFalse(controller.isUpdatingTabs)
        controller.locales = listOf(Locale.ENGLISH)
        assertFalse(controller.isUpdatingTabs)
        verify(exactly = 1) { tabListener.onTabUnselected(any()) }
    }
}
