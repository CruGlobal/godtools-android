package org.cru.godtools.tool.cyoa.ui

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.annotation.VisibleForTesting
import androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
import androidx.fragment.app.commit
import dagger.hilt.android.AndroidEntryPoint
import org.ccci.gto.android.common.androidx.fragment.app.backStackEntries
import org.ccci.gto.android.common.androidx.fragment.app.hasPendingActions
import org.cru.godtools.base.tool.activity.MultiLanguageToolActivity
import org.cru.godtools.base.tool.model.Event
import org.cru.godtools.tool.cyoa.R
import org.cru.godtools.tool.cyoa.databinding.CyoaActivityBinding
import org.cru.godtools.tool.model.Manifest
import org.cru.godtools.tool.model.page.CardCollectionPage
import org.cru.godtools.tool.model.page.ContentPage
import org.cru.godtools.tool.model.page.Page
import org.cru.godtools.tool.model.tips.Tip
import org.cru.godtools.tool.tips.ShowTipCallback
import org.cru.godtools.tool.tips.ui.TipBottomSheetDialogFragment

@AndroidEntryPoint
class CyoaActivity :
    MultiLanguageToolActivity<CyoaActivityBinding>(R.layout.cyoa_activity, Manifest.Type.CYOA),
    CyoaPageFragment.InvalidPageListener,
    ShowTipCallback {
    // region Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // track this tool open
        if (savedInstanceState == null) dataModel.toolCode.value?.let { trackToolOpen(it, Manifest.Type.CYOA) }

        dataModel.activeManifest.observe(this) { it?.let { showInitialPageIfNecessary(it) } }
    }

    override fun onBindingChanged() {
        super.onBindingChanged()
        setupBinding()
        updatePageInsets()
    }

    override fun onOptionsItemSelected(item: MenuItem) = when {
        item.itemId == android.R.id.home && navigateToParentPage() -> true
        else -> super.onOptionsItemSelected(item)
    }

    override fun onInvalidPage(fragment: CyoaPageFragment<*, *>, page: Page?) {
        if (fragment !== pageFragment) return
        when {
            page != null -> showPage(page, true)
            supportFragmentManager.backStackEntryCount == 0 -> finish()
            else -> supportFragmentManager.popBackStack()
        }
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
    override val toolbar get() = binding.appbar
    override val languageToggle get() = binding.languageToggle

    private val pageInsets by viewModels<PageInsets>()

    private fun updatePageInsets() {
        // Not an ideal way to set this, but it works for now
        // TODO: investigate how WindowInsets are propagated to child views,
        //       it might provide a better way of handling this.
        pageInsets.top = binding.appbar.layoutParams.height
    }

    // region Training Tips
    override fun showTip(tip: Tip) {
        TipBottomSheetDialogFragment.create(tip)?.show(supportFragmentManager, null)
    }
    // endregion Training Tips
    // endregion UI

    // region Page management
    @VisibleForTesting
    internal val pageFragment
        get() = with(supportFragmentManager) {
            if (hasPendingActions) executePendingTransactions()
            primaryNavigationFragment as? CyoaPageFragment<*, *>
        }
    private val activePage get() = pageFragment?.page?.value

    private fun showInitialPageIfNecessary(manifest: Manifest) {
        if (pageFragment != null) return

        manifest.pages.firstOrNull { !it.isHidden }
            ?.let { showPage(it, true) }
    }

    private fun checkForPageEvent(event: Event) {
        val pageFragment = pageFragment

        val dismissCurrentPage = pageFragment?.page?.value?.dismissListeners?.contains(event.id) == true
        val newPage = dataModel.activeManifest.value?.pages?.firstOrNull { it.listeners.contains(event.id) }

        // trigger any page content listeners if we aren't dismissing the current page
        if (!dismissCurrentPage) pageFragment?.onContentEvent(event)

        // dismiss/show pages as necessary
        when {
            newPage != null -> showPage(newPage, replaceCurrentPage = dismissCurrentPage)
            dismissCurrentPage -> when {
                supportFragmentManager.backStackEntryCount > 0 -> supportFragmentManager.popBackStack()
                pageFragment != null -> supportFragmentManager.commit { remove(pageFragment) }
            }
        }
    }

    @VisibleForTesting
    internal fun navigateToParentPage(): Boolean {
        val parent = activePage?.parentPage
        if (parent != null) {
            with(supportFragmentManager) {
                // find closest ancestor that is currently in the back stack
                val seen = mutableSetOf<String>()
                var ancestor = parent
                while (ancestor != null && !seen.contains(ancestor.id)) {
                    val ancestorId = ancestor.id
                    if (backStackEntries.any { it.name == ancestorId }) {
                        popBackStack(ancestorId, POP_BACK_STACK_INCLUSIVE)
                        if (ancestor !== parent) showPage(parent)
                        return true
                    }

                    seen += ancestorId
                    ancestor = ancestor.parentPage
                }

                // otherwise pop entire backstack and show parent page as the root page
                if (backStackEntryCount > 0) popBackStack(getBackStackEntryAt(0).id, POP_BACK_STACK_INCLUSIVE)
                showPage(parent, true)
                return true
            }
        }
        return false
    }

    @VisibleForTesting
    internal fun showPage(page: Page, replaceCurrentPage: Boolean = false) {
        val fragment = when (page) {
            is CardCollectionPage -> CyoaCardCollectionPageFragment(page.id)
            is ContentPage -> CyoaContentPageFragment(page.id)
            else -> return
        }
        val fm = supportFragmentManager
        fm.commit {
            setReorderingAllowed(true)
            replace(R.id.page, fragment)
            setPrimaryNavigationFragment(fragment)
            pageFragment?.let {
                if (replaceCurrentPage) {
                    val backStackSize = fm.backStackEntryCount
                    if (backStackSize > 0) {
                        addToBackStack(fm.getBackStackEntryAt(backStackSize - 1).name)
                        fm.popBackStack()
                    }
                } else {
                    addToBackStack(it.pageId)
                }
            }
        }
    }
    // endregion Page management
}
