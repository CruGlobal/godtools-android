package org.keynote.godtools.android.http;

import android.os.AsyncTask;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

public class AuthTask extends AsyncTask<Object, Void, String> {

    private AuthTaskHandler taskHandler;
    private int statusCode;

    public static interface AuthTaskHandler {
        void authComplete(String authorization);

        void authFailed();
    }

    public AuthTask(AuthTaskHandler listener) {
        taskHandler = listener;
    }

    @Override
    protected String doInBackground(Object... params) {

        String url = params[0].toString();

        HttpPost request = new HttpPost(url);
        HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, 10000);
        HttpConnectionParams.setSoTimeout(httpParams, 10000);

        HttpClient httpClient = new DefaultHttpClient(httpParams);

        try {
            HttpResponse response = httpClient.execute(request);
            statusCode = response.getStatusLine().getStatusCode();

            return response.getFirstHeader("Authorization").getValue();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);

        if (statusCode == HttpStatus.SC_NO_CONTENT) {
            taskHandler.authComplete(s);
        } else {
            taskHandler.authFailed();
        }

    }
}
