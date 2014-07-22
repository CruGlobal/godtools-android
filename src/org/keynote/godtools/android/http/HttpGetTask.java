package org.keynote.godtools.android.http;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.IOException;
import java.io.InputStream;

public class HttpGetTask extends HttpTask {

    private String url;
    private String tag;
    private String authorization;

    public HttpGetTask(HttpTaskHandler listener) {
        taskHandler = listener;
    }

    @Override
    protected InputStream doInBackground(Object... params) {

        url = params[0].toString();
        authorization = params[1].toString();
        tag = params[2].toString();

        HttpGet request = new HttpGet(url);
        request.setHeader("Accept", "application/xml");
        request.setHeader("Content-type", "application/xml");
        request.setHeader("Authorization", authorization);
        request.setHeader("Interpreter", "1");

        HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, 10000);

        HttpClient httpClient = new DefaultHttpClient(httpParams);

        try {
            HttpResponse response = httpClient.execute(request);
            statusCode = response.getStatusLine().getStatusCode();

            return response.getEntity().getContent();

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }

    @Override
    protected void onPostExecute(InputStream is) {

        if (statusCode == HttpStatus.SC_OK) {
            taskHandler.httpTaskComplete(url, is, statusCode, tag);
        } else {
            taskHandler.httpTaskFailure(url, is, statusCode, tag);
        }
    }
}
