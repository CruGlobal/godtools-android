package org.cru.godtools.base.tool.activity;

import android.content.Intent;
import android.os.Bundle;

import com.google.common.base.Objects;

import org.ccci.gto.android.common.support.v4.app.SimpleLoaderCallbacks;
import org.ccci.gto.android.common.util.os.BundleUtils;
import org.cru.godtools.model.Language;
import org.cru.godtools.model.Tool;
import org.cru.godtools.xml.content.ManifestLoader;
import org.cru.godtools.xml.model.Manifest;

import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.Loader;

import static org.cru.godtools.base.Constants.EXTRA_LANGUAGE;
import static org.cru.godtools.base.Constants.EXTRA_TOOL;

public abstract class BaseSingleToolActivity extends BaseToolActivity {
    private static final int LOADER_MANIFEST = 101;

    @NonNull
    @SuppressWarnings("ConstantConditions")
    protected /*final*/ String mTool = Tool.INVALID_CODE;
    @NonNull
    protected /*final*/ Locale mLocale = Language.INVALID_CODE;

    @Nullable
    protected Manifest mManifest;

    protected static Bundle populateExtras(@NonNull final Bundle extras, @NonNull final String toolCode,
                                           @NonNull final Locale language) {
        extras.putString(EXTRA_TOOL, toolCode);
        BundleUtils.putLocale(extras, EXTRA_LANGUAGE, language);
        return extras;
    }

    public BaseSingleToolActivity(final boolean immersive) {
        super(immersive);
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

    @Override
    protected void onStop() {
        stopDownloadProgressListener();
        super.onStop();
    }

    // endregion Lifecycle Events

    private boolean validStartState() {
        return !Objects.equal(mTool, Tool.INVALID_CODE) && !Language.INVALID_CODE.equals(mLocale);
    }

    private void startLoaders() {
        getSupportLoaderManager().initLoader(LOADER_MANIFEST, null, new ManifestLoaderCallbacks());
    }

    void setManifest(@Nullable final Manifest manifest) {
        mManifest = manifest;
        onUpdateActiveManifest();
    }

    @Nullable
    @Override
    protected Manifest getActiveManifest() {
        return mManifest;
    }

    class ManifestLoaderCallbacks extends SimpleLoaderCallbacks<Manifest> {
        @Nullable
        @Override
        public Loader<Manifest> onCreateLoader(final int id, @Nullable final Bundle args) {
            switch (id) {
                case LOADER_MANIFEST:
                    return new ManifestLoader(BaseSingleToolActivity.this, mTool, mLocale);
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
