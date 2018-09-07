package org.cru.godtools.base.tool.fragment;

import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.Loader;

import org.ccci.gto.android.common.support.v4.app.SimpleLoaderCallbacks;
import org.ccci.gto.android.common.util.os.BundleUtils;
import org.cru.godtools.base.ui.fragment.BaseFragment;
import org.cru.godtools.model.Language;
import org.cru.godtools.model.Tool;
import org.cru.godtools.xml.content.ManifestLoader;
import org.cru.godtools.xml.model.Manifest;

import java.util.Locale;

import static org.cru.godtools.base.Constants.EXTRA_LANGUAGE;
import static org.cru.godtools.base.Constants.EXTRA_TOOL;

public abstract class BaseToolFragment extends BaseFragment {
    private static final int LOADER_MANIFEST = 101;

    @Nullable
    protected /*final*/ String mTool = Tool.INVALID_CODE;
    @NonNull
    protected /*final*/ Locale mLocale = Language.INVALID_CODE;

    @Nullable
    protected Manifest mManifest;

    protected static void populateArgs(@NonNull final Bundle args, @NonNull final String toolCode,
                                       @NonNull final Locale language) {
        args.putString(EXTRA_TOOL, toolCode);
        BundleUtils.putLocale(args, EXTRA_LANGUAGE, language);
    }

    // region Lifecycle Events

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle args = getArguments();
        if (args != null) {
            mTool = args.getString(EXTRA_TOOL, mTool);
            mLocale = BundleUtils.getLocale(args, EXTRA_LANGUAGE, mLocale);
        }

        startLoaders();
    }

    @CallSuper
    protected void onManifestUpdated() {}

    // endregion Lifecycle Events

    private void startLoaders() {
        getLoaderManager().initLoader(LOADER_MANIFEST, null, new ManifestLoaderCallbacks());
    }

    void setManifest(@Nullable final Manifest manifest) {
        mManifest = manifest;
        onManifestUpdated();
    }

    class ManifestLoaderCallbacks extends SimpleLoaderCallbacks<Manifest> {
        @Nullable
        @Override
        public Loader<Manifest> onCreateLoader(final int id, @Nullable final Bundle args) {
            switch (id) {
                case LOADER_MANIFEST:
                    if (mTool != null) {
                        return new ManifestLoader(requireContext(), mTool, mLocale);
                    }
                    break;
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
