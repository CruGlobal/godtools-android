package org.keynote.godtools.android.http;

import android.os.AsyncTask;

public class HttpTask extends AsyncTask<Object, Void, String>{

    protected HttpTaskHandler taskHandler;
    protected int statusCode;

    public static interface HttpTaskHandler {
        void httpTaskComplete(String url, String xmlString, int statusCode, String tag);
        void httpTaskFailure(String url, String xmlString, int statusCode, String tag);
    }

    @Override
    protected String doInBackground(Object... objects) {
        return null;
    }
}
