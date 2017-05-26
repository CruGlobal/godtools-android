package org.keynote.godtools.android.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import org.ccci.gto.android.common.support.v4.app.SimpleLoaderCallbacks;
import org.ccci.gto.android.common.util.BundleUtils;
import org.cru.godtools.tract.model.Manifest;
import org.keynote.godtools.android.content.TractManifestLoader;
import org.keynote.godtools.android.model.Language;
import org.keynote.godtools.android.model.Tool;

import java.util.Locale;

import static org.keynote.godtools.android.Constants.EXTRA_PARALLEL_LANGUAGE;
import static org.keynote.godtools.android.Constants.EXTRA_PRIMARY_LANGUAGE;
import static org.keynote.godtools.android.Constants.EXTRA_TOOL;

public class TractActivity extends org.cru.godtools.tract.activity.TractActivity {
    private static final int LOADER_MANIFEST_PRIMARY = 101;
    private static final int LOADER_MANIFEST_PARALLEL = 102;

    /*final*/ long mTool = Tool.INVALID_ID;
    @NonNull
    /*final*/ Locale mPrimaryLocale = Language.INVALID_CODE;
    @Nullable
    /*final*/ Locale mParallelLocale = null;

    public static void start(@NonNull final Context context, final long toolId, @NonNull final Locale primary,
                             @Nullable final Locale parallel) {
        final Intent intent = new Intent(context, TractActivity.class);
        intent.putExtra(EXTRA_TOOL, toolId);
        final Bundle extras = new Bundle();
        BundleUtils.putLocale(extras, EXTRA_PRIMARY_LANGUAGE, primary);
        BundleUtils.putLocale(extras, EXTRA_PARALLEL_LANGUAGE, parallel);
        intent.putExtras(extras);
        context.startActivity(intent);
    }

    /* BEGIN lifecycle */

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent intent = getIntent();
        final Bundle extras = intent != null ? intent.getExtras() : null;
        if (extras != null) {
            mTool = extras.getLong(EXTRA_TOOL, mTool);
            //noinspection ConstantConditions
            mPrimaryLocale = BundleUtils.getLocale(extras, EXTRA_PRIMARY_LANGUAGE, mPrimaryLocale);
            mParallelLocale = BundleUtils.getLocale(extras, EXTRA_PARALLEL_LANGUAGE, mParallelLocale);
        }

        startLoaders();
    }

    /* END lifecycle */

    private void startLoaders() {
        final LoaderManager manager = getSupportLoaderManager();

        final ManifestLoaderCallbacks manifestCallbacks = new ManifestLoaderCallbacks();
        manager.initLoader(LOADER_MANIFEST_PRIMARY, null, manifestCallbacks);
        if (mParallelLocale != null) {
            manager.initLoader(LOADER_MANIFEST_PARALLEL, null, manifestCallbacks);
        }
    }

    class ManifestLoaderCallbacks extends SimpleLoaderCallbacks<Manifest> {
        @Nullable
        @Override
        public Loader<Manifest> onCreateLoader(final int id, @Nullable final Bundle args) {
            switch (id) {
                case LOADER_MANIFEST_PRIMARY:
                    return new TractManifestLoader(TractActivity.this, mTool, mPrimaryLocale);
                case LOADER_MANIFEST_PARALLEL:
                    if (mParallelLocale != null) {
                        return new TractManifestLoader(TractActivity.this, mTool, mParallelLocale);
                    }
                default:
                    return null;
            }
        }

        @Override
        public void onLoadFinished(@NonNull final Loader<Manifest> loader, @Nullable final Manifest manifest) {
            switch (loader.getId()) {
                case LOADER_MANIFEST_PRIMARY:
                    setPrimaryManifest(manifest);
                    break;
                case LOADER_MANIFEST_PARALLEL:
                    setParallelManifest(manifest);
                    break;
            }
        }
    }
}
