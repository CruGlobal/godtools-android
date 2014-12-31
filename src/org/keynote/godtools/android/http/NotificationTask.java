package org.keynote.godtools.android.http;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

/**
 * Created by matthewfrederick on 12/31/14.
 */
public class NotificationTask extends AsyncTask<Object, Void, String>
{
    private NotificationTaskHandler taskHandler;
    private int statusCode;

    public static interface NotificationTaskHandler
    {
        void registrationComplete(String regId);

        void registrationFailed();
    }

    public NotificationTask(NotificationTaskHandler listener)
    {
        taskHandler = listener;
    }

    @Override
    protected String doInBackground(Object... objects)
    {
        String url = objects[0].toString();

        HttpPost request = new HttpPost(url);
        HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, 10000);
        HttpConnectionParams.setSoTimeout(httpParams, 10000);

        HttpClient httpClient = new DefaultHttpClient(httpParams);

        try
        {
            HttpResponse response = httpClient.execute(request);
            statusCode = response.getStatusLine().getStatusCode();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }

        return null;
    }

    @Override
    protected void onPostExecute(String s)
    {
        super.onPostExecute(s);

        if (statusCode == HttpStatus.SC_OK) taskHandler.registrationComplete("Complete");
        else taskHandler.registrationFailed();

        Log.i("Notification Task", "Code: " + statusCode);
    }
}
