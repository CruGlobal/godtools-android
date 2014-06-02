package org.keynote.godtools.android.http;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class HttpGetTask extends HttpTask{

    private String url;
    private String tag;

    public HttpGetTask(HttpTaskHandler listener){
        taskHandler = listener;
    }

    @Override
    protected String doInBackground(Object... params) {

        url = params[0].toString();
        tag = params[1].toString();

        HttpGet request = new HttpGet(url);
        request.setHeader("Accept", "application/xml");
        request.setHeader("Content-type", "application/xml");

        HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, 30000);

        HttpClient httpClient = new DefaultHttpClient(httpParams);

        try {
            HttpResponse response = httpClient.execute(request);
            statusCode = response.getStatusLine().getStatusCode();

            BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
            StringBuilder builder = new StringBuilder();
            for (String line; (line = reader.readLine()) != null;) {
                builder.append(line).append("\n");
            }

            return builder.toString();

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }

    @Override
    protected void onPostExecute(String xmlString) {

        if (statusCode == HttpStatus.SC_OK){
            taskHandler.httpTaskComplete(url, xmlString, statusCode, tag);
        } else {
            taskHandler.httpTaskFailure(url, xmlString, statusCode, tag);
        }
    }
}
