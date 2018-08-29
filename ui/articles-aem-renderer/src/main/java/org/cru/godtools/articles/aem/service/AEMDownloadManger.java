package org.cru.godtools.articles.aem.service;

import android.net.Uri;
import android.support.annotation.VisibleForTesting;
import android.util.Log;


import org.apache.commons.io.IOUtils;
import org.cru.godtools.xml.model.Manifest;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;

import javax.net.ssl.HttpsURLConnection;

/**
 *
 * @author Gyasi Story
 */
public class AEMDownloadManger {
    private static final String TAG = "AEMDownloadManger";

    public static void loadAEMFromManifest(final Manifest manifest){

        // Verify that Manifest has AEM Articles (Should be checked already
        if (manifest.getAemImports() == null || manifest.getAemImports().size() <= 0){

            return;  // May need to throw an error to limit unnecessary calls.
        }


        for (Uri aemImports: manifest.getAemImports()){

            try {
                loadAemManifestIntoAemModel(manifest, aemImports);
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(TAG, "loadAEMFromManifest: ", e);
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "loadAEMFromManifest: ", e);
            } {

            }
        }

    }


    private static void loadAemManifestIntoAemModel(Manifest manifest, Uri aemImports) throws IOException, JSONException {

        JSONObject importJson = getJsonFromUri(aemImports);

        // Get Category out of Json
        for (Iterator<String> it = importJson.keys(); it.hasNext(); ) {
            String key = it.next();

        }
    }

    @VisibleForTesting
    public static JSONObject getJsonFromUri(Uri aemImports) throws IOException, JSONException {

        URL url = new URL("https://stage.cru.org/" + aemImports.getPath());
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        String results = IOUtils.toString(connection.getInputStream());
        return new JSONObject(results);
    }


}
