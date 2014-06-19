package org.keynote.godtools.android.http;

import android.content.Context;

import org.keynote.godtools.android.snuffy.SnuffyApplication;

import java.io.File;

public class GodToolsApiClient {

    private static final String BASE_URL = "http://ec2-54-209-152-169.compute-1.amazonaws.com/godtools-api/rest/";
    private static final String ENDPOINT_META = "meta/";
    private static final String ENDPOINT_PACKAGES = "packages/";
    private static final String ENDPOINT_TRANSLATIONS = "translations/";

    public static void getListOfPackages(String langCode, String tag, HttpTask.HttpTaskHandler taskHandler){
        HttpGetTask getTask = new HttpGetTask(taskHandler);
        String url = BASE_URL + ENDPOINT_META + langCode;
        getTask.execute(url, tag);
    }

    public static void downloadLanguagePack(SnuffyApplication app, String langCode, String tag, DownloadTask.DownloadTaskHandler taskHandler){
        String url = BASE_URL + ENDPOINT_PACKAGES + langCode + "?compressed=true";
        String mock_url = "https://docs.google.com/uc?export=download&id=0B1T_JTQ8nih7N0lGTVRlQldkaEk";
        String filePath = app.getDocumentsDir().getAbsolutePath() + File.separator + langCode + File.separator + "package.zip";

        download(app.getApplicationContext(), mock_url, filePath, tag ,taskHandler);
    }

    public static void downloadPackage(SnuffyApplication app, String langCode, String packageName, String tag, DownloadTask.DownloadTaskHandler taskHandler){
        String url = BASE_URL + ENDPOINT_PACKAGES + langCode + "/" + packageName + "?compressed=true";
        String filePath = app.getDocumentsDir().getAbsolutePath() + File.separator + langCode  + "_" + packageName + File.separator + "package.zip";

        download(app.getApplicationContext(), url, filePath, tag, taskHandler);
    }

    public static void downloadTranslation(SnuffyApplication app ,String langCode, String tag, DownloadTask.DownloadTaskHandler taskHandler) {
        String url = BASE_URL + ENDPOINT_TRANSLATIONS + langCode + "?compressed=true";
        String filePath = app.getDocumentsDir().getAbsolutePath() + File.separator + langCode + File.separator + "package.zip";

        download(app.getApplicationContext(), url, filePath, tag,taskHandler);
    }

    private static void download(Context context ,String url, String filePath, String tag, DownloadTask.DownloadTaskHandler taskHandler){
        DownloadTask dlTask = new DownloadTask(context, taskHandler);
        dlTask.execute(url, filePath, tag);
    }
}
