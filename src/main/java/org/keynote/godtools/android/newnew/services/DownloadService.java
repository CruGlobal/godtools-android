package org.keynote.godtools.android.newnew.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import org.ccci.gto.android.common.util.IOUtils;
import org.keynote.godtools.android.api.GodToolsApi;
import org.keynote.godtools.android.business.GTPackage;
import org.keynote.godtools.android.business.GTPackageReader;
import org.keynote.godtools.android.dao.DBContract;
import org.keynote.godtools.android.db.GodToolsDao;
import org.keynote.godtools.android.snuffy.Decompress;
import org.keynote.godtools.renderer.crureader.bo.GPage.Util.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Response;

/**
 * Created by rmatt on 2/28/2017.
 */

public class DownloadService extends IntentService {

    public static final String DOWNLOAD_SERVICE_BO_KEY = "org.keynote.godtools.android.newnew.services.BO_KEY";
    public static final String DOWNLOAD_COMPLETED_KEY = "org.keynote.godtools.android.newnew.services.DOWNLOAD_COMPLETED_KEY";
    public static final String DOWNLOAD_SUCCESSFUL_KEY = "org.keynote.godtools.android.newnew.services.DOWNLOAD_SUCCESSFUL_KEY";
    public static final String DOWNLOAD_BROADCAST_ACTION =
            "godtools.keynote.org.gttestui.services.download_broadcast";
    // Defines the key for the status "extra" in an Intent
    public static final String DOWNLOAD_EXTENDED_DATA_PROGRESS =
            "godtools.keynote.org.gttestui.services.download_progress";
    private static final String TAG = "DownloadService";
    public static final String DOWNLOAD_EXTENDED_DATA_LANGUAGE_CODE = "godtools.keynote.org.gttestui.services.language_code";

    public DownloadService() {
        super("DownloadService");
    }

    public DownloadService(String name) {
        super(name);
    }

    public static Intent createIntent(Context context, String url, String filePath, String tag, String authorization, String langCode) {
        DownloadServiceBO downloadServiceBO = new DownloadServiceBO(url, filePath, tag, authorization, langCode);
        Intent mDownloadServiceIntent = new Intent(context, DownloadService.class);
        mDownloadServiceIntent.putExtra(DOWNLOAD_SERVICE_BO_KEY, downloadServiceBO);
        return mDownloadServiceIntent;
    }

    @Override
    protected void onHandleIntent(Intent workIntent) {

        DownloadServiceBO dsBO = workIntent.getParcelableExtra(DOWNLOAD_SERVICE_BO_KEY);
        sendResult(dsBO, doInBackground(dsBO));
    }

    /*10% setup, 60% for download, 20% for unzip, 10% for delete and cleanup */
    protected void sendProgress(String langCode, int percent) {
        Intent localIntent =
                new Intent(DownloadService.DOWNLOAD_BROADCAST_ACTION);
        // Puts the status into the Intent
        localIntent.putExtra(DOWNLOAD_EXTENDED_DATA_PROGRESS, percent);
        localIntent.putExtra(DOWNLOAD_EXTENDED_DATA_LANGUAGE_CODE, langCode);
        // Broadcasts the Intent to receivers in this app.
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);

    }

    protected void sendResult(DownloadServiceBO dsBO, boolean successful) {
        Intent localIntent =
                new Intent(DownloadService.DOWNLOAD_BROADCAST_ACTION);
        // Puts the status into the Intent
        localIntent.putExtra(DOWNLOAD_COMPLETED_KEY, true);
        localIntent.putExtra(DOWNLOAD_SUCCESSFUL_KEY, successful);
        localIntent.putExtra(DOWNLOAD_SERVICE_BO_KEY, dsBO);
        // Broadcasts the Intent to receivers in this app.
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);

    }

    protected Boolean doInBackground(DownloadServiceBO dsBO) {

        int downloadResponseCode = -1;
        int downloadContentLength = -1;

        // get a temporary directory for extracting the zip file to
        final File tmpDir = FileUtils.getTmpDir(getApplicationContext());
        final File zipfile = new File(tmpDir, "package.zip");
        if (tmpDir == null) {
            Crashlytics.logException(new Exception("unable to get temporary directory for download: " + dsBO.getUrl()));
            return false;
        }
        try {
            Thread.sleep(15000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        sendProgress(dsBO.getLangCode(), 5);

        // download & extract zip file to tmp directory
        try {
            // download zip file

            Response<ResponseBody> response =
                    GodToolsApi.getInstance(this).legacy.downloadPackages(dsBO.getAuthorization(), dsBO.getLangCode())
                            .execute();
            if (response.isSuccessful()) {
                Log.d(TAG, "server contacted and has file");

                sendProgress(dsBO.getLangCode(), 10);
                boolean writtenToDisk = writeResponseBodyToDisk(dsBO, response.body(), zipfile);
                Log.d(TAG, "file download was a success? " + writtenToDisk);
                if (writtenToDisk) {
                    new Decompress().unzip(zipfile, tmpDir);
                    sendProgress(dsBO.getLangCode(), 90);
                    // parse content.xml
                    File contentFile = new File(tmpDir, "contents.xml");
                    List<GTPackage> packageList = GTPackageReader.processContentFile(contentFile);

                    final GodToolsDao adapter = GodToolsDao.getInstance(getApplicationContext());

                    // delete draft packages before storing download
                    if (dsBO.getTag().contains("draft")) {
                        adapter.delete(GTPackage.class, DBContract.GTPackageTable.SQL_WHERE_DRAFT_BY_LANGUAGE.args(dsBO.getLangCode()));
                    }

                    // save the parsed packages to database
                    for (GTPackage gtp : packageList) {
                        adapter.updateOrInsert(gtp);
                    }

                    // delete package.zip and contents.xml
                    zipfile.delete();
                    contentFile.delete();

                    // move files to main directory
                    FileInputStream inputStream;
                    FileOutputStream outputStream;

                    File[] fileList = tmpDir.listFiles();
                    File oldFile;
                    for (int i = 0; i < fileList.length; i++) {
                        oldFile = fileList[i];
                        inputStream = new FileInputStream(oldFile);
                        outputStream = new FileOutputStream(new File(FileUtils.getResourcesDir(), oldFile.getName()));

                        IOUtils.copy(inputStream, outputStream);

                        inputStream.close();
                        outputStream.flush();
                        outputStream.close();
                        oldFile.delete();
                    }
                    sendProgress(dsBO.getLangCode(), 100);
                    return true;
                }

            } else {
                Log.d(TAG, "server contact failed");
            }

        } catch (Exception e) {
            // log any other exceptions encountered
            Crashlytics.setString("downloadParameters", dsBO.getTag().toString());
            Crashlytics.setString("downloadResponseStatus", Integer.toString(downloadResponseCode));
            Crashlytics.setString("downloadContentLength", Integer.toString(downloadContentLength));

            Crashlytics.logException(e);
            return false;
        } finally {
            // delete unzip directory
            FileUtils.deleteRecursive(tmpDir, false);
        }
        return false;
    }

    private boolean writeResponseBodyToDisk(DownloadServiceBO dsBO, ResponseBody body, File outputFile) {
        try {

            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                byte[] fileReader = new byte[4096];

                long fileSize = body.contentLength();
                long fileSizeDownloaded = 0;

                inputStream = body.byteStream();
                outputStream = new FileOutputStream(outputFile);

                while (true) {
                    int read = inputStream.read(fileReader);

                    if (read == -1) {
                        break;
                    }

                    outputStream.write(fileReader, 0, read);

                    fileSizeDownloaded += read;
                    int percentDownloaded = Math.round((fileSizeDownloaded / fileSize) * 60);
                    sendProgress(dsBO.getLangCode(), percentDownloaded + 10);
                    Log.d(TAG, "file download: " + fileSizeDownloaded + " of " + fileSize);
                }

                outputStream.flush();

                return true;
            } catch (IOException e) {
                return false;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }

                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            return false;
        }
    }

}

//    public class DownloadTask extends AsyncTask<Object, Void, Boolean> {
//
//        private static final String TAG = "DownloadTask";
//        private org.keynote.godtools.android.http.DownloadTask.DownloadTaskHandler mTaskHandler;
//        private Context mContext;
//        @NonNull
//        private final File mResourcesDir;
//        private String url, filePath, tag, langCode;
//
//        public interface DownloadTaskHandler {
//            void downloadTaskComplete(String url, String filePath, String langCode, String tag);
//
//            void downloadTaskFailure(String url, String filePath, String langCode, String tag);
//        }
//
//        public DownloadTask(Context context, @NonNull final File resourcesDir, org.keynote.godtools.android.http.DownloadTask.DownloadTaskHandler taskHandler) {
//            this.mTaskHandler = taskHandler;
//            this.mContext = context;
//            mResourcesDir = resourcesDir;
//        }
//
//        @Override
//        protected Boolean doInBackground(Object... params) {
//            url = params[0].toString();
//            filePath = params[1].toString();
//            tag = params[2].toString();
//            String authorization = (String) params[3];
//            langCode = params[4].toString();
//            int downloadResponseCode = -1;
//            int downloadContentLength = -1;
//
//            // get a temporary directory for extracting the zip file to
//            final File tmpDir = FileUtils.getTmpDir(mContext);
//            final File zipfile = new File(tmpDir, "package.zip");
//            if (tmpDir == null) {
//                Crashlytics.logException(new Exception("unable to get temporary directory for download: " + url));
//                return false;
//            }
//
//            // download & extract zip file to tmp directory
//            try {
//                // download zip file
//
//                Response<ResponseBody> response = GodToolsApi.INSTANCE.downloadPackages(authorization, langCode).execute();
//                if (response.isSuccessful()) {
//                    Log.d(TAG, "server contacted and has file");
//                    boolean writtenToDisk = writeResponseBodyToDisk(response.body(), zipfile);
//                    Log.d(TAG, "file download was a success? " + writtenToDisk);
//                    if(writtenToDisk)
//                    {
//                        new Decompress().unzip(zipfile, tmpDir);
//
//                        // parse content.xml
//                        File contentFile = new File(tmpDir, "contents.xml");
//                        List<GTPackage> packageList = GTPackageReader.processContentFile(contentFile);
//
//                        DBAdapter adapter = DBAdapter.getInstance(mContext);
//
//                        // delete draft packages before storing download
//                        if (tag.contains("draft")) {
//                            adapter.delete(GTPackage.class, DBContract.GTPackageTable.SQL_WHERE_DRAFT_BY_LANGUAGE.args(langCode));
//                        }
//
//                        // save the parsed packages to database
//                        for (GTPackage gtp : packageList) {
//                            adapter.updateOrInsert(gtp);
//                        }
//
//                        // delete package.zip and contents.xml
//                        zipfile.delete();
//                        contentFile.delete();
//
//                        // move files to main directory
//                        FileInputStream inputStream;
//                        FileOutputStream outputStream;
//
//                        File[] fileList = tmpDir.listFiles();
//                        File oldFile;
//                        for (int i = 0; i < fileList.length; i++) {
//                            oldFile = fileList[i];
//                            inputStream = new FileInputStream(oldFile);
//                            outputStream = new FileOutputStream(new File(mResourcesDir, oldFile.getName()));
//
//                            IOUtils.copy(inputStream, outputStream);
//
//                            inputStream.close();
//                            outputStream.flush();
//                            outputStream.close();
//                            oldFile.delete();
//                        }
//
//                        return true;
//                    }
//
//                } else {
//                    Log.d(TAG, "server contact failed");
//                }
//
//
//            } catch (Exception e) {
//                // log any other exceptions encountered
//                Crashlytics.setString("url", url);
//                Crashlytics.setString("filePath", filePath);
//                Crashlytics.setString("tag", tag);
//                Crashlytics.setString("authorization", authorization);
//                Crashlytics.setString("langCode", langCode);
//                Crashlytics.setString("downloadResponseStatus", Integer.toString(downloadResponseCode));
//                Crashlytics.setString("downloadContentLength", Integer.toString(downloadContentLength));
//
//                Crashlytics.logException(e);
//                return false;
//            } finally {
//                // delete unzip directory
//                FileUtils.deleteRecursive(tmpDir, false);
//            }
//            return false;
//        }
//
//        private boolean writeResponseBodyToDisk(ResponseBody body, File outputFile) {
//            try {
//
//
//                InputStream inputStream = null;
//                OutputStream outputStream = null;
//
//                try {
//                    byte[] fileReader = new byte[4096];
//
//                    long fileSize = body.contentLength();
//                    long fileSizeDownloaded = 0;
//
//                    inputStream = body.byteStream();
//                    outputStream = new FileOutputStream(outputFile);
//
//                    while (true) {
//                        int read = inputStream.read(fileReader);
//
//                        if (read == -1) {
//                            break;
//                        }
//
//                        outputStream.write(fileReader, 0, read);
//
//                        fileSizeDownloaded += read;
//                        int percentDownloaded = Math.round((fileSizeDownloaded / fileSize) * 100);
//
//
//                        Log.d(TAG, "file download: " + fileSizeDownloaded + " of " + fileSize);
//                    }
//
//                    outputStream.flush();
//
//                    return true;
//                } catch (IOException e) {
//                    return false;
//                } finally {
//                    if (inputStream != null) {
//                        inputStream.close();
//                    }
//
//                    if (outputStream != null) {
//                        outputStream.close();
//                    }
//                }
//            } catch (IOException e) {
//                return false;
//            }
//        }
//
//
//        @Override
//        protected void onPostExecute(Boolean isSuccessful) {
//
//            if (isSuccessful)
//                mTaskHandler.downloadTaskComplete(url, filePath, langCode, tag);
//            else
//                mTaskHandler.downloadTaskFailure(url, filePath, langCode, tag);
//
//        }
//    }
//
//
//}
