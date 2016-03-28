package org.keynote.godtools.android.http;

import android.support.v4.os.AsyncTaskCompat;

import org.keynote.godtools.android.snuffy.SnuffyApplication;

import java.io.File;
import java.util.UUID;

import static org.keynote.godtools.android.BuildConfig.BASE_URL;
import static org.keynote.godtools.android.BuildConfig.BASE_URL_V2;

public class GodToolsApiClient {

    private static final String ENDPOINT_META = "meta/";
    private static final String ENDPOINT_PACKAGES = "packages/";
    private static final String ENDPOINT_TRANSLATIONS = "translations/";
    private static final String ENDPOINT_DRAFTS = "drafts/";
    private static final String ENDPOINT_AUTH = "auth/";
    private static final String ENDPOINT_NOTIFICATIONS = "notification/";

    public static void getListOfPackages(String tag, MetaTask.MetaTaskHandler taskHandler){
        MetaTask metaTask = new MetaTask(taskHandler);
        String url = BASE_URL_V2 + ENDPOINT_META;
        metaTask.execute(url, tag);
    }

    public static void getListOfDrafts(String authorization, String language, String tag, MetaTask.MetaTaskHandler taskHandler){
        DraftMetaTask draftTask = new DraftMetaTask(taskHandler, authorization);
        String url = BASE_URL_V2 + ENDPOINT_META + language;
        draftTask.execute(url, tag);
    }

    public static void downloadLanguagePack(SnuffyApplication app, String langCode, String tag, DownloadTask.DownloadTaskHandler taskHandler) {
        String url = BASE_URL_V2 + ENDPOINT_PACKAGES + langCode;
        String filePath = app.getDocumentsDir().getAbsolutePath() + File.separator + langCode + File.separator + "package.zip";

        download(app, url, filePath, tag, null, langCode, taskHandler);
    }

    public static void verifyStatusOfAuthToken(String authToken, AuthTask.AuthTaskHandler taskHandler)
    {
        AuthTask authTask = new AuthTask(taskHandler, false, true);
        String url = BASE_URL + ENDPOINT_AUTH;
        authTask.execute(url, authToken);
    }

    public static void downloadDrafts(SnuffyApplication app, String authorization, String langCode, String tag, DownloadTask.DownloadTaskHandler taskHandler){
        String url = BASE_URL_V2 + ENDPOINT_DRAFTS + langCode + "?compressed=true";
        String filePath = app.getDocumentsDir().getAbsolutePath() + File.separator + langCode + File.separator + "package.zip";

        download(app, url, filePath, tag, authorization, langCode, taskHandler);
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

        download(app,
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
        String url = BASE_URL_V2 + ENDPOINT_TRANSLATIONS + languageCode + File.separator + packageCode;

        new DraftCreationTask(taskHandler).execute(url, authorization);
    }

    public static void publishDraft(String authorization,
                                    String languageCode,
                                    String packageCode,
                                    DraftPublishTask.DraftTaskHandler taskHandler)
    {
        String url = BASE_URL_V2 + ENDPOINT_TRANSLATIONS + languageCode + File.separator + packageCode;

        new DraftPublishTask(taskHandler).execute(url, authorization);
    }

    public static void registerDeviceForNotifications(String registrationID, String deviceId, String registrationsOn, NotificationRegistrationTask.NotificationTaskHandler taskHandler)
    {
        String url = BASE_URL + ENDPOINT_NOTIFICATIONS + registrationID;

        new NotificationRegistrationTask(taskHandler).execute(url, deviceId, registrationsOn);
    }

    public static void updateNotification(String authcode, String registrationId, int notificationType,  NotificationUpdateTask.NotificationUpdateTaskHandler taskHandler)
    {
        String url = BASE_URL + ENDPOINT_NOTIFICATIONS + "update";

        new NotificationUpdateTask(taskHandler).execute(url, authcode, registrationId, notificationType);
    }

    private static void download(SnuffyApplication app, String url, String filePath, String tag, String authorization,
                                 String langCode, DownloadTask.DownloadTaskHandler taskHandler) {
        DownloadTask downloadTask = new DownloadTask(app.getApplicationContext(), app.getResourcesDir(), taskHandler);

        AsyncTaskCompat.executeParallel(downloadTask, url, filePath, tag, authorization, langCode);
    }
}
