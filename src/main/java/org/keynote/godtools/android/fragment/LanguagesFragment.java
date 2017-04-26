package org.keynote.godtools.android.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.ccci.gto.android.common.support.v4.app.SimpleLoaderCallbacks;
import org.ccci.gto.android.common.support.v4.util.FragmentUtils;
import org.keynote.godtools.android.R;
import org.keynote.godtools.android.adapter.LanguagesAdapter;
import org.keynote.godtools.android.content.ActiveLocaleLoader;
import org.keynote.godtools.android.content.LanguagesLoader;
import org.keynote.godtools.android.model.Language;
import org.keynote.godtools.android.sync.GodToolsSyncService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class LanguagesFragment extends Fragment implements LanguagesAdapter.Callbacks {
    private static final String EXTRA_PRIMARY = LanguagesFragment.class.getName() + ".PRIMARY";

    private static final int LOADER_LANGUAGES = 101;
    private static final int LOADER_SELECTED_LOCALE = 102;

    public interface Callbacks {
        void onLocaleSelected(@NonNull final Locale locale);
    }

    @Nullable
    Unbinder mButterKnife;

    @Nullable
    @BindView(R.id.languages)
    RecyclerView mLanguagesView;
    @Nullable
    private LanguagesAdapter mLanguagesAdapter;

    // these properties should be treated as final and only set/modified in onCreate()
    /*final*/ boolean mPrimary = true;

    @Nullable
    private Locale mSelectedLocale;
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

        final Bundle args = getArguments();
        if (args != null) {
            mPrimary = args.getBoolean(EXTRA_PRIMARY, mPrimary);
        }

        syncData(false);
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
        ButterKnife.bind(this, view);
        setupLanguagesList();
    }

    void onLoadSelectedLocale(@Nullable final Locale locale) {
        mSelectedLocale = locale;
        updateLanguagesList();
    }

    void onLoadLanguages(@Nullable final List<Language> languages) {
        if (languages == null) {
            mLanguages = null;
        } else {
            mLanguages = new ArrayList<>(languages);
            //noinspection ComparatorCombinators,Java8ListSort
            Collections.sort(mLanguages, (l1, l2) -> l1.getDisplayName().compareTo(l2.getDisplayName()));
        }

        updateLanguagesList();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        cleanupLanguagesList();
        if (mButterKnife != null) {
            mButterKnife.unbind();
        }
        mButterKnife = null;
    }

    /* END lifecycle */

    private void startLoaders() {
        final LoaderManager lm = getLoaderManager();
        lm.initLoader(LOADER_LANGUAGES, null, new LanguagesLoaderCallbacks());
        lm.initLoader(LOADER_SELECTED_LOCALE, null, new LocaleLoaderCallbacks());
    }

    private void syncData(final boolean force) {
        GodToolsSyncService.syncLanguages(getContext(), force).sync();
    }

    private void setupLanguagesList() {
        if (mLanguagesView != null) {
            final Context context = mLanguagesView.getContext();
            final LinearLayoutManager layoutManager = new LinearLayoutManager(context);
            mLanguagesView.setLayoutManager(layoutManager);
            mLanguagesView.addItemDecoration(new DividerItemDecoration(context, layoutManager.getOrientation()));

            mLanguagesAdapter = new LanguagesAdapter(context);
            mLanguagesAdapter.setCallbacks(this);
            mLanguagesView.setAdapter(mLanguagesAdapter);
        }
    }

    private void updateLanguagesList() {
        if (mLanguagesAdapter != null) {
            mLanguagesAdapter.setSelected(mSelectedLocale);
            mLanguagesAdapter.setLanguages(mLanguages);
        }
    }

    private void cleanupLanguagesList() {
        if (mLanguagesAdapter != null) {
            mLanguagesAdapter.setCallbacks(null);
        }
        mLanguagesAdapter = null;
    }

    @Override
    public void onLanguageSelected(@NonNull final Locale language) {
        final Callbacks listener = FragmentUtils.getListener(this, Callbacks.class);
        if (listener != null) {
            listener.onLocaleSelected(language);
        }
    }

    class LanguagesLoaderCallbacks extends SimpleLoaderCallbacks<List<Language>> {
        @Nullable
        @Override
        public Loader<List<Language>> onCreateLoader(final int id, @Nullable final Bundle args) {
            switch (id) {
                case LOADER_LANGUAGES:
                    return new LanguagesLoader(getContext());
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

    class LocaleLoaderCallbacks extends SimpleLoaderCallbacks<Locale> {
        @Nullable
        @Override
        public Loader<Locale> onCreateLoader(final int id, @Nullable final Bundle args) {
            switch (id) {
                case LOADER_SELECTED_LOCALE:
                    return new ActiveLocaleLoader(getContext(), mPrimary);
                default:
                    return null;
            }
        }

        @Override
        public void onLoadFinished(@NonNull final Loader<Locale> loader, @Nullable final Locale locale) {
            switch (loader.getId()) {
                case LOADER_SELECTED_LOCALE:
                    onLoadSelectedLocale(locale);
                    break;
            }
        }
    }
}
