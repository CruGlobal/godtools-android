package org.keynote.godtools.android.http;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

/**
 * Created by matthewfrederick on 1/5/15.
 */
public class NotificationUpdateTask extends AsyncTask<Object, Void, String>
{
    private NotificationTaskHandler taskHandler;
    private int statusCode;
    private String TAG = "NotificationUpdateTask";

    public static interface NotificationTaskHandler
    {
        void registrationComplete(String regId);

        void registrationFailed();
    }

    public NotificationUpdateTask(NotificationTaskHandler listener)
    {
        taskHandler = listener;
    }

    @Override
    protected String doInBackground(Object... objects)
    {
        String url = objects[0].toString();
        String authcode = objects[1].toString();

        JSONObject jsonObject = new JSONObject();
        try
        {
            jsonObject.put("id", null); // done by api
            jsonObject.put("registrationId", objects[2].toString());
            jsonObject.put("notificationType", objects[3].toString());
            jsonObject.put("notificationSent", false);
            jsonObject.put("createdTimestamp", null); // done by api


            HttpPost request = new HttpPost(url);
            Log.i(TAG, url);
            request.setHeader("Authorization", authcode);

            StringEntity stringEntity = new StringEntity(jsonObject.toString());
            stringEntity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
            Log.i(TAG, stringEntity.toString());

            request.setEntity(stringEntity);

            HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, 10000);
            HttpConnectionParams.setSoTimeout(httpParams, 10000);

            HttpClient httpClient = new DefaultHttpClient(httpParams);

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
