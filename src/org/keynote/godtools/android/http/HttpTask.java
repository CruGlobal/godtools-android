package org.keynote.godtools.android.http;

import android.os.AsyncTask;

import java.io.InputStream;

public class HttpTask extends AsyncTask<Object, Void, InputStream> {

    protected HttpTaskHandler taskHandler;
    protected int statusCode;

    public static interface HttpTaskHandler {
        void httpTaskComplete(String url, InputStream is, int statusCode, String tag);

        void httpTaskFailure(String url, InputStream is, int statusCode, String tag);
    }

    @Override
    protected InputStream doInBackground(Object... objects) {
        return null;
    }
}
