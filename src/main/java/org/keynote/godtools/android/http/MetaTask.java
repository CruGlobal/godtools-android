package org.keynote.godtools.android.http;

//public class MetaTask extends AsyncTask<Object, Void, List<GTLanguage>>
//{
//
//    private int statusCode;
//    private String tag;
//    private MetaTaskHandler metaTaskHandler;
//
//    public interface MetaTaskHandler
//    {
//        void metaTaskComplete(List<GTLanguage> languageList, String tag);
//
//        void metaTaskFailure(List<GTLanguage> languageList, String tag, int statusCode);
//    }
//
//    public MetaTask(MetaTaskHandler listener)
//    {
//        metaTaskHandler = listener;
//    }
//
//    @Override
//    protected List<GTLanguage> doInBackground(Object... params)
//    {
//        tag = params[1].toString();
//
//        String url = params[0].toString();
//
//        try
//        {
//            HttpURLConnection getDownloadUrlConnection = getHttpURLConnection(url);
//            getDownloadUrlConnection.connect();
//            statusCode = getDownloadUrlConnection.getResponseCode();
//            Map<String, List<String>> headerMap = getDownloadUrlConnection.getHeaderFields();
//            Log.i("APICheck", "Determine headers");
//            for(String key: headerMap.keySet())
//            {
//                Log.i("APICHECK", "Key: " + key);
//                for(String value : headerMap.get(key))
//                {
//                    Log.i("APICHECK", "Value: " + value);
//                }
//            }
//            return GTPackageReader.processMetaResponse(getDownloadUrlConnection.getInputStream());
//        }
//        catch (Exception e)
//        {
//            // ensure that failure code is exectued in "onPostExecute"
//            statusCode = 502;
//            e.printStackTrace();
//            return null;
//        }
//    }
//
//    @Override
//    protected void onPostExecute(List<GTLanguage> languageList)
//    {
//        if (statusCode == HttpURLConnection.HTTP_OK)
//        {
//            metaTaskHandler.metaTaskComplete(languageList, tag);
//        }
//        else
//        {
//            metaTaskHandler.metaTaskFailure(languageList, tag, statusCode);
//        }
//    }
//
//    protected HttpURLConnection getHttpURLConnection(String url) throws IOException
//    {
//        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
//        connection.setReadTimeout(10000 /* milliseconds */);
//        connection.setConnectTimeout(15000 /* milliseconds */);
//        connection.setRequestMethod("GET");
//        connection.setRequestProperty(INTERPRETER_HEADER, BuildConfig.INTERPRETER_VERSION);
//
//        return connection;
//    }
//}
