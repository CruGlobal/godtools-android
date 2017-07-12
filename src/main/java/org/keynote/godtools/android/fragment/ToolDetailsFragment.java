package org.keynote.godtools.android.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.annimon.stream.Stream;

import org.ccci.gto.android.common.picasso.view.PicassoImageView;
import org.ccci.gto.android.common.support.v4.app.SimpleLoaderCallbacks;
import org.ccci.gto.android.common.support.v4.util.FragmentUtils;
import org.cru.godtools.download.manager.GodToolsDownloadManager;
import org.cru.godtools.download.manager.GodToolsDownloadManager.DownloadProgress;
import org.cru.godtools.model.Attachment;
import org.cru.godtools.model.Translation;
import org.cru.godtools.util.ModelUtils;
import org.keynote.godtools.android.R;
import org.keynote.godtools.android.content.AttachmentLoader;
import org.keynote.godtools.android.content.AvailableLanguagesLoader;
import org.keynote.godtools.android.content.LatestTranslationLoader;
import org.keynote.godtools.android.content.ToolLoader;
import org.keynote.godtools.android.model.Tool;
import org.keynote.godtools.android.util.ViewUtils;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.Optional;

import static org.cru.godtools.base.Constants.EXTRA_TOOL;
import static org.keynote.godtools.android.util.ViewUtils.bindDownloadProgress;
import static org.keynote.godtools.android.util.ViewUtils.bindShares;

public class ToolDetailsFragment extends BaseFragment
        implements GodToolsDownloadManager.OnDownloadProgressUpdateListener {
    public interface Callbacks {
        void onToolAdded();

        void onToolRemoved();
    }

    private static final int LOADER_TOOL = 101;
    private static final int LOADER_BANNER = 102;
    private static final int LOADER_LATEST_TRANSLATION = 103;
    private static final int LOADER_AVAILABLE_LANGUAGES = 104;

    @Nullable
    private GodToolsDownloadManager mDownloadManager;

    // these properties should be treated as final and only set/modified in onCreate()
    @Nullable
    /*final*/ String mToolCode = Tool.INVALID_CODE;

    @Nullable
    @BindView(R.id.banner)
    PicassoImageView mBanner;
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
    ProgressBar mDownloadProgressBar;
    @Nullable
    @BindView(R.id.action_add)
    View mActionAdd;
    @Nullable
    @BindView(R.id.action_remove)
    View mActionRemove;

    @Nullable
    private Tool mTool;
    @Nullable
    private Attachment mBannerAttachment;
    @Nullable
    private Translation mLatestTranslation;
    @Nullable
    private DownloadProgress mDownloadProgress;
    @NonNull
    private List<Locale> mLanguages = Collections.emptyList();

    public static Fragment newInstance(@Nullable final String code) {
        final ToolDetailsFragment fragment = new ToolDetailsFragment();
        final Bundle args = new Bundle(1);
        args.putString(EXTRA_TOOL, code);
        fragment.setArguments(args);
        return fragment;
    }

    /* BEGIN lifecycle */

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);
        if (mDownloadManager == null && context != null) {
            mDownloadManager = GodToolsDownloadManager.getInstance(context);
        }
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle args = getArguments();
        if (args != null) {
            mToolCode = args.getString(EXTRA_TOOL, mToolCode);
        }

        startLoaders();
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tool_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        updateViews();
        updateDownloadProgress();
    }

    @Override
    public void onStart() {
        super.onStart();
        updateLatestTranslationLoader();
        startProgressListener();
    }

    @Override
    protected void onUpdatePrimaryLanguage() {
        updateLatestTranslationLoader();

        // restart the progress listener
        stopProgressListener();
        startProgressListener();
    }

    void onLoadTool(@Nullable final Tool tool) {
        mTool = tool;
        updateBannerLoader();
        updateViews();
    }

    void onLoadBanner(@Nullable final Attachment banner) {
        mBannerAttachment = banner;
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

    @Override
    public void onDownloadProgressUpdated(@Nullable final DownloadProgress progress) {
        mDownloadProgress = progress;
        updateDownloadProgress();
    }

    @Override
    public void onStop() {
        super.onStop();
        stopProgressListener();
    }

    /* END lifecycle */

    private void startProgressListener() {
        if (mDownloadManager != null && mToolCode != null) {
            mDownloadManager.addOnDownloadProgressUpdateListener(mToolCode, mPrimaryLanguage, this);

            // get the initial progress
            onDownloadProgressUpdated(mDownloadManager.getDownloadProgress(mToolCode, mPrimaryLanguage));
        }
    }

    private void updateDownloadProgress() {
        bindDownloadProgress(mDownloadProgressBar, mDownloadProgress);
    }

    private void stopProgressListener() {
        if (mDownloadManager != null) {
            mDownloadManager.removeOnDownloadProgressUpdateListener(this);
        }
    }

    private void updateViews() {
        ViewUtils.bindLocalImage(mBanner, mBannerAttachment);
        if (mTitle != null) {
            mTitle.setText(ModelUtils.getTranslationName(getContext(), mLatestTranslation, mTool));
        }
        bindShares(mShares, mTool);
        if (mDescription != null) {
            mDescription.setText(ModelUtils.getTranslationDescription(getContext(), mLatestTranslation, mTool));
        }
        if (mLanguagesHeader != null) {
            final int count = mLanguages.size();
            mLanguagesHeader.setText(mLanguagesHeader.getResources()
                                             .getQuantityString(R.plurals.label_tools_languages, count, count));
        }
        if (mLanguagesView != null) {
            mLanguagesView.setVisibility(mLanguages.isEmpty() ? View.GONE : View.VISIBLE);
            mLanguagesView.setText(Stream.of(mLanguages)
                                           .map(Locale::getDisplayName)
                                           .withoutNulls()
                                           .sorted(String.CASE_INSENSITIVE_ORDER)
                                           .reduce((l1, l2) -> l1 + ", " + l2)
                                           .orElse(""));
        }
        if (mActionAdd != null) {
            mActionAdd.setEnabled(mTool != null && !mTool.isAdded());
            mActionAdd.setVisibility(mTool == null || !mTool.isAdded() ? View.VISIBLE : View.GONE);
        }
        if (mActionRemove != null) {
            mActionRemove.setEnabled(mTool != null && mTool.isAdded());
            mActionRemove.setVisibility(mTool == null || mTool.isAdded() ? View.VISIBLE : View.GONE);
        }
    }

    @Optional
    @OnClick(R.id.action_add)
    void addTool() {
        if (mToolCode != null) {
            GodToolsDownloadManager.getInstance(getContext()).addTool(mToolCode);
            final Callbacks callbacks = FragmentUtils.getListener(this, Callbacks.class);
            if (callbacks != null) {
                callbacks.onToolAdded();
            }
        }
    }

    @Optional
    @OnClick(R.id.action_remove)
    void removeTool() {
        if (mToolCode != null) {
            GodToolsDownloadManager.getInstance(getContext()).removeTool(mToolCode);
            final Callbacks callbacks = FragmentUtils.getListener(this, Callbacks.class);
            if (callbacks != null) {
                callbacks.onToolRemoved();
            }
        }
    }

    private void startLoaders() {
        final LoaderManager lm = getLoaderManager();
        lm.initLoader(LOADER_TOOL, null, new ToolLoaderCallbacks());
        lm.initLoader(LOADER_BANNER, null, new AttachmentLoaderCallbacks());
        lm.initLoader(LOADER_LATEST_TRANSLATION, null, new TranslationLoaderCallbacks());
        lm.initLoader(LOADER_AVAILABLE_LANGUAGES, null, new LocalesLoaderCallbacks());

        updateBannerLoader();
    }

    private void updateBannerLoader() {
        final Loader loader = getLoaderManager().getLoader(LOADER_BANNER);
        if (loader instanceof AttachmentLoader) {
            ((AttachmentLoader) loader).setId(mTool != null ? mTool.getDetailsBannerId() : Attachment.INVALID_ID);
        }
    }

    private void updateLatestTranslationLoader() {
        final Loader<Translation> loader = getLoaderManager().getLoader(LOADER_LATEST_TRANSLATION);
        if (loader instanceof LatestTranslationLoader) {
            ((LatestTranslationLoader) loader).setLocale(mPrimaryLanguage);
        }
    }

    class ToolLoaderCallbacks extends SimpleLoaderCallbacks<Tool> {
        @Nullable
        @Override
        public Loader<Tool> onCreateLoader(final int id, @Nullable final Bundle args) {
            switch (id) {
                case LOADER_TOOL:
                    if (mToolCode != null) {
                        return new ToolLoader(getContext(), mToolCode);
                    }
                    break;
            }

            return null;
        }

        @Override
        public void onLoadFinished(@NonNull final Loader<Tool> loader, @Nullable final Tool tool) {
            switch (loader.getId()) {
                case LOADER_TOOL:
                    onLoadTool(tool);
                    break;
            }
        }
    }

    class AttachmentLoaderCallbacks extends SimpleLoaderCallbacks<Attachment> {
        @Nullable
        @Override
        public Loader<Attachment> onCreateLoader(final int id, @Nullable final Bundle args) {
            switch (id) {
                case LOADER_BANNER:
                    return new AttachmentLoader(getContext());
                default:
                    return null;
            }
        }

        @Override
        public void onLoadFinished(@NonNull final Loader<Attachment> loader, @Nullable final Attachment attachment) {
            switch (loader.getId()) {
                case LOADER_BANNER:
                    onLoadBanner(attachment);
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
                    if (mToolCode != null) {
                        return new LatestTranslationLoader(getContext(), mToolCode, mPrimaryLanguage);
                    }
                    break;
            }

            return null;
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
                    if (mToolCode != null) {
                        return new AvailableLanguagesLoader(getContext(), mToolCode);
                    }
                    break;
            }

            return null;
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
