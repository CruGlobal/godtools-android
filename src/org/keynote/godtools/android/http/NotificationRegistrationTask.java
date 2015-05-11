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
public class NotificationRegistrationTask extends AsyncTask<Object, Void, String>
{
    private NotificationTaskHandler taskHandler;
    private int statusCode;
    private String TAG = "NotificationRegistrationTask";

    public static interface NotificationTaskHandler
    {
        void registrationComplete(String regId);

        void registrationFailed();
    }

    public NotificationRegistrationTask(NotificationTaskHandler listener)
    {
        taskHandler = listener;
    }

    @Override
    protected String doInBackground(Object... objects)
    {
        String url = objects[0].toString();
        String deviceId = objects[1].toString();

        HttpPost request = new HttpPost(url);
        Log.i(TAG, url);
        Log.i(TAG, deviceId);
        request.setHeader("deviceId", deviceId);
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

        Log.i(TAG, "Code: " + statusCode);
    }
}
