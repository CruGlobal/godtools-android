package org.keynote.godtools.android.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.ccci.gto.android.common.support.v4.app.SimpleLoaderCallbacks;
import org.keynote.godtools.android.R;
import org.keynote.godtools.android.activity.LanguageSelectionActivity;
import org.keynote.godtools.android.content.ActiveLocaleLoader;
import org.keynote.godtools.android.utils.WordUtils;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Optional;

import static com.google.common.base.MoreObjects.firstNonNull;

public class LanguageSettingsFragment extends Fragment {
    private static final int LOADER_PRIMARY_LOCALE = 101;
    private static final int LOADER_PARALLEL_LOCALE = 102;

    private final LocaleLoaderCallbacks mLocaleLoaderCallbacks = new LocaleLoaderCallbacks();

    @Nullable
    @BindView(R.id.primary_language)
    TextView mPrimaryLanguage;
    @Nullable
    @BindView(R.id.parallel_language)
    TextView mParallelLanguage;

    @Nullable
    Locale mPrimary;
    @Nullable
    Locale mParallel;

    public static LanguageSettingsFragment newInstance() {
        return new LanguageSettingsFragment();
    }

    /* BEGIN lifecycle */

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startLoaders();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_language_settings, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateLanguages();
    }

    void onUpdateLocale(@Nullable final Locale locale, final boolean primary) {
        if (primary) {
            mPrimary = locale;
        } else {
            mParallel = locale;
        }
        updateLanguages();
    }

    /* END lifecycle */

    private void startLoaders() {
        final LoaderManager lm = getLoaderManager();
        lm.initLoader(LOADER_PRIMARY_LOCALE, null, mLocaleLoaderCallbacks);
        lm.initLoader(LOADER_PARALLEL_LOCALE, null, mLocaleLoaderCallbacks);
    }

    private void updateLanguages() {
        if (mPrimaryLanguage != null) {
            mPrimaryLanguage
                    .setText(WordUtils.capitalize(firstNonNull(mPrimary, Locale.getDefault()).getDisplayName()));
        }
        if (mParallelLanguage != null) {
            if (mParallel != null) {
                mParallelLanguage.setText(WordUtils.capitalize(mParallel.getDisplayName()));
            } else {
                mParallelLanguage.setText(R.string.action_language_parallel_select);
            }
        }
    }

    @Optional
    @OnClick(R.id.primary_language)
    void editPrimaryLanguage() {
        LanguageSelectionActivity.start(getContext(), true);
    }

    @Optional
    @OnClick(R.id.parallel_language)
    void editParallelLanguage() {
        LanguageSelectionActivity.start(getContext(), false);
    }

    class LocaleLoaderCallbacks extends SimpleLoaderCallbacks<Locale> {
        @Nullable
        @Override
        public Loader<Locale> onCreateLoader(final int id, @Nullable final Bundle args) {
            switch (id) {
                case LOADER_PRIMARY_LOCALE:
                    return new ActiveLocaleLoader(getContext(), true);
                case LOADER_PARALLEL_LOCALE:
                    return new ActiveLocaleLoader(getContext(), false);
                default:
                    return null;
            }
        }

        @Override
        public void onLoadFinished(@NonNull final Loader<Locale> loader, @Nullable final Locale locale) {
            switch (loader.getId()) {
                case LOADER_PRIMARY_LOCALE:
                    onUpdateLocale(locale, true);
                    break;
                case LOADER_PARALLEL_LOCALE:
                    onUpdateLocale(locale, false);
                    break;
            }
        }
    }
}
