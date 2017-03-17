package org.keynote.godtools.android.newnew.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import org.ccci.gto.android.common.util.IOUtils;
import org.keynote.godtools.android.api.GodToolsApi;
import org.keynote.godtools.android.business.GTLanguage;
import org.keynote.godtools.android.business.GTPackage;
import org.keynote.godtools.android.business.GTPackages;
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
                    List<GTPackage> packageList = GTPackages.processContentFile(new FileInputStream(contentFile));

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
                    GTLanguage language = new GTLanguage();
                    language.setLanguageCode(dsBO.getLangCode());
                    language.setDownloaded(true);
                    GodToolsDao.getInstance(this).updateAsync(language, DBContract.GTLanguageTable.COL_DOWNLOADED);
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
