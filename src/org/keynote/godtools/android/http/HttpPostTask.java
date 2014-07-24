package org.keynote.godtools.android.http;

import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;

public class HttpPostTask extends HttpTask {

    private String url;
    private String authorization;
    private String tag;

    public HttpPostTask(HttpTaskHandler listener) {
        taskHandler = listener;
    }

    @Override
    protected InputStream doInBackground(Object... params) {

        url = params[0].toString();
        authorization = params[1].toString();
        tag = params[2].toString();

        HttpPost request = new HttpPost(url);
        request.setHeader("Accept", "application/xml");
        request.setHeader("Content-type", "application/xml");
        request.setHeader("Authorization", authorization);


        HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, 30000);

        HttpClient httpClient = new DefaultHttpClient(httpParams);

        try {
            HttpResponse response = httpClient.execute(request);
            statusCode = response.getStatusLine().getStatusCode();

            Log.i("authtoken", response.getFirstHeader("Authtoken").getValue());

            return response.getEntity().getContent();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(InputStream is) {

        if (statusCode == HttpStatus.SC_NO_CONTENT) {
            taskHandler.httpTaskComplete(url, is, statusCode, tag);
        } else {
            taskHandler.httpTaskFailure(url, is, statusCode, tag);
        }

    }
}
