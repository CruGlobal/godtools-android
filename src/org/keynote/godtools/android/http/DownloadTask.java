package org.keynote.godtools.android.http;

import android.os.AsyncTask;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class DownloadTask extends AsyncTask<Object, Void, Boolean>{

    private DownloadTaskHandler taskHandler;
    private String url, filePath, tag;

    public static interface  DownloadTaskHandler {
        void downloadTaskComplete(String url, String filePath, String tag);
        void downloadTaskFailure(String url, String filePath, String tag);
    }

    public DownloadTask(DownloadTaskHandler taskHandler) {
        this.taskHandler = taskHandler;
    }

    @Override
    protected Boolean doInBackground(Object... params) {

        url = params[0].toString();
        filePath = params[1].toString();
        tag = params[2].toString();

        try {
            URL mURL = new URL(url);
            InputStream is = mURL.openStream();
            DataInputStream dis = new DataInputStream(is);

            File file = new File(filePath);
            byte[] buffer = new byte[2048];
            int length;

            FileOutputStream fout = new FileOutputStream(file);
            BufferedOutputStream bufferOut = new BufferedOutputStream(fout, buffer.length);

            while ((length = dis.read(buffer, 0, buffer.length)) != -1)
                bufferOut.write(buffer, 0, length);

            bufferOut.flush();
            bufferOut.close();

            dis.close();
            fout.close();

            return true;

        } catch (MalformedURLException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean isSuccessful) {

        if (isSuccessful)
            taskHandler.downloadTaskComplete(url, filePath, tag);
        else
            taskHandler.downloadTaskFailure(url, filePath, tag);

    }
}
