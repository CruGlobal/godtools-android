package org.keynote.godtools.android.http;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.IOException;

import static org.keynote.godtools.android.BuildConfig.INTERPRETER;

/**
 * Created by ryancarlson on 9/10/14.
 */
public class DraftCreationTask extends AsyncTask<Object, Void, Integer>
{
    private final String TAG = getClass().getSimpleName();
    
    private final DraftTaskHandler taskHandler;

    public DraftCreationTask(DraftTaskHandler taskHandler)
    {
        this.taskHandler = taskHandler;
    }

    public interface DraftTaskHandler
    {
        void draftTaskComplete();
        void draftTaskFailure(int code);
    }

    @Override
    protected Integer doInBackground(Object... params)
    {
        String url = params[0].toString();
        String authorization = params[1].toString();


        HttpPost request = new HttpPost(url);
        request.setHeader("Accept", "application/xml");
        request.setHeader("Content-type", "application/xml");
        request.setHeader("Authorization", authorization);
        request.setHeader("interpreter", INTERPRETER);

        HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, 10000);
        HttpConnectionParams.setSoTimeout(httpParams, 30000);

        HttpClient httpClient = new DefaultHttpClient(httpParams);

        try
        {
            return httpClient.execute(request).getStatusLine().getStatusCode();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(Integer responseStatusCode)
    {
        Log.i(TAG, "Response Code: " + responseStatusCode);
        
        if(responseStatusCode.equals(201))
        {
            taskHandler.draftTaskComplete();
        }
        else
        {
            taskHandler.draftTaskFailure(responseStatusCode);
        }
    }
}
