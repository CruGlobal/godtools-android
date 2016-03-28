package org.keynote.godtools.android.http;

import android.os.AsyncTask;
import android.util.Log;

import com.google.common.net.HttpHeaders;

import org.keynote.godtools.android.api.GodToolsApi;

import java.io.IOException;
import java.net.HttpURLConnection;

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
        // verify the provided auth token
        if (verifyStatus)
        {

            Log.i(TAG, "verifying auth token");
            String authToken = params[1].toString();
            Log.i(TAG, "Token: " + authToken);

            try
            {
                final Response<ResponseBody> response = GodToolsApi.INSTANCE.verifyAuthToken(authToken).execute();
                statusCode = response.code();

                Log.i(TAG, "Auth Token Verified. Status Code: " + statusCode);

                return authToken;
            } catch (final IOException e)
            {
                e.printStackTrace();
                Log.e(TAG, "Status Code: " + statusCode);
                return null;
            }

        }
        // retrieve a new auth token
        else
        {
            try
            {
                final Response<ResponseBody> response =
                        GodToolsApi.INSTANCE.getAuthToken(params[1].toString()).execute();
                statusCode = response.code();
                return response.headers().get(HttpHeaders.AUTHORIZATION);
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

        if (statusCode == HttpURLConnection.HTTP_NO_CONTENT)
        {
            taskHandler.authComplete(s, authenticateAccessCode, verifyStatus);
        }
        else
        {
            taskHandler.authFailed(authenticateAccessCode, verifyStatus);
        }

    }
}
