package org.keynote.godtools.android.http;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;

import org.apache.http.client.methods.HttpPost;
import org.keynote.godtools.android.snuffy.SnuffyApplication;

import java.io.File;

public class GodToolsApiClient {

    private static final String BASE_URL = "http://godtoolsapi-stage-1291189452.us-east-1.elb.amazonaws.com/godtools-api/rest/";
    private static final String ENDPOINT_META = "meta/";
    private static final String ENDPOINT_PACKAGES = "packages/";
    private static final String ENDPOINT_TRANSLATIONS = "translations/";
    private static final String ENDPOINT_DRAFTS = "drafts/";
    private static final String ENDPOINT_AUTH = "auth/";

    public static void getListOfPackages(String authorization, String tag, MetaTask.MetaTaskHandler taskHandler){
        MetaTask metaTask = new MetaTask(taskHandler);
        String url = BASE_URL + ENDPOINT_META;
        metaTask.execute(url, authorization, "", tag);
    }

    public static void getListOfDrafts(String authorization, String language, String tag, MetaTask.MetaTaskHandler taskHandler){
        MetaTask draftTask = new MetaTask(taskHandler);
        String url = BASE_URL + ENDPOINT_META + language;
        draftTask.execute(url, authorization, language, tag);
    }

    public static void downloadLanguagePack(SnuffyApplication app, String langCode, String tag, String authorization, DownloadTask.DownloadTaskHandler taskHandler) {
        String url = BASE_URL + ENDPOINT_PACKAGES + langCode + "?compressed=true";
        String filePath = app.getDocumentsDir().getAbsolutePath() + File.separator + langCode + File.separator + "package.zip";

        download(app.getApplicationContext(), url, filePath, tag, authorization, langCode, taskHandler);
    }

    public static void authenticateAccessCode(String accessCode, AuthTask.AuthTaskHandler taskHandler){
        AuthTask authTask = new AuthTask(taskHandler);
        String url = BASE_URL + ENDPOINT_AUTH + accessCode;
        authTask.execute(url);
    }

    public static void downloadDrafts(SnuffyApplication app, String authorization, String langCode, String tag, DownloadTask.DownloadTaskHandler tashHandler){
        String url = BASE_URL + ENDPOINT_DRAFTS + langCode + "?compressed=true";
        String filePath = app.getDocumentsDir().getAbsolutePath() + File.separator + langCode + File.separator + "package.zip";

        download(app.getApplicationContext(), url, filePath, tag, authorization, langCode, tashHandler);
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

    private static void download(Context context, String url, String filePath, String tag, String authorization, String langCode, DownloadTask.DownloadTaskHandler taskHandler) {
        DownloadTask dlTask = new DownloadTask(context, taskHandler);
        //dlTask.execute(url, filePath, tag);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            dlTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url, filePath, tag, authorization, langCode);
        } else {
            dlTask.execute(url, filePath, tag, authorization, langCode);
        }

    }
}
