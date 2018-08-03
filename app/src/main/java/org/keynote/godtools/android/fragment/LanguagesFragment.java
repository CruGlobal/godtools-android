package org.keynote.godtools.android.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.Loader;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.annimon.stream.Stream;

import org.ccci.gto.android.common.support.v4.app.SimpleLoaderCallbacks;
import org.ccci.gto.android.common.support.v4.util.FragmentUtils;
import org.cru.godtools.R;
import org.cru.godtools.adapter.LanguagesAdapter;
import org.cru.godtools.model.Language;
import org.cru.godtools.sync.GodToolsSyncService;
import org.keynote.godtools.android.content.LanguagesLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;

public class LanguagesFragment extends BaseFragment implements LanguagesAdapter.Callbacks {
    private static final String EXTRA_PRIMARY = LanguagesFragment.class.getName() + ".PRIMARY";

    private static final int LOADER_LANGUAGES = 101;

    public interface Callbacks {
        void onLocaleSelected(@Nullable Locale locale);
    }

    @Nullable
    @BindView(R.id.languages)
    RecyclerView mLanguagesView;
    @Nullable
    private LanguagesAdapter mLanguagesAdapter;
    private MenuItem mSearchItem;

    // these properties should be treated as final and only set/modified in onCreate()
    /*final*/ boolean mPrimary = true;

    // this string is to store query result and maintain them during device rotation
    private String mQuery = "";
    private static final String KEY_SEARCH = LanguagesFragment.class.getName() + ".SEARCH";

    // this boolean value will return if the searchView is open
    private boolean mIsSearchViewOpen = false;
    private static final String KEY_IS_SEARCH_OPEN = LanguagesFragment.class.getName() + ".IS_OPEN";

    @Nullable
    private List<Language> mLanguages;

    public static Fragment newInstance(final boolean primary) {
        final Fragment fragment = new LanguagesFragment();
        final Bundle args = new Bundle();
        args.putBoolean(EXTRA_PRIMARY, primary);
        fragment.setArguments(args);
        return fragment;
    }

    /* BEGIN lifecycle */

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Allow Fragment to have its own menu
        setHasOptionsMenu(true);
        final Bundle args = getArguments();
        if (args != null) {
            mPrimary = args.getBoolean(EXTRA_PRIMARY, mPrimary);
        }
        if (savedInstanceState != null) {
            mQuery = savedInstanceState.getString(KEY_SEARCH, "");
            mIsSearchViewOpen = savedInstanceState.getBoolean(KEY_IS_SEARCH_OPEN, false);
        }
        startLoaders();
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @NonNull final ViewGroup container,
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

    void onLoadLanguages(@Nullable final List<Language> languages) {
        if (languages == null) {
            mLanguages = null;
        } else {
            mLanguages = Stream.of(languages)
                    .sorted((l1, l2) -> l1.getDisplayName().compareToIgnoreCase(l2.getDisplayName()))
                    .toList();
        }
        updateLanguagesList();
    }

    @Override
    public void onLanguageSelected(@Nullable final Locale language) {
        final Callbacks listener = FragmentUtils.getListener(this, Callbacks.class);
        if (listener != null) {
            listener.onLocaleSelected(language);
        }
    }

    @Override
    public void onDestroyView() {
        cleanupLanguagesList();
        super.onDestroyView();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_IS_SEARCH_OPEN, mSearchItem.isActionViewExpanded());
        outState.putString(KEY_SEARCH, mQuery);
    }

    //region Initialize Menu
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.fragment_language_search, menu);
        setSearchView(menu);

    }

    private void setSearchView(Menu menu) {
        // Configuring the SearchView
        mSearchItem = menu.findItem(R.id.app_bar_language_search);
        SearchView mSearchView = (SearchView) mSearchItem.getActionView();
        mSearchView.setQueryHint(getString(R.string.label_language_search));
        if (mIsSearchViewOpen) {
            mSearchItem.expandActionView();
            if (!TextUtils.isEmpty(mQuery)) {
                mSearchView.setQuery(mQuery, false);
            }
        }

        // Will listen for search event and trigger
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mQuery = query;
                updateLanguagesList();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mQuery = newText;
                updateLanguagesList();
                return false;
            }
        });
    }

    private List<Language> updateLanguageWithSearch(final String query) {
        if (mLanguages != null) {
            return Stream.of(mLanguages)
                    .filter(l -> l.getDisplayName().toLowerCase().contains(query.toLowerCase()))
                    .toList();
        } else {
            return new ArrayList<>();
        }
    }

    //endregion

    /* END lifecycle */

    private void startLoaders() {
        getLoaderManager().initLoader(LOADER_LANGUAGES, null, new LanguagesLoaderCallbacks());
    }

    @CallSuper
    protected void syncData(final boolean force) {
        super.syncData(force);
        mSyncHelper.sync(GodToolsSyncService.syncLanguages(getContext(), force));
    }

    private void setupLanguagesList() {
        if (mLanguagesView != null) {
            final Context context = mLanguagesView.getContext();
            final LinearLayoutManager layoutManager = new LinearLayoutManager(context);
            mLanguagesView.setLayoutManager(layoutManager);
            mLanguagesView.addItemDecoration(new DividerItemDecoration(context, layoutManager.getOrientation()));

            mLanguagesAdapter = new LanguagesAdapter(context);
            mLanguagesAdapter.setShowNone(!mPrimary);
            mLanguagesAdapter.setCallbacks(this);
            mLanguagesView.setAdapter(mLanguagesAdapter);
        }
    }

    private void updateLanguagesList() {
        if (mLanguagesAdapter != null) {
            mLanguagesAdapter.setSelected(mPrimary ? mPrimaryLanguage : mParallelLanguage);
            mLanguagesAdapter.setLanguages(filterLang(mLanguages));
            mLanguagesAdapter.setDisabled(mPrimary ? null : mPrimaryLanguage);
            mLanguagesAdapter.setProtected(mSettings != null ? mSettings.getProtectedLanguages() : null);
        }
    }

    // This will check to see if the language needs to be filtered by search
    private List<Language> filterLang(List<Language> languages) {
        if (TextUtils.isEmpty(mQuery)) {
            return languages;
        } else {
            return updateLanguageWithSearch(mQuery);
        }

    }

    private void cleanupLanguagesList() {
        if (mLanguagesAdapter != null) {
            mLanguagesAdapter.setCallbacks(null);
        }
        mLanguagesAdapter = null;
    }

    class LanguagesLoaderCallbacks extends SimpleLoaderCallbacks<List<Language>> {
        @Nullable
        @Override
        public Loader<List<Language>> onCreateLoader(final int id, @Nullable final Bundle args) {
            switch (id) {
                case LOADER_LANGUAGES:
                    return new LanguagesLoader(requireContext());
                default:
                    return null;
            }
        }

        @Override
        public void onLoadFinished(@NonNull final Loader<List<Language>> loader,
                                   @Nullable final List<Language> languages) {
            switch (loader.getId()) {
                case LOADER_LANGUAGES:
                    onLoadLanguages(languages);
                    break;
            }
        }
    }
}
