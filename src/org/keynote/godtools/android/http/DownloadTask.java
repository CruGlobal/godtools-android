package org.keynote.godtools.android.http;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.crashlytics.android.Crashlytics;
import com.google.common.base.Strings;

import org.keynote.godtools.android.business.GTPackage;
import org.keynote.godtools.android.business.GTPackageReader;
import org.keynote.godtools.android.dao.DBAdapter;
import org.keynote.godtools.android.snuffy.Decompress;
import org.keynote.godtools.android.utils.FileUtils;
import org.keynote.godtools.android.utils.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

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
        HttpURLConnection conn = null;
        try {
            conn = getHttpURLConnection(url, authorization);
            downloadResponseCode = conn.getResponseCode();
            downloadContentLength = conn.getContentLength();

            final File zipfile = new File(tmpDir, "package.zip");

            // output zip file
            InputStream is = conn.getInputStream();
            FileOutputStream fout = new FileOutputStream(zipfile);
            IOUtils.copy(is, fout);
            is.close();
            fout.close();

            // unzip package.zip
            new Decompress().unzip(zipfile, tmpDir);

            // parse content.xml
            File contentFile = new File(tmpDir, "contents.xml");
            List<GTPackage> packageList = GTPackageReader.processContentFile(contentFile);

            DBAdapter adapter = DBAdapter.getInstance(mContext);
            adapter.open();

            // delete packages
            if (tag.contains("draft")) {
                adapter.deletePackages(langCode, "draft");
            }

            // save the parsed packages to database
            for (GTPackage gtp : packageList) {
                adapter.upsertGTPackage(gtp);
            }

            // delete package.zip and contents.xml
            zipfile.delete();
            contentFile.delete();

            // make sure resources directory exists
            // XXX: should we handle this globally elsewhere?
            if (!mResourcesDir.isDirectory() && !mResourcesDir.mkdirs()) {
                throw new RuntimeException("Unable to create resources directory");
            }

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
            IOUtils.closeQuietly(conn);

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
