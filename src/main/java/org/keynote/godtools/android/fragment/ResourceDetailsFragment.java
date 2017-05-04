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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.annimon.stream.Stream;

import org.ccci.gto.android.common.support.v4.app.SimpleLoaderCallbacks;
import org.keynote.godtools.android.R;
import org.keynote.godtools.android.content.AvailableLanguagesLoader;
import org.keynote.godtools.android.content.LatestTranslationLoader;
import org.keynote.godtools.android.content.ResourceLoader;
import org.keynote.godtools.android.model.Resource;
import org.keynote.godtools.android.model.Translation;
import org.keynote.godtools.android.service.GodToolsResourceManager;
import org.keynote.godtools.android.util.ModelUtils;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Optional;

import static org.keynote.godtools.android.Constants.EXTRA_RESOURCE;
import static org.keynote.godtools.android.util.ViewUtils.bindShares;

public class ResourceDetailsFragment extends BaseFragment {
    private static final int LOADER_RESOURCE = 101;
    private static final int LOADER_LATEST_TRANSLATION = 102;
    private static final int LOADER_AVAILABLE_LANGUAGES = 103;

    // these properties should be treated as final and only set/modified in onCreate()
    /*final*/ long mResourceId = Resource.INVALID_ID;

    @Nullable
    @BindView(R.id.banner)
    ImageView mBanner;
    @Nullable
    @BindView(R.id.title)
    TextView mTitle;
    @Nullable
    @BindView(R.id.shares)
    TextView mShares;
    @Nullable
    @BindView(R.id.description)
    TextView mDescription;
    @Nullable
    @BindView(R.id.languages_header)
    TextView mLanguagesHeader;
    @Nullable
    @BindView(R.id.languages)
    TextView mLanguagesView;
    @Nullable
    @BindView(R.id.download_progress)
    ProgressBar mProgressBar;
    @Nullable
    @BindView(R.id.action_add)
    View mActionAdd;
    @Nullable
    @BindView(R.id.action_remove)
    View mActionRemove;

    @Nullable
    private Resource mResource;
    @Nullable
    private Translation mLatestTranslation;
    @NonNull
    private List<Locale> mLanguages = Collections.emptyList();

    public static Fragment newInstance(final long id) {
        final ResourceDetailsFragment fragment = new ResourceDetailsFragment();
        final Bundle args = new Bundle(1);
        args.putLong(EXTRA_RESOURCE, id);
        fragment.setArguments(args);
        return fragment;
    }

    /* BEGIN lifecycle */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle args = getArguments();
        if (args != null) {
            mResourceId = args.getLong(EXTRA_RESOURCE, mResourceId);
        }

        startLoaders();
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_resource_details, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        updateViews();
    }

    @Override
    public void onStart() {
        super.onStart();
        updateLatestTranslationLoader();
    }

    @Override
    protected void onUpdatePrimaryLanguage() {
        updateLatestTranslationLoader();
    }

    void onLoadResource(@Nullable final Resource resource) {
        mResource = resource;
        updateViews();
    }

    void onLoadLatestTranslation(@Nullable final Translation translation) {
        mLatestTranslation = translation;
        updateViews();
    }

    void onLoadAvailableLanguages(@Nullable final List<Locale> locales) {
        mLanguages = locales != null ? locales : Collections.emptyList();
        updateViews();
    }

    /* END lifecycle */

    private void updateViews() {
        if (mTitle != null) {
            mTitle.setText(ModelUtils.getTranslationName(mLatestTranslation, mResource));
        }
        bindShares(mShares, mResource);
        if (mDescription != null) {
            mDescription.setText(mLatestTranslation != null ? mLatestTranslation.getDescription() : "");
        }
        if (mLanguagesHeader != null) {
            final int count = mLanguages.size();
            mLanguagesHeader.setText(mLanguagesHeader.getResources()
                                             .getQuantityString(R.plurals.label_resources_languages, count, count));
        }
        if (mLanguagesView != null) {
            mLanguagesView.setVisibility(mLanguages.isEmpty() ? View.GONE : View.VISIBLE);
            mLanguagesView.setText(Stream.of(mLanguages)
                                           .map(Locale::getDisplayLanguage)
                                           .withoutNulls()
                                           .sorted(String::compareToIgnoreCase)
                                           .reduce((l1, l2) -> l1 + ", " + l2)
                                           .orElse(""));
        }
        if (mActionAdd != null) {
            mActionAdd.setEnabled(mResource != null && !mResource.isAdded());
            mActionAdd.setVisibility(mResource == null || !mResource.isAdded() ? View.VISIBLE : View.GONE);
        }
        if (mActionRemove != null) {
            mActionRemove.setEnabled(mResource != null && mResource.isAdded());
            mActionRemove.setVisibility(mResource == null || mResource.isAdded() ? View.VISIBLE : View.GONE);
        }
    }

    @Optional
    @OnClick(R.id.action_add)
    void addResource() {
        GodToolsResourceManager.getInstance(getContext()).addResource(mResourceId);
    }

    @Optional
    @OnClick(R.id.action_remove)
    void removeResource() {
        GodToolsResourceManager.getInstance(getContext()).removeResource(mResourceId);
    }

    private void startLoaders() {
        final LoaderManager lm = getLoaderManager();
        lm.initLoader(LOADER_RESOURCE, null, new ResourceLoaderCallbacks());
        lm.initLoader(LOADER_LATEST_TRANSLATION, null, new TranslationLoaderCallbacks());
        lm.initLoader(LOADER_AVAILABLE_LANGUAGES, null, new LocalesLoaderCallbacks());
    }

    private void updateLatestTranslationLoader() {
        final Loader<Translation> loader = getLoaderManager().getLoader(LOADER_LATEST_TRANSLATION);
        if (loader instanceof LatestTranslationLoader) {
            ((LatestTranslationLoader) loader).setLocale(mPrimaryLanguage);
        }
    }

    class ResourceLoaderCallbacks extends SimpleLoaderCallbacks<Resource> {
        @Nullable
        @Override
        public Loader<Resource> onCreateLoader(final int id, @Nullable final Bundle args) {
            switch (id) {
                case LOADER_RESOURCE:
                    return new ResourceLoader(getContext(), mResourceId);
                default:
                    return null;
            }
        }

        @Override
        public void onLoadFinished(@NonNull final Loader<Resource> loader, @Nullable final Resource resource) {
            switch (loader.getId()) {
                case LOADER_RESOURCE:
                    onLoadResource(resource);
                    break;
            }
        }
    }

    class TranslationLoaderCallbacks extends SimpleLoaderCallbacks<Translation> {
        @Nullable
        @Override
        public Loader<Translation> onCreateLoader(final int id, @Nullable final Bundle args) {
            switch (id) {
                case LOADER_LATEST_TRANSLATION:
                    return new LatestTranslationLoader(getContext(), mResourceId, mPrimaryLanguage);
                default:
                    return null;
            }
        }

        @Override
        public void onLoadFinished(@NonNull final Loader<Translation> loader, @Nullable final Translation translation) {
            switch (loader.getId()) {
                case LOADER_LATEST_TRANSLATION:
                    onLoadLatestTranslation(translation);
                    break;
            }
        }
    }

    class LocalesLoaderCallbacks extends SimpleLoaderCallbacks<List<Locale>> {
        @Nullable
        @Override
        public Loader<List<Locale>> onCreateLoader(final int id, @Nullable final Bundle args) {
            switch (id) {
                case LOADER_AVAILABLE_LANGUAGES:
                    return new AvailableLanguagesLoader(getContext(), mResourceId);
                default:
                    return null;
            }
        }

        @Override
        public void onLoadFinished(@NonNull final Loader<List<Locale>> loader,
                                   @Nullable final List<Locale> locales) {
            switch (loader.getId()) {
                case LOADER_AVAILABLE_LANGUAGES:
                    onLoadAvailableLanguages(locales);
                    break;
            }
        }
    }
}
