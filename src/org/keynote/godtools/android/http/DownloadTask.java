package org.keynote.godtools.android.http;

import android.content.Context;
import android.os.AsyncTask;

import com.crashlytics.android.Crashlytics;
import com.google.common.base.Strings;

import org.keynote.godtools.android.business.GTPackage;
import org.keynote.godtools.android.business.GTPackageReader;
import org.keynote.godtools.android.dao.DBAdapter;
import org.keynote.godtools.android.snuffy.Decompress;
import org.keynote.godtools.android.utils.IOUtils;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class DownloadTask extends AsyncTask<Object, Void, Boolean> {

    private DownloadTaskHandler mTaskHandler;
    private Context mContext;
    private String url, filePath, tag, langCode;

    public interface DownloadTaskHandler {
        void downloadTaskComplete(String url, String filePath, String langCode, String tag);

        void downloadTaskFailure(String url, String filePath, String langCode, String tag);
    }

    public DownloadTask(Context context, DownloadTaskHandler taskHandler) {
        this.mTaskHandler = taskHandler;
        this.mContext = context;
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

        try {

            HttpURLConnection connection = getHttpURLConnection(url, authorization);

            connection.connect();

            downloadResponseCode = connection.getResponseCode();
            downloadContentLength = connection.getContentLength();

            DataInputStream dis = new DataInputStream(connection.getInputStream());

            File zipfile = new File(filePath);

            // get the temporary zip directory
            File unzipDir = zipfile.getParentFile();
            if (!unzipDir.isDirectory() && !unzipDir.mkdirs())
            {
                throw new RuntimeException("Unable to create zip download directory");
            }

            // output zip file
            FileOutputStream fout = new FileOutputStream(zipfile);
            IOUtils.copy(dis, fout);
            dis.close();
            fout.close();

            // unzip package.zip
            new Decompress().unzip(zipfile, unzipDir);

            // parse content.xml
            File contentFile = new File(unzipDir, "contents.xml");
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

            // get resources directory
            final File resourcesDir = new File(unzipDir.getParentFile(), "resources");
            if (!resourcesDir.isDirectory() && !resourcesDir.mkdirs()) {
                throw new RuntimeException("Unable to create resources directory");
            }

            // move files to main directory
            FileInputStream inputStream;
            FileOutputStream outputStream;

            File[] fileList = unzipDir.listFiles();
            File oldFile;
            for (int i = 0; i < fileList.length; i++) {
                oldFile = fileList[i];
                inputStream = new FileInputStream(oldFile);
                outputStream = new FileOutputStream(new File(resourcesDir, oldFile.getName()));

                IOUtils.copy(inputStream, outputStream);

                inputStream.close();
                outputStream.flush();
                outputStream.close();
                oldFile.delete();
            }

            // delete unzip directory
            unzipDir.delete();

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
