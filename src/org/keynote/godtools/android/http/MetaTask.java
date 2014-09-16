package org.keynote.godtools.android.http;

import android.os.AsyncTask;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.InputStream;

public class MetaTask extends AsyncTask<Object, Void, InputStream>
{

    private int statusCode;
    private String tag, langCode;
    private MetaTaskHandler metaTaskHandler;

    public static interface MetaTaskHandler
    {
        void metaTaskComplete(InputStream is, String langCode, String tag);

        void metaTaskFailure(InputStream is, String langCode, String tag);
    }

    public MetaTask(MetaTaskHandler listener)
    {
        metaTaskHandler = listener;
    }

    @Override
    protected InputStream doInBackground(Object... params)
    {

        String url = params[0].toString();
        String authorization = params[1].toString();
        langCode = params[2].toString();
        tag = params[3].toString();

        HttpGet request = new HttpGet(url);
        request.setHeader("Accept", "application/xml");
        request.setHeader("Content-type", "application/xml");
        request.setHeader("Authorization", authorization);
        request.setHeader("Interpreter", "1");

        HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, 10000);
        HttpConnectionParams.setSoTimeout(httpParams, 10000);

        HttpClient httpClient = new DefaultHttpClient(httpParams);

        try
        {
            HttpResponse response = httpClient.execute(request);
            statusCode = response.getStatusLine().getStatusCode();

            return response.getEntity().getContent();

        } catch (Exception e)
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
            metaTaskHandler.metaTaskComplete(inputStream, langCode, tag);
        }
        else
        {
            metaTaskHandler.metaTaskFailure(inputStream, langCode, tag);
        }
    }
}
