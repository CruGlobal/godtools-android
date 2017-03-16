package org.keynote.godtools.android.newnew.fragments;

import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;

import org.ccci.gto.android.common.support.v4.app.SimpleLoaderCallbacks;
import org.ccci.gto.android.common.util.LocaleCompat;
import org.keynote.godtools.android.business.GTPackage;
import org.keynote.godtools.android.newnew.services.DownloadService;
import org.keynote.godtools.android.newnew.services.DownloadServiceBO;
import org.keynote.godtools.android.newnew.services.DownloadStateReceiver;
import org.keynote.godtools.android.support.v4.content.LivePackagesLoader;
import org.keynote.godtools.android.utils.WordUtils;

import java.util.List;
import java.util.Locale;

/**
 * Created by rmatt on 3/14/2017.
 */

public abstract class DownloadMonitoringFragment extends Fragment {
    protected static final int LOADER_LIVE_PACKAGES = 1;
    protected final PackagesLoaderCallbacks mLoaderCallbacksPackages = new PackagesLoaderCallbacks();
    protected DownloadStateReceiver mDownloadStateReceiver;

    protected void startLoaders() {
        final LoaderManager manager = getActivity().getSupportLoaderManager();
        manager.initLoader(LOADER_LIVE_PACKAGES, null, mLoaderCallbacksPackages);
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter statusIntentFilter = new IntentFilter(
                DownloadService.DOWNLOAD_BROADCAST_ACTION);

        mDownloadStateReceiver =
                new DownloadStateReceiver() {
                    @Override
                    public void progress(String languageCode, int progress) {
                        downloadProgress(languageCode, progress);
                    }

                    @Override
                    public void fail(DownloadServiceBO downloadServiceBO) {
                        downloadFail(downloadServiceBO);
                    }

                    @Override
                    public void success(DownloadServiceBO downloadServiceBO) {
                        downloadSuccess(downloadServiceBO);
                    }

                };
        // Registers the DownloadStateReceiver and its intent filters
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
                mDownloadStateReceiver,
                statusIntentFilter);

    }

    @Override
    public void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(
                mDownloadStateReceiver);
    }

    abstract void downloadProgress(String languageCode, int progress);

    abstract void downloadSuccess(DownloadServiceBO dsBO);

    abstract void downloadFail(DownloadServiceBO dsBO);

    abstract void bindAdapter(List<GTPackage> packages);

    protected void displayMessage(DownloadServiceBO downloadServiceBO, String followupMessage) {
        Locale localePrimary = LocaleCompat.forLanguageTag(downloadServiceBO.getLangCode());
        String primaryName = WordUtils.capitalize(localePrimary.getDisplayName(Locale.getDefault()));
        Snackbar.make(getView(), primaryName + " " + followupMessage, Snackbar.LENGTH_SHORT).show();
    }

    protected class PackagesLoaderCallbacks extends SimpleLoaderCallbacks<List<GTPackage>> {
        @Override
        public Loader<List<GTPackage>> onCreateLoader(final int id, final Bundle args) {
            switch (id) {
                case LOADER_LIVE_PACKAGES:
                    return new LivePackagesLoader(DownloadMonitoringFragment.this.getActivity());
                default:
                    return null;
            }
        }



        @Override
        public void onLoadFinished(@NonNull final Loader<List<GTPackage>> loader,
                                   @Nullable final List<GTPackage> packages) {
            switch (loader.getId()) {
                case LOADER_LIVE_PACKAGES:
                    bindAdapter(packages);
                    break;
            }
        }
    }
}
