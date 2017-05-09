package org.keynote.godtools.android.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.Loader;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.annimon.stream.Stream;

import org.ccci.gto.android.common.support.v4.app.SimpleLoaderCallbacks;
import org.ccci.gto.android.common.support.v4.util.FragmentUtils;
import org.keynote.godtools.android.R;
import org.keynote.godtools.android.adapter.LanguagesAdapter;
import org.keynote.godtools.android.content.LanguagesLoader;
import org.keynote.godtools.android.model.Language;
import org.keynote.godtools.android.sync.GodToolsSyncService;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;

public class LanguagesFragment extends BaseFragment implements LanguagesAdapter.Callbacks {
    private static final String EXTRA_PRIMARY = LanguagesFragment.class.getName() + ".PRIMARY";

    private static final int LOADER_LANGUAGES = 101;

    public interface Callbacks {
        void onLocaleSelected(@NonNull final Locale locale);
    }

    @Nullable
    @BindView(R.id.languages)
    RecyclerView mLanguagesView;
    @Nullable
    private LanguagesAdapter mLanguagesAdapter;

    // these properties should be treated as final and only set/modified in onCreate()
    /*final*/ boolean mPrimary = true;

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
    public void onLanguageSelected(@NonNull final Locale language) {
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

    /* END lifecycle */

    private void startLoaders() {
        getLoaderManager().initLoader(LOADER_LANGUAGES, null, new LanguagesLoaderCallbacks());
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
            mLanguagesAdapter.setSelected(mPrimary ? mPrimaryLanguage : mParallelLanguage);
            mLanguagesAdapter.setLanguages(mLanguages);
            mLanguagesAdapter.setDisabled(mPrimary ? null : mPrimaryLanguage);
            mLanguagesAdapter.setProtected(Locale.ENGLISH, mPrimaryLanguage, mParallelLanguage);
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
}
