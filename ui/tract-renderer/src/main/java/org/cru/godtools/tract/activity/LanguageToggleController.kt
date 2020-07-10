package org.cru.godtools.tract.activity

import com.google.android.material.tabs.TabLayout
import org.ccci.gto.android.common.material.tabs.setBackgroundTint
import org.cru.godtools.base.util.getDisplayName
import org.cru.godtools.model.Language
import org.cru.godtools.xml.model.Manifest
import org.cru.godtools.xml.model.navBarControlColor
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
    var languages: Map<Locale, Language> = emptyMap()
        set(value) {
            val changed = value != field
            field = value
            if (changed) configureTabs()
        }

    private fun createTabs() {
        // if the selected locale is no longer valid, we need to remove all tabs to prevent an onTabSelected callback
        val selected = tabs.getTabAt(tabs.selectedTabPosition)?.tag as? Locale
        if (selected != null && !locales.contains(selected)) tabs.removeAllTabs()

        // update tabs
        locales.forEachIndexed { i, locale ->
            // if this is the selected locale, reposition the selected tab
            if (locale == selected) while (tabs.selectedTabPosition > i) tabs.removeTabAt(i)

            // insert a new tab if the current tab isn't correct
            if (tabs.getTabAt(i)?.tag != locale) tabs.addTab(tabs.newTab().setTag(locale), i, locale == activeLocale)
        }

        // remove excess tabs
        while (tabs.tabCount > locales.size) tabs.removeTabAt(locales.size)
        configureTabs()
    }

    private fun configureTabs() {
        val controlColor = activeManifest.navBarControlColor
        tabs.forEachTab { tab ->
            val locale = tab.tag as? Locale

            tab.setBackgroundTint(controlColor)
            tab.text = languages[locale]?.getDisplayName(tabs.context) ?: locale?.getDisplayName(tabs.context)
        }
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
