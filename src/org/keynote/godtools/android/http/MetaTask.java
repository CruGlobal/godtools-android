package org.keynote.godtools.android.http;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.InputStream;

public class MetaTask extends AsyncTask<Object, Void, InputStream> {

    private int statusCode;
    private String tag;
    private MetaTaskHandler metaTaskHandler;

    public interface MetaTaskHandler {
        void metaTaskComplete(InputStream is, String tag);

        void metaTaskFailure(InputStream is, String tag, int statusCode);
    }

    public MetaTask(MetaTaskHandler listener) {
        metaTaskHandler = listener;
    }

    @Override
    protected InputStream doInBackground(Object... params) {

        String url = params[0].toString();
        tag = params[3].toString();

        HttpGet getDownloadUrlRequest = new HttpGet(url);

        HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, 10000);
        HttpConnectionParams.setSoTimeout(httpParams, 10000);

        HttpClient httpClient = new DefaultHttpClient(httpParams);

        try
        {
            HttpResponse getDownloadUrlResponse = httpClient.execute(getDownloadUrlRequest);
            statusCode = getDownloadUrlResponse.getStatusLine().getStatusCode();

            Log.i("MetaTask", "Status: " + statusCode);

            HttpGet getMetaFileRequest = new HttpGet(getDownloadUrlResponse.getFirstHeader("Location").getValue());

            HttpResponse getMetaFileResponse = httpClient.execute(getMetaFileRequest);

            statusCode = getMetaFileResponse.getStatusLine().getStatusCode();

            return getMetaFileResponse.getEntity().getContent();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(InputStream inputStream) {

        if (statusCode == HttpStatus.SC_OK) 
        {
            metaTaskHandler.metaTaskComplete(inputStream, tag);
        } else 
        {
            metaTaskHandler.metaTaskFailure(inputStream, tag, statusCode);
        }
    }
}
