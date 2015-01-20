package org.keynote.godtools.android.http;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;

import org.keynote.godtools.android.snuffy.SnuffyApplication;

import java.io.File;
import java.util.UUID;

public class GodToolsApiClient {
    private static final String BASE_URL = "http://api.godtoolsapp.com/godtools-api/rest/";
    private static final String ENDPOINT_META = "meta/";
    private static final String ENDPOINT_PACKAGES = "packages/";
    private static final String ENDPOINT_TRANSLATIONS = "translations/";
    private static final String ENDPOINT_DRAFTS = "drafts/";
    private static final String ENDPOINT_AUTH = "auth/";
    private static final String ENDPOINT_NOTIFICATIONS = "notification/";

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

    public static void authenticateGeneric(AuthTask.AuthTaskHandler taskHandler)
    {
        AuthTask authTask = new AuthTask(taskHandler);
        String url = BASE_URL + ENDPOINT_AUTH;
        authTask.execute(url);
    }

    public static void authenticateAccessCode(String accessCode, AuthTask.AuthTaskHandler taskHandler){
        AuthTask authTask = new AuthTask(taskHandler);
        String url = BASE_URL + ENDPOINT_AUTH + accessCode;
        authTask.execute(url);
    }

    public static void downloadDrafts(SnuffyApplication app, String authorization, String langCode, String tag, DownloadTask.DownloadTaskHandler taskHandler){
        String url = BASE_URL + ENDPOINT_DRAFTS + langCode + "?compressed=true";
        String filePath = app.getDocumentsDir().getAbsolutePath() + File.separator + langCode + File.separator + "package.zip";

        download(app.getApplicationContext(), url, filePath, tag, authorization, langCode, taskHandler);
    }

    public static void downloadDraftPage(SnuffyApplication app,
                                         String authorization,
                                         String languageCode,
                                         String packageCode,
                                         UUID pageId,
                                         DownloadTask.DownloadTaskHandler taskHandler)
    {
        String url = BASE_URL + ENDPOINT_DRAFTS + languageCode + File.separator + packageCode + File.separator + "pages" + File.separator + pageId + "?compressed=true";
        String filePath = app.getDocumentsDir().getAbsolutePath() + File.separator + languageCode + File.separator + pageId + ".zip";

        download(app.getApplicationContext(),
                url,
                filePath,
                "draft",
                authorization,
                languageCode,
                taskHandler);
    }

    public static void createDraft(String authorization,
                                   String languageCode,
                                   String packageCode,
                                   DraftCreationTask.DraftTaskHandler taskHandler)
    {
        String url = BASE_URL + ENDPOINT_TRANSLATIONS + languageCode + File.separator + packageCode;

        new DraftCreationTask(taskHandler).execute(url, authorization);
    }

    public static void publishDraft(String authorization,
                                    String languageCode,
                                    String packageCode,
                                    DraftPublishTask.DraftTaskHandler taskHandler)
    {
        String url = BASE_URL + ENDPOINT_TRANSLATIONS + languageCode + File.separator + packageCode;

        new DraftPublishTask(taskHandler).execute(url, authorization);
    }

    public static void registerDeviceForNotifications(String registrationID, String deviceId, NotificationRegistrationTask.NotificationTaskHandler taskHandler)
    {
        String url = BASE_URL + ENDPOINT_NOTIFICATIONS + registrationID;

        new NotificationRegistrationTask(taskHandler).execute(url, deviceId);
    }

    public static void updateNotification(String authcode, String registrationId, int notificationType,  NotificationUpdateTask.NotificationUpdateTaskHandler taskHandler)
    {
        String url = BASE_URL + ENDPOINT_NOTIFICATIONS + "update";

        new NotificationUpdateTask(taskHandler).execute(url, authcode, registrationId, notificationType);
    }

    private static void download(Context context, String url, String filePath, String tag, String authorization, String langCode, DownloadTask.DownloadTaskHandler taskHandler) {
        DownloadTask downloadTask = new DownloadTask(context, taskHandler);
        //downloadTask.execute(url, filePath, tag);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            downloadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url, filePath, tag, authorization, langCode);
        } else {
            downloadTask.execute(url, filePath, tag, authorization, langCode);
        }

    }
}
