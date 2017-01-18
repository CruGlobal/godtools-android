package org.keynote.godtools.android.http;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.crashlytics.android.Crashlytics;
import com.google.common.base.Strings;

import org.ccci.gto.android.common.util.IOUtils;
import org.keynote.godtools.android.BuildConfig;
import org.keynote.godtools.android.business.GTPackage;
import org.keynote.godtools.android.business.GTPackageReader;
import org.keynote.godtools.android.dao.DBAdapter;
import org.keynote.godtools.android.dao.DBContract.GTPackageTable;
import org.keynote.godtools.android.snuffy.Decompress;
import org.keynote.godtools.renderer.crureader.bo.GPage.Util.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import static org.keynote.godtools.android.utils.Constants.INTERPRETER_HEADER;


public class DownloadTask extends AsyncTask<Object, Void, Boolean> {

    private DownloadTaskHandler mTaskHandler;
    private Context mContext;
    @NonNull
    private final File mResourcesDir;
    private String url, filePath, tag, langCode;

    public interface DownloadTaskHandler {
        void downloadTaskComplete(String url, String filePath, String langCode, String tag);

        void downloadTaskFailure(String url, String filePath, String langCode, String tag);
    }

    public DownloadTask(Context context, @NonNull final File resourcesDir, DownloadTaskHandler taskHandler) {
        this.mTaskHandler = taskHandler;
        this.mContext = context;
        mResourcesDir = resourcesDir;
    }

    @Override
    protected Boolean doInBackground(Object... params) {
        url = params[0].toString();
        filePath = params[1].toString();
        tag = params[2].toString();
        String authorization = (String) params[3];
        langCode = params[4].toString();
        int downloadResponseCode = -1;
        int downloadContentLength = -1;

        // get a temporary directory for extracting the zip file to
        final File tmpDir = FileUtils.getTmpDir(mContext);
        if (tmpDir == null) {
            Crashlytics.logException(new Exception("unable to get temporary directory for download: " + url));
            return false;
        }

        // download & extract zip file to tmp directory
        try {
            // download zip file
            HttpURLConnection connection = null;
            final File zipfile = new File(tmpDir, "package.zip");
            try {
                connection = getHttpURLConnection(url, authorization);
                downloadResponseCode = connection.getResponseCode();
                downloadContentLength = connection.getContentLength();

                // output zip file
                InputStream is = connection.getInputStream();
                FileOutputStream fout = new FileOutputStream(zipfile);
                IOUtils.copy(is, fout);
                is.close();
                fout.close();
            } catch (final IOException e) {
                // don't log IOExceptions when downloading, they will be tracked by newrelic
                return false;
            } finally {
                IOUtils.closeQuietly(connection);
            }

            // unzip package.zip
            new Decompress().unzip(zipfile, tmpDir);

            // parse content.xml
            File contentFile = new File(tmpDir, "contents.xml");
            List<GTPackage> packageList = GTPackageReader.processContentFile(contentFile);

            DBAdapter adapter = DBAdapter.getInstance(mContext);

            // delete draft packages before storing download
            if (tag.contains("draft")) {
                adapter.delete(GTPackage.class, GTPackageTable.SQL_WHERE_DRAFT_BY_LANGUAGE.args(langCode));
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
                outputStream = new FileOutputStream(new File(mResourcesDir, oldFile.getName()));

                IOUtils.copy(inputStream, outputStream);

                inputStream.close();
                outputStream.flush();
                outputStream.close();
                oldFile.delete();
            }

            return true;
        } catch (Exception e) {
            // log any other exceptions encountered
            Crashlytics.setString("url", url);
            Crashlytics.setString("filePath", filePath);
            Crashlytics.setString("tag", tag);
            Crashlytics.setString("authorization", authorization);
            Crashlytics.setString("langCode", langCode);
            Crashlytics.setString("downloadResponseStatus", Integer.toString(downloadResponseCode));
            Crashlytics.setString("downloadContentLength", Integer.toString(downloadContentLength));

            Crashlytics.logException(e);
            return false;
        } finally {
            // delete unzip directory
            FileUtils.deleteRecursive(tmpDir, false);
        }
    }

    private HttpURLConnection getHttpURLConnection(String url, String authorization) throws IOException
    {
        HttpURLConnection getDownloadUrlConnection = (HttpURLConnection) new URL(url).openConnection();
        getDownloadUrlConnection.setReadTimeout(90000 /* milliseconds */);
        getDownloadUrlConnection.setConnectTimeout(10000 /* milliseconds */);
        getDownloadUrlConnection.setRequestMethod("GET");
        getDownloadUrlConnection.setRequestProperty(INTERPRETER_HEADER, BuildConfig.INTERPRETER_VERSION);

        if(!Strings.isNullOrEmpty(authorization))
        {
            getDownloadUrlConnection.setRequestProperty("Authorization", authorization);
        }
        return getDownloadUrlConnection;
    }

    @Override
    protected void onPostExecute(Boolean isSuccessful) {

        if (isSuccessful)
            mTaskHandler.downloadTaskComplete(url, filePath, langCode, tag);
        else
            mTaskHandler.downloadTaskFailure(url, filePath, langCode, tag);

    }
}
