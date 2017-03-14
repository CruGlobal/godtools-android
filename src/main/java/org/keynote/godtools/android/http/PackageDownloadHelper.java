package org.keynote.godtools.android.http;

import android.content.Intent;

import org.keynote.godtools.android.newnew.services.DownloadService;
import org.keynote.godtools.android.snuffy.SnuffyApplication;

import java.io.File;

import static org.keynote.godtools.android.BuildConfig.BASE_URL_V2;

public class PackageDownloadHelper {

    private static final String ENDPOINT_PACKAGES = "packages/";
    private static final String ENDPOINT_DRAFTS = "drafts/";


    public static void downloadLanguagePack(SnuffyApplication app, String langCode, String tag, DownloadTask.DownloadTaskHandler taskHandler) {
        String url = BASE_URL_V2 + ENDPOINT_PACKAGES + langCode;
        String filePath = app.getDocumentsDir().getAbsolutePath() + File.separator + langCode + File.separator + "package.zip";
        download(app, url, filePath, tag, null, langCode, taskHandler);
    }

    public static void downloadDrafts(SnuffyApplication app, String authorization, String langCode, String tag, DownloadTask.DownloadTaskHandler taskHandler){
        String url = BASE_URL_V2 + ENDPOINT_DRAFTS + langCode + "?compressed=true";
        String filePath = app.getDocumentsDir().getAbsolutePath() + File.separator + langCode + File.separator + "package.zip";
        download(app, url, filePath, tag, authorization, langCode, taskHandler);
    }

    private static void download(SnuffyApplication app, String url, String filePath, String tag, String authorization,
                                 String langCode, DownloadTask.DownloadTaskHandler taskHandler) {

        Intent mDownloadServiceIntent = DownloadService.createIntent(app, url, filePath, tag, authorization, langCode);
        app.startService(mDownloadServiceIntent);
    }
}
