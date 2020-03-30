package org.cru.godtools.ui.languages

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.annotation.CallSuper
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.android.support.AndroidSupportInjection
import org.ccci.gto.android.common.androidx.lifecycle.dagger.viewmodel.DaggerSavedStateViewModelProviderFactory
import org.ccci.gto.android.common.support.v4.util.FragmentUtils
import org.cru.godtools.R
import org.cru.godtools.databinding.LanguagesFragmentBinding
import org.cru.godtools.fragment.BasePlatformFragment
import org.cru.godtools.sync.syncLanguages
import splitties.fragmentargs.argOrDefault
import java.util.Locale
import javax.inject.Inject

class LanguagesFragment() : BasePlatformFragment<LanguagesFragmentBinding>(R.layout.languages_fragment),
    LocaleSelectedListener {
    constructor(primary: Boolean) : this() {
        isPrimary = primary
    }

    private var isPrimary by argOrDefault(true)

    // region Lifecycle
    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

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

    override fun onBindingCreated(binding: LanguagesFragmentBinding, savedInstanceState: Bundle?) {
        super.onBindingCreated(binding, savedInstanceState)
        binding.setupLanguagesList()
    }

    override fun onUpdatePrimaryLanguage() {
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

    // region ViewModelProvider.Factory
    @Inject
    internal lateinit var viewModelProviderFactory: DaggerSavedStateViewModelProviderFactory
    private val defaultViewModelProvider by lazy { viewModelProviderFactory.create(this, arguments) }

    override fun getDefaultViewModelProviderFactory() = defaultViewModelProvider
    // endregion ViewModelProvider.Factory

    // region ViewModel
    private val viewModel: LanguagesFragmentViewModel by viewModels()

    private fun setupViewModel() {
        viewModel.isPrimary.value = isPrimary
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
        syncHelper.sync(requireContext().syncLanguages(force))
    }

    // region Languages List
    private val languagesAdapter by lazy {
        LanguagesAdapter(this, viewModel.selectedLanguage).also {
            it.callbacks = this
            viewModel.languages.observe(this, it)
        }
    }

    private fun LanguagesFragmentBinding.setupLanguagesList() {
        languages.apply {
            adapter = languagesAdapter
            (layoutManager as? LinearLayoutManager)?.let {
                addItemDecoration(DividerItemDecoration(context, it.orientation))
            }
        }
        updateLanguagesList()
    }

    private fun updateLanguagesList() {
        languagesAdapter.setDisabled(*(if (isPrimary) emptyArray() else arrayOf(primaryLanguage)))
    }
    // endregion Languages List
}
