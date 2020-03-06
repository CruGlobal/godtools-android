package org.cru.godtools.base.tool.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import org.ccci.gto.android.common.util.os.BundleUtils;
import org.cru.godtools.base.tool.viewmodel.LatestPublishedManifestDataModel;
import org.cru.godtools.model.Language;
import org.cru.godtools.model.Translation;
import org.cru.godtools.model.loader.LatestTranslationLoader;
import org.cru.godtools.xml.model.Manifest;

import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

import static org.cru.godtools.base.Constants.EXTRA_LANGUAGE;
import static org.cru.godtools.base.Constants.EXTRA_TOOL;

public abstract class BaseSingleToolActivity extends BaseToolActivity {
    private static final int LOADER_TRANSLATION = 101;

    private final boolean mRequireTool;

    @Nullable
    protected /*final*/ String mTool = null;
    @NonNull
    protected /*final*/ Locale mLocale = Language.INVALID_CODE;

    private boolean mTranslationLoaded = false;
    @Nullable
    private Translation mTranslation;
    @Nullable
    private Manifest mManifest;

    @NonNull
    public static Bundle buildExtras(@NonNull final Context context, @Nullable final String toolCode,
                                     @Nullable final Locale language) {
        final Bundle extras = buildExtras(context);
        extras.putString(EXTRA_TOOL, toolCode);
        BundleUtils.putLocale(extras, EXTRA_LANGUAGE, language);
        return extras;
    }

    public BaseSingleToolActivity(final boolean immersive) {
        this(immersive, true);
    }

    public BaseSingleToolActivity(final boolean immersive, final boolean requireTool) {
        super(immersive);
        mRequireTool = requireTool;
    }

    // region Lifecycle Events

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent intent = getIntent();
        final Bundle extras = intent != null ? intent.getExtras() : null;
        if (extras != null) {
            mTool = extras.getString(EXTRA_TOOL, mTool);
            mLocale = BundleUtils.getLocale(extras, EXTRA_LANGUAGE, mLocale);
        }

        // finish now if this activity is in an invalid state
        if (!validStartState()) {
            finish();
            return;
        }

        setupDataModel();
        startLoaders();
    }

    @Override
    protected void onStart() {
        super.onStart();
        startDownloadProgressListener(mTool, mLocale);
    }

    protected void onUpdateTranslation() {
        updateVisibilityState();
    }

    @Override
    protected void onStop() {
        stopDownloadProgressListener();
        super.onStop();
    }

    // endregion Lifecycle Events

    private boolean hasTool() {
        return mTool != null && !Language.INVALID_CODE.equals(mLocale);
    }

    @NonNull
    protected final String getTool() {
        if (!mRequireTool) {
            throw new UnsupportedOperationException(
                    "You cannot call getTool() on a fragment that doesn't require a tool");
        }
        if (mTool == null) {
            throw new IllegalStateException("mRequireTool is true, but a tool wasn't specified");
        }
        return mTool;
    }

    @NonNull
    protected final Locale getLocale() {
        if (!mRequireTool) {
            throw new UnsupportedOperationException(
                    "You cannot call getLocale() on a fragment that doesn't require a tool");
        }
        if (mLocale.equals(Language.INVALID_CODE)) {
            throw new IllegalStateException("mRequireTool is true, but a valid locale wasn't specified");
        }
        return mLocale;
    }

    @Override
    protected void cacheTools() {
        if (mDownloadManager != null && mTool != null) {
            mDownloadManager.cacheTranslation(mTool, mLocale);
        }
    }

    @Override
    protected int determineActiveToolState() {
        if (!hasTool()) {
            return STATE_LOADED;
        } else if (mManifest != null) {
            if (!isSupportedType(mManifest.getType())) {
                return STATE_INVALID_TYPE;
            }
            return STATE_LOADED;
        } else if (mTranslationLoaded && mTranslation == null) {
            return STATE_NOT_FOUND;
        } else {
            return STATE_LOADING;
        }
    }

    protected abstract boolean isSupportedType(@NonNull Manifest.Type type);

    private boolean validStartState() {
        return !mRequireTool || hasTool();
    }

    private void startLoaders() {
        // only start loaders if we have a tool
        if (hasTool()) {
            final LoaderManager manager = LoaderManager.getInstance(this);

            manager.initLoader(LOADER_TRANSLATION, null, new TranslationLoaderCallbacks());
            mDataModel.getManifest().observe(this, this::setManifest);
        } else {
            setTranslation(null);
            setManifest(null);
        }
    }

    void setManifest(@Nullable final Manifest manifest) {
        mManifest = manifest;
        onUpdateActiveManifest();
    }

    void setTranslation(@Nullable final Translation translation) {
        mTranslationLoaded = true;
        mTranslation = translation;
        onUpdateTranslation();
    }

    @Nullable
    @Override
    protected Manifest getActiveManifest() {
        return mManifest;
    }

    // region Data Model
    private LatestPublishedManifestDataModel mDataModel;

    private void setupDataModel() {
        mDataModel = (new ViewModelProvider(this)).get(LatestPublishedManifestDataModel.class);
        mDataModel.getToolCode().setValue(mTool);
        mDataModel.getLocale().setValue(mLocale);
    }
    // region Data Model

    // region Up Navigation

    @NonNull
    @Override
    protected Bundle buildParentIntentExtras() {
        final Bundle extras = super.buildParentIntentExtras();
        extras.putString(EXTRA_TOOL, mTool);
        BundleUtils.putLocale(extras, EXTRA_LANGUAGE, mLocale);
        return extras;
    }

    // endregion Up Navigation

    class TranslationLoaderCallbacks implements LoaderManager.LoaderCallbacks<Translation> {
        @Nullable
        @Override
        public Loader<Translation> onCreateLoader(final int id, @Nullable final Bundle args) {
            switch (id) {
                case LOADER_TRANSLATION:
                    assert mTool != null : "onCreateLoader() only called when we have a tool and locale";
                    return new LatestTranslationLoader(BaseSingleToolActivity.this, mTool, mLocale);
            }

            return null;
        }

        @Override
        public void onLoadFinished(@NonNull final Loader<Translation> loader, @Nullable final Translation translation) {
            switch (loader.getId()) {
                case LOADER_TRANSLATION:
                    setTranslation(translation);
                    break;
            }
        }

        @Override
        public void onLoaderReset(@NonNull final Loader<Translation> loader) {
            // noop
        }
    }
}
