package org.cru.godtools.ui.languages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import org.ccci.gto.android.common.support.v4.util.FragmentUtils
import org.cru.godtools.R
import org.cru.godtools.fragment.BasePlatformFragment
import org.cru.godtools.sync.syncLanguages
import splitties.fragmentargs.argOrDefault
import java.util.Locale

class LanguagesFragment() : BasePlatformFragment(), LocaleSelectedListener {
    constructor(primary: Boolean) : this() {
        isPrimary = primary
    }

    private var isPrimary by argOrDefault(true)

    // region Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        setupViewModel()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_language_search, menu)
        menu.setupSearchView()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_languages, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupLanguagesList()
    }

    override fun onUpdatePrimaryLanguage() {
        updateLanguagesList()
    }

    override fun onUpdateParallelLanguage() {
        updateLanguagesList()
    }

    override fun onLocaleSelected(locale: Locale?) {
        FragmentUtils.getListener(this, LocaleSelectedListener::class.java)?.onLocaleSelected(locale)
    }

    override fun onDestroyOptionsMenu() {
        cleanupSearchView()
        super.onDestroyOptionsMenu()
    }
    // endregion Lifecycle

    // region ViewModel
    private val viewModel: LanguagesFragmentViewModel by viewModels()

    private fun setupViewModel() {
        viewModel.showNone.value = !isPrimary
    }
    // endregion ViewModel

    // region Search Action Item
    private var searchItem: MenuItem? = null
    private val searchView get() = searchItem?.actionView as? SearchView

    private fun Menu.setupSearchView() {
        searchItem = findItem(R.id.action_search)
        searchItem?.apply {
            if (viewModel.isSearchViewOpen) expandActionView()
            setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
                override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                    viewModel.isSearchViewOpen = true
                    return true
                }

                override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                    viewModel.isSearchViewOpen = false
                    return true
                }
            })
        }

        // Configuring the SearchView
        searchView?.apply {
            queryHint = getString(R.string.label_language_search)
            setQuery(viewModel.query.value, false)
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    viewModel.query.value = query
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    viewModel.query.value = newText
                    return true
                }
            })
        }
    }

    private fun cleanupSearchView() {
        searchView?.setOnQueryTextListener(null)
        searchItem?.setOnActionExpandListener(null)
        searchItem = null
    }
    // endregion Search Action Item

    @CallSuper
    override fun syncData(force: Boolean) {
        super.syncData(force)
        syncHelper.sync(syncLanguages(requireContext(), force))
    }

    // region Languages List
    @JvmField
    @BindView(R.id.languages)
    var languagesView: RecyclerView? = null
    private val languagesAdapter by lazy {
        LanguagesAdapter().also {
            it.callbacks = this
            viewModel.languages.observe(this, it)
        }
    }

    private fun setupLanguagesList() {
        languagesView?.apply {
            adapter = languagesAdapter
            (layoutManager as? LinearLayoutManager)?.let {
                addItemDecoration(DividerItemDecoration(context, it.orientation))
            }
        }
    }

    private fun updateLanguagesList() {
        languagesAdapter.selected.set(if (isPrimary) primaryLanguage else parallelLanguage)
        languagesAdapter.setDisabled(*(if (isPrimary) emptyArray() else arrayOf(primaryLanguage)))
    }
    // endregion Languages List
}
