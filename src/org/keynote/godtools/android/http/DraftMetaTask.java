package org.keynote.godtools.android.http;

import java.io.IOException;
import java.net.HttpURLConnection;

/**
 * Created by ryancarlson on 7/18/15.
 */
public class DraftMetaTask extends MetaTask
{

    private final String authorization;

    public DraftMetaTask(MetaTaskHandler handler, String authorization)
    {
        super(handler);
        this.authorization = authorization;
    }

    @Override
    protected HttpURLConnection getHttpURLConnection(String url) throws IOException
    {
        HttpURLConnection connection = super.getHttpURLConnection(url);
        connection.setRequestProperty("Authorization", authorization);
        return connection;
    }
}
