package org.cru.godtools.fragment;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.google.common.collect.ImmutableList;

import org.ccci.gto.android.common.support.v4.app.SimpleLoaderCallbacks;
import org.ccci.gto.android.common.support.v4.util.FragmentUtils;
import org.cru.godtools.R;
import org.cru.godtools.adapter.LanguagesAdapter;
import org.cru.godtools.content.LanguagesLoader;
import org.cru.godtools.model.Language;
import org.cru.godtools.sync.GodToolsSyncServiceKt;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;

public class LanguagesFragment extends BasePlatformFragment implements LanguagesAdapter.Callbacks {
    private static final String EXTRA_PRIMARY = LanguagesFragment.class.getName() + ".PRIMARY";
    private static final String EXTRA_SEARCH = LanguagesFragment.class.getName() + ".SEARCH";
    private static final String EXTRA_SEARCH_OPEN = LanguagesFragment.class.getName() + ".SEARCH_OPEN";

    private static final int LOADER_LANGUAGES = 101;

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

    @Nullable
    private SortedMap<String, Language> mLanguages;

    // search related properties
    private boolean mIsSearchViewOpen = false;
    String mQuery = "";

    public static Fragment newInstance(final boolean primary) {
        final Fragment fragment = new LanguagesFragment();
        final Bundle args = new Bundle();
        args.putBoolean(EXTRA_PRIMARY, primary);
        fragment.setArguments(args);
        return fragment;
    }

    // region Lifecycle Events

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        final Bundle args = getArguments();
        if (args != null) {
            mPrimary = args.getBoolean(EXTRA_PRIMARY, mPrimary);
        }

        if (savedInstanceState != null) {
            mQuery = savedInstanceState.getString(EXTRA_SEARCH, mQuery);
            mIsSearchViewOpen = savedInstanceState.getBoolean(EXTRA_SEARCH_OPEN, mIsSearchViewOpen);
        }

        startLoaders();
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_language_search, menu);
        mSearchItem = menu.findItem(R.id.action_search);
        setupSearchMenu();
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
                    .collect(Collectors.toMap(l -> l.getDisplayName(getContext()), l -> l, (l1, l2) -> l1,
                                              () -> new TreeMap<>(String::compareToIgnoreCase)));
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
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(EXTRA_SEARCH_OPEN,
                            mSearchItem != null ? mSearchItem.isActionViewExpanded() : mIsSearchViewOpen);
        outState.putString(EXTRA_SEARCH, mQuery);
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

    // endregion Lifecycle Events

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
            if (!TextUtils.isEmpty(mQuery)) {
                mSearchView.setQuery(mQuery, false);
            }

            // Will listen for search event and trigger
            mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(@Nullable final String query) {
                    mQuery = query;
                    updateLanguagesList();
                    return true;
                }

                @Override
                public boolean onQueryTextChange(@Nullable final String newText) {
                    mQuery = newText;
                    updateLanguagesList();
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

    private void startLoaders() {
        getLoaderManager().initLoader(LOADER_LANGUAGES, null, new LanguagesLoaderCallbacks());
    }

    @CallSuper
    protected void syncData(final boolean force) {
        super.syncData(force);
        getSyncHelper().sync(GodToolsSyncServiceKt.syncLanguages(requireContext(), force));
    }

    // region Languages List

    private void setupLanguagesList() {
        if (mLanguagesView != null) {
            final Context context = mLanguagesView.getContext();
            final LinearLayoutManager layoutManager = new LinearLayoutManager(context);
            mLanguagesView.setLayoutManager(layoutManager);
            mLanguagesView.addItemDecoration(new DividerItemDecoration(context, layoutManager.getOrientation()));

            mLanguagesAdapter = new LanguagesAdapter(!mPrimary);
            mLanguagesAdapter.setCallbacks(this);
            mLanguagesView.setAdapter(mLanguagesAdapter);
        }
    }

    void updateLanguagesList() {
        if (mLanguagesAdapter != null) {
            mLanguagesAdapter.setSelected(mPrimary ? getPrimaryLanguage() : getParallelLanguage());
            mLanguagesAdapter.setLanguages(filterLangs(mLanguages, mQuery));
            mLanguagesAdapter.setDisabled(mPrimary ? null : getPrimaryLanguage());
        }
    }

    @Nullable
    private List<Language> filterLangs(@Nullable final Map<String, Language> languages, @Nullable final String query) {
        // short-circuit if there aren't any languages to filter
        if (languages == null || languages.isEmpty()) {
            return ImmutableList.of();
        }

        // short-circuit if there isn't a query
        if (TextUtils.isEmpty(query)) {
            return ImmutableList.copyOf(languages.values());
        }

        // otherwise filter the list of languages based on the query
        return Stream.of(languages)
                .filter(l -> l.getKey().toLowerCase().contains(query.toLowerCase()))
                .map(Map.Entry::getValue)
                .toList();
    }

    private void cleanupLanguagesList() {
        if (mLanguagesAdapter != null) {
            mLanguagesAdapter.setCallbacks(null);
        }
        mLanguagesAdapter = null;
    }

    // endregion Languages List

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
