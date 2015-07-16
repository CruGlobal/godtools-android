package org.keynote.godtools.android.http;

import android.os.AsyncTask;

import org.apache.http.HttpStatus;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MetaTask extends AsyncTask<Object, Void, InputStream>
{

    private int statusCode;
    private String tag;
    private MetaTaskHandler metaTaskHandler;

    public interface MetaTaskHandler
    {
        void metaTaskComplete(InputStream is, String tag);

        void metaTaskFailure(InputStream is, String tag, int statusCode);
    }

    public MetaTask(MetaTaskHandler listener)
    {
        metaTaskHandler = listener;
    }

    @Override
    protected InputStream doInBackground(Object... params)
    {
        tag = params[1].toString();

        String url = params[0].toString();

        try
        {
            HttpURLConnection getDownloadUrlConnection = getHttpURLConnection(url);

            getDownloadUrlConnection.connect();

            String locationHeader = getDownloadUrlConnection.getHeaderField("Location");

            HttpURLConnection downloadMetaFileConnection = getHttpURLConnection(locationHeader);

            downloadMetaFileConnection.connect();

            statusCode = downloadMetaFileConnection.getResponseCode();

            return downloadMetaFileConnection.getInputStream();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(InputStream inputStream)
    {
        if (statusCode == HttpStatus.SC_OK) 
        {
            metaTaskHandler.metaTaskComplete(inputStream, tag);
        }
        else
        {
            metaTaskHandler.metaTaskFailure(inputStream, tag, statusCode);
        }
    }

    private HttpURLConnection getHttpURLConnection(String url) throws IOException
    {
        HttpURLConnection getDownloadUrlConnection = (HttpURLConnection) new URL(url).openConnection();
        getDownloadUrlConnection.setReadTimeout(10000 /* milliseconds */);
        getDownloadUrlConnection.setConnectTimeout(15000 /* milliseconds */);
        getDownloadUrlConnection.setRequestMethod("GET");
        return getDownloadUrlConnection;
    }
}
