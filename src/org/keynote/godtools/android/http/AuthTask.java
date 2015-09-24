package org.keynote.godtools.android.http;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

public class AuthTask extends AsyncTask<Object, Void, String>
{

    private AuthTaskHandler taskHandler;
    private int statusCode;
    private static final String TAG = AuthTask.class.getSimpleName();

    // lets the task know that the access code is being authenticated
    private boolean authenticateAccessCode = false;

    // lets the task know that the access code status is being verified
    private boolean verifyStatus = false;

    public interface AuthTaskHandler
    {
        void authComplete(String authorization, boolean authenticateAccessCode, boolean verifyStatus);

        void authFailed(boolean authenticateAccessCode, boolean verifyStatus);
    }

    public AuthTask(AuthTaskHandler listener, boolean authenticateAccessCode, boolean verifyStatus)
    {
        taskHandler = listener;
        this.authenticateAccessCode = authenticateAccessCode;
        this.verifyStatus = verifyStatus;
    }

    @Override
    protected String doInBackground(Object... params)
    {

        String url = params[0].toString();
        HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, 10000);
        HttpConnectionParams.setSoTimeout(httpParams, 10000);

        HttpClient httpClient = new DefaultHttpClient(httpParams);

        HttpResponse response;

        if (verifyStatus)
        {
            Log.i(TAG, "verifying auth token");
            String authToken = params[1].toString();
            Log.i(TAG, "Token: " + authToken);

            url = url + "status";

            HttpGet request = new HttpGet(url);
            request.setHeader("Authorization", authToken);
            try
            {
                response = httpClient.execute(request);
                statusCode = response.getStatusLine().getStatusCode();

                Log.i(TAG, "Auth Token Verified. Status Code: " + statusCode);

                return authToken;
            } catch (Exception e)
            {
                e.printStackTrace();
                Log.e(TAG, "Status Code: " + statusCode);
                return null;
            }

        }
        else
        {
            HttpPost request = new HttpPost(url);
            try
            {
                response = httpClient.execute(request);
                statusCode = response.getStatusLine().getStatusCode();

                return response.getFirstHeader("Authorization").getValue();
            } catch (Exception e)
            {
                e.printStackTrace();
                Log.e(TAG, "Status Code: " + statusCode);
                return null;
            }
        }
    }

    @Override
    protected void onPostExecute(String s)
    {
        super.onPostExecute(s);

        if (statusCode == HttpStatus.SC_NO_CONTENT)
        {
            taskHandler.authComplete(s, authenticateAccessCode, verifyStatus);
        }
        else
        {
            taskHandler.authFailed(authenticateAccessCode, verifyStatus);
        }

    }
}
