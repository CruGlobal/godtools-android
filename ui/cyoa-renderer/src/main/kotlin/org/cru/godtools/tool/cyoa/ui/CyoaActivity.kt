package org.cru.godtools.tool.cyoa.ui

import android.os.Bundle
import dagger.hilt.android.AndroidEntryPoint
import org.cru.godtools.base.tool.activity.MultiLanguageToolActivity
import org.cru.godtools.tool.cyoa.R
import org.cru.godtools.tool.cyoa.databinding.CyoaActivityBinding
import org.cru.godtools.tool.model.Manifest
import org.cru.godtools.tool.model.page.Page

@AndroidEntryPoint
class CyoaActivity : MultiLanguageToolActivity<CyoaActivityBinding>(R.layout.cyoa_activity, Manifest.Type.CYOA) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataModel.activeManifest.observe(this) { it?.let { showInitialPageIfNecessary(it) } }
    }

    override fun onBindingChanged() {
        super.onBindingChanged()
        setupBinding()
    }

    private fun setupBinding() {
        binding.activeLocale = dataModel.activeLocale
        binding.visibleLocales = dataModel.visibleLocales
    }

    // region Page management
    private val pageFragment get() = supportFragmentManager.primaryNavigationFragment

    private fun showInitialPageIfNecessary(manifest: Manifest) {
        if (pageFragment != null) return

        manifest.pages.firstOrNull { !it.isHidden }
            ?.let { showPage(it, false) }
    }

    private fun showPage(page: Page, addToBackStack: Boolean = true) {
    }
    // endregion Page management
}
