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
import org.keynote.godtools.android.api.GodToolsApi;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Response;

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
                HttpResponse response = httpClient.execute(request);
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
            try
            {
                final Response<ResponseBody> response = GodToolsApi.INSTANCE.getAuthToken().execute();
                statusCode = response.code();
                return response.headers().get("Authorization");
            } catch (final IOException e)
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
