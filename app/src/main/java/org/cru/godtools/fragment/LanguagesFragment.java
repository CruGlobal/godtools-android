package org.cru.godtools.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import org.ccci.gto.android.common.support.v4.util.FragmentUtils;
import org.cru.godtools.R;
import org.cru.godtools.sync.GodToolsSyncServiceKt;
import org.cru.godtools.ui.languages.LanguagesAdapter;
import org.cru.godtools.ui.languages.LanguagesFragmentViewModel;
import org.cru.godtools.ui.languages.LocaleSelectedListener;

import java.util.Locale;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;

public class LanguagesFragment extends BasePlatformFragment implements LocaleSelectedListener {
    private static final String EXTRA_PRIMARY = LanguagesFragment.class.getName() + ".PRIMARY";
    private static final String EXTRA_SEARCH_OPEN = LanguagesFragment.class.getName() + ".SEARCH_OPEN";

    public interface Callbacks {
        void onLocaleSelected(@Nullable Locale locale);
    }

    @Nullable
    @BindView(R.id.languages)
    RecyclerView mLanguagesView;
    @Nullable
    private LanguagesAdapter mLanguagesAdapter;
    @Nullable
    private MenuItem mSearchItem;
    @Nullable
    private SearchView mSearchView;

    // these properties should be treated as final and only set/modified in onCreate()
    /*final*/ boolean mPrimary = true;

    // search related properties
    private boolean mIsSearchViewOpen = false;

    public static Fragment newInstance(final boolean primary) {
        final Fragment fragment = new LanguagesFragment();
        final Bundle args = new Bundle();
        args.putBoolean(EXTRA_PRIMARY, primary);
        fragment.setArguments(args);
        return fragment;
    }

    // region Lifecycle
    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        final Bundle args = getArguments();
        if (args != null) {
            mPrimary = args.getBoolean(EXTRA_PRIMARY, mPrimary);
        }

        if (savedInstanceState != null) {
            mIsSearchViewOpen = savedInstanceState.getBoolean(EXTRA_SEARCH_OPEN, mIsSearchViewOpen);
        }

        setupDataModel();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull final Menu menu, final @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_language_search, menu);
        mSearchItem = menu.findItem(R.id.action_search);
        setupSearchMenu();
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_languages, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupLanguagesList();
    }

    @Override
    protected void onUpdatePrimaryLanguage() {
        updateLanguagesList();
    }

    @Override
    protected void onUpdateParallelLanguage() {
        updateLanguagesList();
    }

    @Override
    public void onLocaleSelected(@Nullable final Locale language) {
        final Callbacks listener = FragmentUtils.getListener(this, Callbacks.class);
        if (listener != null) {
            listener.onLocaleSelected(language);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(EXTRA_SEARCH_OPEN,
                            mSearchItem != null ? mSearchItem.isActionViewExpanded() : mIsSearchViewOpen);
    }

    @Override
    public void onDestroyOptionsMenu() {
        cleanupSearchMenu();
        super.onDestroyOptionsMenu();
    }

    @Override
    public void onDestroyView() {
        cleanupLanguagesList();
        super.onDestroyView();
    }
    // endregion Lifecycle

    // region ViewModel
    private LanguagesFragmentViewModel mViewModel;

    private void setupDataModel() {
        mViewModel =
                ViewModelProviders.of(this, getDefaultViewModelProviderFactory()).get(LanguagesFragmentViewModel.class);
        mViewModel.getShowNone().setValue(!mPrimary);
    }
    // endregion ViewModel

    // region Search Action Item

    private void setupSearchMenu() {
        if (mSearchItem != null) {
            mSearchView = (SearchView) mSearchItem.getActionView();
            if (mIsSearchViewOpen) {
                mSearchItem.expandActionView();
            }
        }

        // Configuring the SearchView
        if (mSearchView != null) {
            mSearchView.setQueryHint(getString(R.string.label_language_search));
            if (!TextUtils.isEmpty(mViewModel.getQuery().getValue())) {
                mSearchView.setQuery(mViewModel.getQuery().getValue(), false);
            }

            // Will listen for search event and trigger
            mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(@Nullable final String query) {
                    mViewModel.getQuery().setValue(query);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(@Nullable final String newText) {
                    mViewModel.getQuery().setValue(newText);
                    return true;
                }
            });
        }
    }

    private void cleanupSearchMenu() {
        if (mSearchView != null) {
            mSearchView.setOnQueryTextListener(null);
        }
        if (mSearchItem != null) {
            mIsSearchViewOpen = mSearchItem.isActionViewExpanded();
        }
        mSearchView = null;
        mSearchItem = null;
    }

    // endregion Search Action Item

    @CallSuper
    protected void syncData(final boolean force) {
        super.syncData(force);
        getSyncHelper().sync(GodToolsSyncServiceKt.syncLanguages(requireContext(), force));
    }

    // region Languages List
    private void setupLanguagesList() {
        if (mLanguagesView != null) {
            final RecyclerView.LayoutManager layoutManager = mLanguagesView.getLayoutManager();
            if (layoutManager instanceof LinearLayoutManager) {
                mLanguagesView.addItemDecoration(new DividerItemDecoration(
                        mLanguagesView.getContext(), ((LinearLayoutManager) layoutManager).getOrientation()
                ));
            }

            mLanguagesAdapter = new LanguagesAdapter();
            mLanguagesAdapter.setCallbacks(this);
            mViewModel.getLanguages().observe(getViewLifecycleOwner(), mLanguagesAdapter);
            mLanguagesView.setAdapter(mLanguagesAdapter);
        }
    }

    void updateLanguagesList() {
        if (mLanguagesAdapter != null) {
            mLanguagesAdapter.getSelected().set(mPrimary ? getPrimaryLanguage() : getParallelLanguage());
            mLanguagesAdapter.setDisabled(mPrimary ? null : getPrimaryLanguage());
        }
    }

    private void cleanupLanguagesList() {
        if (mLanguagesAdapter != null) {
            mLanguagesAdapter.setCallbacks(null);
        }
        mLanguagesAdapter = null;
    }
    // endregion Languages List
}
