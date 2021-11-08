package org.cru.godtools.tool.cyoa.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.annotation.VisibleForTesting
import androidx.fragment.app.commit
import dagger.hilt.android.AndroidEntryPoint
import org.cru.godtools.base.tool.activity.MultiLanguageToolActivity
import org.cru.godtools.base.tool.model.Event
import org.cru.godtools.tool.cyoa.R
import org.cru.godtools.tool.cyoa.databinding.CyoaActivityBinding
import org.cru.godtools.tool.model.Manifest
import org.cru.godtools.tool.model.page.Page

@AndroidEntryPoint
class CyoaActivity : MultiLanguageToolActivity<CyoaActivityBinding>(R.layout.cyoa_activity, Manifest.Type.CYOA) {
    // region Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataModel.activeManifest.observe(this) { it?.let { showInitialPageIfNecessary(it) } }
    }

    override fun onBindingChanged() {
        super.onBindingChanged()
        setupBinding()
        updatePageInsets()
    }

    override fun onContentEvent(event: Event) {
        checkForPageEvent(event)
    }
    // endregion Lifecycle

    private fun setupBinding() {
        binding.activeLocale = dataModel.activeLocale
        binding.visibleLocales = dataModel.visibleLocales
    }

    // region UI
    private val pageInsets by viewModels<PageInsets>()

    private fun updatePageInsets() {
        // Not an ideal way to set this, but it works for now
        // TODO: investigate how WindowInsets are propagated to child views,
        //       it might provide a better way of handling this.
        pageInsets.top = binding.appbar.layoutParams.height
    }
    // endregion UI

    // region Page management
    @VisibleForTesting
    internal val pageFragment
        get() = with(supportFragmentManager) {
            executePendingTransactions()
            primaryNavigationFragment as? CyoaPageFragment
        }

    private fun showInitialPageIfNecessary(manifest: Manifest) {
        if (pageFragment != null) return

        manifest.pages.firstOrNull { !it.isHidden }
            ?.let { showPage(it, false) }
    }

    private fun checkForPageEvent(event: Event) {
        val pageFragment = pageFragment

        val dismissCurrentPage = pageFragment?.page?.value?.dismissListeners?.contains(event.id) == true
        val newPage = dataModel.activeManifest.value?.pages?.firstOrNull { it.listeners.contains(event.id) }

        // trigger any page content listeners if we aren't dismissing the current page
        if (!dismissCurrentPage) pageFragment?.onContentEvent(event)

        // dismiss/show pages as necessary
        when {
            newPage != null -> showPage(newPage, addCurrentPageToBackStack = !dismissCurrentPage)
            dismissCurrentPage -> when {
                supportFragmentManager.backStackEntryCount > 0 -> supportFragmentManager.popBackStack()
                pageFragment != null -> supportFragmentManager.commit { remove(pageFragment) }
            }
        }
    }

    private fun showPage(page: Page, addCurrentPageToBackStack: Boolean = true) {
        supportFragmentManager.commit {
            val fragment = CyoaPageFragment(page.id)
            setReorderingAllowed(true)
            if (addCurrentPageToBackStack) pageFragment?.let { addToBackStack(it.pageId) }
            replace(R.id.page, fragment)
            setPrimaryNavigationFragment(fragment)
        }
    }
    // endregion Page management
}
