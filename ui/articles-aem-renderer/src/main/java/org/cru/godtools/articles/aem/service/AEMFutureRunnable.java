package org.cru.godtools.articles.aem.service;

import android.content.Context;

public class AEMFutureRunnable implements Runnable {
    private AEMDownloadManger mManager;

    public AEMFutureRunnable(Context context) {
        mManager = AEMDownloadManger.getInstance(context);
    }

    @Override
    public void run() {
        mManager.extractAemImportsFromManifestsTask();
        mManager.syncStaleAemImportsTask();
    }
}
