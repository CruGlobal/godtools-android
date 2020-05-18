package org.cru.godtools.tract

import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.setBackgroundTint
import org.cru.godtools.base.util.getDisplayName
import org.cru.godtools.xml.model.Manifest
import java.util.Locale

class LanguageToggleController(private val tabs: TabLayout) {
    var activeLocale: Locale? = null
        set(value) {
            val changed = value != field
            field = value
            if (changed) updateSelectedTab()
        }
    var activeManifest: Manifest? = null
        set(value) {
            val changed = value != field
            field = value
            if (changed) configureTabs()
        }
    var locales: List<Locale> = emptyList()
        set(value) {
            val changed = value != field
            field = value
            if (changed) createTabs()
        }

    private fun createTabs() {
        val selected = tabs.getTabAt(tabs.selectedTabPosition)?.tag as? Locale
        tabs.removeAllTabs()
        locales.forEach { tabs.addTab(tabs.newTab().setTag(it), selected == it) }
        configureTabs()
    }

    private fun configureTabs() {
        val controlColor = Manifest.getNavBarControlColor(activeManifest)
        tabs.forEachTab { tab ->
            val locale = tab.tag as? Locale

            tab.setBackgroundTint(controlColor)
            tab.text = locale?.getDisplayName(tabs.context)
        }
        updateSelectedTab()
    }

    private fun updateSelectedTab() {
        tabs.forEachTab { tab ->
            val locale = tab.tag as? Locale
            if (activeLocale != null && locale == activeLocale) {
                if (!tab.isSelected) tab.select()
                return
            } else {
                if (tab.isSelected) tabs.selectTab(null)
            }
        }
    }
}

private inline fun TabLayout.forEachTab(block: (TabLayout.Tab) -> Unit) {
    for (i in 0 until tabCount) block(getTabAt(i)!!)
}
