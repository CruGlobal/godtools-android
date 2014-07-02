package org.keynote.godtools.android.http;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;

import org.keynote.godtools.android.snuffy.SnuffyApplication;

import java.io.File;

public class GodToolsApiClient {

    private static final String BASE_URL = "http://ec2-54-209-152-169.compute-1.amazonaws.com/godtools-api/rest/";
    private static final String ENDPOINT_META = "meta/";
    private static final String ENDPOINT_PACKAGES = "packages/";
    private static final String ENDPOINT_TRANSLATIONS = "translations/";

    public static void getListOfPackages(String tag, HttpTask.HttpTaskHandler taskHandler) {
        HttpGetTask getTask = new HttpGetTask(taskHandler);
        String url = BASE_URL + ENDPOINT_META;
        String mock_url = "http://demo9996907.mockable.io/meta";
        getTask.execute(mock_url, tag);
    }

    public static void downloadLanguagePack(SnuffyApplication app, String langCode, String tag, DownloadTask.DownloadTaskHandler taskHandler) {
        String url = BASE_URL + ENDPOINT_PACKAGES + langCode + "?compressed=true";
        String mock_url = getMockURL(langCode);
        String filePath = app.getDocumentsDir().getAbsolutePath() + File.separator + langCode + File.separator + "package.zip";

        download(app.getApplicationContext(), mock_url, filePath, tag, taskHandler);
    }

    public static void downloadPackage(SnuffyApplication app, String langCode, String packageName, String tag, DownloadTask.DownloadTaskHandler taskHandler) {
        String url = BASE_URL + ENDPOINT_PACKAGES + langCode + File.separator + packageName + "?compressed=true";
        String filePath = app.getDocumentsDir().getAbsolutePath() + File.separator + langCode + "_" + packageName + File.separator + "package.zip";

        download(app.getApplicationContext(), url, filePath, tag, taskHandler);
    }

    public static void downloadTranslation(SnuffyApplication app, String packageName, String langCode, String tag, DownloadTask.DownloadTaskHandler taskHandler) {
        String url = BASE_URL + ENDPOINT_TRANSLATIONS + langCode + File.separator + packageName + "?compressed=true";
        String filePath = app.getDocumentsDir().getAbsolutePath() + File.separator + langCode + "_" + packageName + File.separator + "package.zip";

        download(app.getApplicationContext(), url, filePath, tag, taskHandler);
    }

    private static void download(Context context, String url, String filePath, String tag, DownloadTask.DownloadTaskHandler taskHandler) {
        DownloadTask dlTask = new DownloadTask(context, taskHandler);
//        dlTask.execute(url, filePath, tag);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            dlTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url, filePath, tag);
        } else {
            dlTask.execute(url, filePath, tag);
        }

    }

    private static String getMockURL(String languageCode) {
        String url = "";
        if (languageCode.equalsIgnoreCase("en"))
            url = "https://docs.google.com/uc?export=download&id=0B1T_JTQ8nih7N0lGTVRlQldkaEk";
        else if (languageCode.equalsIgnoreCase("es"))
            url = "https://docs.google.com/uc?export=download&id=0B1T_JTQ8nih7LUpqVTJxZHZXZUE";
        else if (languageCode.equalsIgnoreCase("fr"))
            url = "https://docs.google.com/uc?export=download&id=0B1T_JTQ8nih7REs4ZHZTSHp2RGM";
        else if (languageCode.equalsIgnoreCase("et"))
            url = "https://docs.google.com/uc?export=download&id=0B1T_JTQ8nih7Z1hTWGdIczJNa3c";

        return url;
    }
}
