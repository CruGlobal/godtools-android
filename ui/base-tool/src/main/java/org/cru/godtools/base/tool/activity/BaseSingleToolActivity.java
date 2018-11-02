package org.cru.godtools.base.tool.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.google.common.base.Objects;

import org.ccci.gto.android.common.support.v4.app.SimpleLoaderCallbacks;
import org.ccci.gto.android.common.util.os.BundleUtils;
import org.cru.godtools.model.Language;
import org.cru.godtools.model.Tool;
import org.cru.godtools.model.Translation;
import org.cru.godtools.model.loader.LatestTranslationLoader;
import org.cru.godtools.xml.content.ManifestLoader;
import org.cru.godtools.xml.model.Manifest;

import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

import static org.cru.godtools.base.Constants.EXTRA_LANGUAGE;
import static org.cru.godtools.base.Constants.EXTRA_TOOL;

public abstract class BaseSingleToolActivity extends BaseToolActivity {
    private static final int LOADER_TRANSLATION = 101;
    private static final int LOADER_MANIFEST = 102;

    private final boolean mRequireTool;

    @NonNull
    @SuppressWarnings("ConstantConditions")
    private /*final*/ String mTool = Tool.INVALID_CODE;
    @NonNull
    private /*final*/ Locale mLocale = Language.INVALID_CODE;

    private boolean mTranslationLoaded = false;
    @Nullable
    private Translation mTranslation;
    @Nullable
    protected Manifest mManifest;

    @NonNull
    protected static Bundle buildExtras(@NonNull final Activity activity, @NonNull final String toolCode,
                                        @NonNull final Locale language) {
        final Bundle extras = buildExtras(activity);
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

    @NonNull
    protected final String getTool() {
        if (!mRequireTool) {
            throw new UnsupportedOperationException(
                    "You cannot call getTool() on a fragment that doesn't require a tool");
        }
        return mTool;
    }

    @NonNull
    protected final Locale getLocale() {
        if (!mRequireTool) {
            throw new UnsupportedOperationException(
                    "You cannot call getLocale() on a fragment that doesn't require a tool");
        }
        return mLocale;
    }

    @Override
    protected void cacheTools() {
        if (mDownloadManager != null) {
            mDownloadManager.cacheTranslation(mTool, mLocale);
        }
    }

    @Override
    protected int determineActiveToolState() {
        if (mManifest != null) {
            return STATE_LOADED;
        } else if (mTranslationLoaded && mTranslation == null) {
            return STATE_NOT_FOUND;
        } else {
            return STATE_LOADING;
        }
    }

    private boolean validStartState() {
        return !Objects.equal(mTool, Tool.INVALID_CODE) && !Language.INVALID_CODE.equals(mLocale);
    }

    private void startLoaders() {
        final LoaderManager manager = LoaderManager.getInstance(this);

        manager.initLoader(LOADER_TRANSLATION, null, new TranslationLoaderCallbacks());
        manager.initLoader(LOADER_MANIFEST, null, new ManifestLoaderCallbacks());
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
                    return new LatestTranslationLoader(BaseSingleToolActivity.this, getTool(), getLocale());
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

    class ManifestLoaderCallbacks extends SimpleLoaderCallbacks<Manifest> {
        @Nullable
        @Override
        public Loader<Manifest> onCreateLoader(final int id, @Nullable final Bundle args) {
            switch (id) {
                case LOADER_MANIFEST:
                    return new ManifestLoader(BaseSingleToolActivity.this, getTool(), getLocale());
            }

            return null;
        }

        @Override
        public void onLoadFinished(@NonNull final Loader<Manifest> loader, @Nullable final Manifest manifest) {
            switch (loader.getId()) {
                case LOADER_MANIFEST:
                    setManifest(manifest);
                    break;
            }
        }
    }
}
