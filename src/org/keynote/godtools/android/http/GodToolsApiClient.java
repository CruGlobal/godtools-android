package org.keynote.godtools.android.http;

public class GodToolsApiClient {

    private static final String BASE_URL = "http://demo9996907.mockable.io/";
    private static final String ENDPOINT_META = "meta/";
    private static final String ENDPOINT_PACKAGE = "package/";

    public static void getListOfPackages(String langCode, String tag, HttpTask.HttpTaskHandler taskHandler){
        HttpGetTask getTask = new HttpGetTask(taskHandler);
        String url = BASE_URL + ENDPOINT_META + langCode;
        getTask.execute(url, tag);
    }

    public static void downloadLanguagePack(String langCode, String filePath, String tag, DownloadTask.DownloadTaskHandler taskHandler){
        String url = BASE_URL + ENDPOINT_PACKAGE + langCode;
        download(url, filePath, tag ,taskHandler);
    }

    public static void downloadPackageFiles(String langCode, String packageName, String filePath, String tag, DownloadTask.DownloadTaskHandler taskHandler){
        String url = BASE_URL + ENDPOINT_PACKAGE + langCode + "/" + packageName;
        download(url, filePath, tag, taskHandler);
    }

    private static void download(String url, String filePath, String tag, DownloadTask.DownloadTaskHandler taskHandler){
        DownloadTask dlTask = new DownloadTask(taskHandler);
        dlTask.execute(url, filePath, tag);
    }
}
