package org.keynote.godtools.android.http;

import android.os.AsyncTask;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.IOException;

/**
 * Created by ryancarlson on 9/10/14.
 */
public class DraftPublishTask extends AsyncTask<Object, Void, Integer>
{
    private final DraftTaskHandler taskHandler;

    public DraftPublishTask(DraftTaskHandler taskHandler)
    {
        this.taskHandler = taskHandler;
    }

    public static interface DraftTaskHandler
    {
        void draftTaskComplete();

        void draftTaskFailure();
    }

    @Override
    protected Integer doInBackground(Object... params)
    {
        String url = params[0].toString() + "?publish=true";
        String authorization = params[1].toString();

        HttpPut request = new HttpPut(url);
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
            return httpClient.execute(request).getStatusLine().getStatusCode();
        } catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(Integer responseStatusCode)
    {
        if (responseStatusCode.equals(204))
        {
            taskHandler.draftTaskComplete();
        }
        else
        {
            taskHandler.draftTaskFailure();
        }
    }
}