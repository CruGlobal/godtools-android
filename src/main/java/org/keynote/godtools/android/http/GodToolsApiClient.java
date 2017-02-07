package org.keynote.godtools.android.http;

import android.support.v4.os.AsyncTaskCompat;
import android.util.Log;

import org.keynote.godtools.android.snuffy.SnuffyApplication;
import org.keynote.godtools.renderer.crureader.bo.GPage.Util.FileUtils;

import java.io.File;

import static org.keynote.godtools.android.BuildConfig.BASE_URL;
import static org.keynote.godtools.android.BuildConfig.BASE_URL_V2;

public class GodToolsApiClient {

    private static final String ENDPOINT_META = "meta/";
    private static final String ENDPOINT_PACKAGES = "packages/";
    private static final String ENDPOINT_TRANSLATIONS = "translations/";
    private static final String ENDPOINT_DRAFTS = "drafts/";
    private static final String ENDPOINT_NOTIFICATIONS = "notification/";

//    public static void getListOfPackages(String tag, MetaTask.MetaTaskHandler taskHandler){
//        Log.i("APICHECK", "getListOfPackages tag: " + tag);
//        MetaTask metaTask = new MetaTask(taskHandler);
//        String url = BASE_URL_V2 + ENDPOINT_META;
//        Log.i("APICHECK", "url: " + url);
//        metaTask.execute(url, tag);
//    }

//    public static void getListOfDrafts(String authorization, String language, String tag, MetaTask.MetaTaskHandler taskHandler){
//        Log.i("APICHECK", "getListOfDrafts authorization: " + authorization + " language: " + language + " tag " + tag);
//        DraftMetaTask draftTask = new DraftMetaTask(taskHandler, authorization);
//        String url = BASE_URL_V2 + ENDPOINT_META + language;
//        Log.i("APICHECK", "url: " + url);
//        draftTask.execute(url, tag);
//    }

    public static void downloadLanguagePack(SnuffyApplication app, String langCode, String tag, DownloadTask.DownloadTaskHandler taskHandler) {
        Log.i("APICHECK", "downloadLanguagePack " + " language: " + langCode + " tag " + tag);

        String url = BASE_URL_V2 + ENDPOINT_PACKAGES + langCode;
        Log.i("APICHECK", "url: " + url);
        String filePath = app.getDocumentsDir().getAbsolutePath() + File.separator + langCode + File.separator + "package.zip";

        download(app, url, filePath, tag, null, langCode, taskHandler);
    }

    public static void downloadDrafts(SnuffyApplication app, String authorization, String langCode, String tag, DownloadTask.DownloadTaskHandler taskHandler){
        Log.i("APICHECK", "downloadDrafts " + "authorization: " + authorization + " language: " + langCode + " tag " + tag);
        String url = BASE_URL_V2 + ENDPOINT_DRAFTS + langCode + "?compressed=true";
        Log.i("APICHECK", "url: " + url);
        String filePath = app.getDocumentsDir().getAbsolutePath() + File.separator + langCode + File.separator + "package.zip";

        download(app, url, filePath, tag, authorization, langCode, taskHandler);
    }

    public static void createDraft(String authorization,
                                   String languageCode,
                                   String packageCode,
                                   DraftCreationTask.DraftTaskHandler taskHandler)
    {
        Log.i("APICHECK", "createDraft " + "authorization: " + authorization + " language: " + languageCode + " packageCode " + packageCode);
        String url = BASE_URL_V2 + ENDPOINT_TRANSLATIONS + languageCode + File.separator + packageCode;
        Log.i("APICHECK", "url " + url);
        new DraftCreationTask(taskHandler).execute(url, authorization);
    }

    public static void publishDraft(String authorization,
                                    String languageCode,
                                    String packageCode,
                                    DraftPublishTask.DraftTaskHandler taskHandler)
    {
        Log.i("APICHECK", "publishDraft " + "authorization: " + authorization + " language: " + languageCode + " packageCode " + packageCode);
        String url = BASE_URL_V2 + ENDPOINT_TRANSLATIONS + languageCode + File.separator + packageCode;
        Log.i("APICHECK", "url: " + url);
        new DraftPublishTask(taskHandler).execute(url, authorization);
    }

    public static void updateNotification(String authcode, String registrationId, int notificationType,  NotificationUpdateTask.NotificationUpdateTaskHandler taskHandler)
    {
        Log.i("APICHECK", "updateNotification " + "authorization: " + authcode + " registrationId: " + registrationId + " notificationType " + notificationType);
        String url = BASE_URL + ENDPOINT_NOTIFICATIONS + "update";
        Log.i("APICHECK", "url: " + url);
        new NotificationUpdateTask(taskHandler).execute(url, authcode, registrationId, notificationType);
    }

    private static void download(SnuffyApplication app, String url, String filePath, String tag, String authorization,
                                 String langCode, DownloadTask.DownloadTaskHandler taskHandler) {
        Log.i("APICHECK", "download " + "url: " + url + " filePath: " + filePath + " tag " + tag + " authorization  " + authorization);
        DownloadTask downloadTask = new DownloadTask(app.getApplicationContext(), FileUtils.getResourcesDir(), taskHandler);

        AsyncTaskCompat.executeParallel(downloadTask, url, filePath, tag, authorization, langCode);
    }
}
