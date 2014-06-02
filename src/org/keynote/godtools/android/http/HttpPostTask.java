package org.keynote.godtools.android.http;

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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

public class HttpPostTask extends HttpTask{

    private String url;
    private JSONObject data;
    private String tag;

    public HttpPostTask(HttpTaskHandler listener) {
        taskHandler = listener;
    }

    @Override
    protected String doInBackground(Object... params) {

        url = params[0].toString();
        data = (JSONObject)params[1];
        tag = params[2].toString();

        HttpPost request = new HttpPost(url);
        request.setHeader("Accept", "application/xml");
        request.setHeader("Content-type", "application/xml");

        if (data != null) {
            try {
                StringEntity se = new StringEntity(data.toString());
                // sets the post request as the resulting string
                request.setEntity(se);
            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
            }
        }

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

        } catch (Exception e){
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
