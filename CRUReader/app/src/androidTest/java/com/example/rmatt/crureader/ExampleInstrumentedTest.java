package com.example.rmatt.crureader;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.example.rmatt.crureader.bo.GDocument.GDocument;
import com.example.rmatt.crureader.bo.GDocument.GDocumentPage;
import com.example.rmatt.crureader.bo.GPage.GPage;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    private static final String TAG = "ExampleInstrumentedTest";

    @Test
    public void xmlGPageTest() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();
        Log.i(TAG, "BaseXML: " + MainActivity.BASE_XML);

        try {

            //InputStream testIS = appContext.getResources().getAssets().open("test.xml");
            //Serializer serializer = new Persister();

            GPage GPageExample = XMLUtil.parseGPage(appContext, "test.xml"); //serializer.read(GPage.class, testIS);

        } catch (Exception e) {

            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
        assertEquals("com.example.rmatt.crureader", appContext.getPackageName());
    }

    @Test
    public void xmlGDocumentPageTest() throws Exception {

        Context appContext = InstrumentationRegistry.getTargetContext();
        Log.i(TAG, "BaseXML: " + MainActivity.BASE_XML);

        try {

            //InputStream testIS = appContext.getResources().getAssets().open("test.xml");
            //Serializer serializer = new Persister();

            GDocument GDocument = XMLUtil.parseGDocument(appContext, MainActivity.BASE_XML); //serializer.read(GPage.class, testIS);
            Assert.assertNotNull(GDocument);
            Assert.assertNotNull(GDocument.about);
            Assert.assertNotNull(GDocument.instructions);
            Assert.assertNotNull(GDocument.lang);
            Assert.assertNotNull(GDocument.packagename);
            Assert.assertNotNull(GDocument.pages);
            Assert.assertTrue(GDocument.pages.size() == 13);
            Assert.assertEquals(GDocument.pages.get(12).content.trim(), "Websites To Assist You\n".trim());
            Assert.assertEquals(GDocument.pages.get(12).gtapiTrxId, "6b7dc303-66a7-4abb-987b-636020e31b77");

        } catch (Exception e) {

            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
        assertEquals("com.example.rmatt.crureader", appContext.getPackageName());
    }


    @Test
    public void xmlAllTest() throws Exception {
        Context appContext = InstrumentationRegistry.getTargetContext();
        Log.i(TAG, "BaseXML: " + MainActivity.BASE_XML);

        try {

            //InputStream testIS = appContext.getResources().getAssets().open("test.xml");
            //Serializer serializer = new Persister();

            GDocument GDocument = XMLUtil.parseGDocument(appContext, MainActivity.BASE_XML); //serializer.read(GPage.class, testIS);
            Assert.assertNotNull(GDocument);
            Assert.assertNotNull(GDocument.about);
            Assert.assertNotNull(GDocument.instructions);
            Assert.assertNotNull(GDocument.lang);
            Assert.assertNotNull(GDocument.packagename);
            Assert.assertNotNull(GDocument.pages);
            Assert.assertTrue(GDocument.pages.size() == 13);
            Assert.assertEquals(GDocument.pages.get(12).content.trim(), "Websites To Assist You\n".trim());
            Assert.assertEquals(GDocument.pages.get(12).gtapiTrxId, "6b7dc303-66a7-4abb-987b-636020e31b77");
            for(GDocumentPage page : GDocument.pages)
            {
                GPage GPageExample = XMLUtil.parseGPage(appContext, page.filename); //serializer.read(GPage.class, testIS);
            }

        } catch (Exception e) {

            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
        assertEquals("com.example.rmatt.crureader", appContext.getPackageName());
    }



}
