package org.keynote.godtools.android.http;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;

import org.keynote.godtools.android.snuffy.SnuffyApplication;

import java.io.File;

public class GodToolsApiClient {

    private static final String BASE_URL = "http://godtoolsapi-stage-1291189452.us-east-1.elb.amazonaws.com/godtools-api/rest/";
    private static final String ENDPOINT_META = "meta/";
    private static final String ENDPOINT_PACKAGES = "packages/";
    private static final String ENDPOINT_TRANSLATIONS = "translations/";

    public static void getListOfPackages(String authorization, String tag, HttpTask.HttpTaskHandler taskHandler) {
        HttpGetTask getTask = new HttpGetTask(taskHandler);
        String url = BASE_URL + ENDPOINT_META;
        String mock_url = "http://demo9996907.mockable.io/meta";
        getTask.execute(url, authorization, tag);
    }

    public static void downloadLanguagePack(SnuffyApplication app, String langCode, String tag, String authorization, DownloadTask.DownloadTaskHandler taskHandler) {
        String url = BASE_URL + ENDPOINT_PACKAGES + langCode + "?compressed=true";
        String mock_url = getMockURL(langCode);
        String filePath = app.getDocumentsDir().getAbsolutePath() + File.separator + langCode + File.separator + "package.zip";

        download(app.getApplicationContext(), url, filePath, tag, authorization, taskHandler);
    }

    /**
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
    */

    private static void download(Context context, String url, String filePath, String tag, String authorization, DownloadTask.DownloadTaskHandler taskHandler) {
        DownloadTask dlTask = new DownloadTask(context, taskHandler);
        //dlTask.execute(url, filePath, tag);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            dlTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url, filePath, tag, authorization);
        } else {
            dlTask.execute(url, filePath, tag, authorization);
        }

    }

    private static String getMockURL(String languageCode) {
        String url = "";
        if (languageCode.equalsIgnoreCase("en"))
            url = "https://docs.google.com/uc?export=download&id=0B1T_JTQ8nih7N0lGTVRlQldkaEk";
        else if (languageCode.equalsIgnoreCase("es"))
            url = "https://docs.google.com/uc?export=download&id=0B1T_JTQ8nih7ZHpna3A5UC1UVnc";//"https://docs.google.com/uc?export=download&id=0B1T_JTQ8nih7LUpqVTJxZHZXZUE";
        else if (languageCode.equalsIgnoreCase("fr"))
            url = "https://docs.google.com/uc?export=download&id=0B1T_JTQ8nih7ZUtNLTR4aDFFYVU";//"https://docs.google.com/uc?export=download&id=0B1T_JTQ8nih7REs4ZHZTSHp2RGM";
        else if (languageCode.equalsIgnoreCase("et"))
            url = "https://docs.google.com/uc?export=download&id=0B1T_JTQ8nih7LVB2WUs0WFJmMjA";//"https://docs.google.com/uc?export=download&id=0B1T_JTQ8nih7Z1hTWGdIczJNa3c";

        return url;
    }
}
