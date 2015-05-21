package org.keynote.godtools.android.http;

import android.content.Context;
import android.os.AsyncTask;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.keynote.godtools.android.business.GTPackage;
import org.keynote.godtools.android.business.GTPackageReader;
import org.keynote.godtools.android.dao.DBAdapter;
import org.keynote.godtools.android.snuffy.Decompress;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class DownloadTask extends AsyncTask<Object, Void, Boolean> {

    private DownloadTaskHandler mTaskHandler;
    private Context mContext;
    private String url, filePath, tag, langCode;

    public static interface DownloadTaskHandler {
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
        String authorization = params[3].toString();
        langCode = params[4].toString();

        try {
            HttpGet request = new HttpGet(url);
            request.setHeader("Accept", "application/xml");
            request.setHeader("Content-type", "application/xml");
            request.setHeader("Authorization", authorization);
            request.setHeader("Interpreter", "1");

            HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, 10000);
            HttpConnectionParams.setSoTimeout(httpParams, 30000);

            HttpClient httpClient = new DefaultHttpClient(httpParams);
            HttpResponse response;
            try
            {
                response = httpClient.execute(request);
            }
            catch (IOException e)
            {
                e.printStackTrace();
                return false;
            }

            DataInputStream dis = new DataInputStream(response.getEntity().getContent());

            File zipfile = new File(filePath);
            String parentDir = zipfile.getParent();
            File unzipDir = new File(parentDir);
            unzipDir.mkdirs();

            byte[] buffer = new byte[2048];
            int length;

            FileOutputStream fout = new FileOutputStream(zipfile);
            BufferedOutputStream bufferOut = new BufferedOutputStream(fout, buffer.length);

            while ((length = dis.read(buffer, 0, buffer.length)) != -1)
                bufferOut.write(buffer, 0, length);

            bufferOut.flush();
            bufferOut.close();

            dis.close();
            fout.close();

            // unzip package.zip
            new Decompress().unzip(zipfile, unzipDir);

            // parse content.xml
            String content = unzipDir + "/contents.xml";
            File contentFile = new File(content);
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

            // move files to main directory
            String mainDir = unzipDir.getParent();
            String resourcesDir = mainDir + "/resources";
            FileInputStream inputStream;
            FileOutputStream outputStream;

            File[] fileList = unzipDir.listFiles();
            File oldFile;
            for (int i = 0; i < fileList.length; i++) {
                oldFile = fileList[i];
                inputStream = new FileInputStream(oldFile);
                outputStream = new FileOutputStream(resourcesDir + File.separator + oldFile.getName());
                copyFile(inputStream, outputStream);

                inputStream.close();
                outputStream.flush();
                outputStream.close();
                oldFile.delete();
            }

            // delete unzip directory
            unzipDir.delete();

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    @Override
    protected void onPostExecute(Boolean isSuccessful) {

        if (isSuccessful)
            mTaskHandler.downloadTaskComplete(url, filePath, langCode, tag);
        else
            mTaskHandler.downloadTaskFailure(url, filePath, langCode, tag);

    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }
}
