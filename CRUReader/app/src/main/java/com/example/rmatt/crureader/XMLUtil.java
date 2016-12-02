package com.example.rmatt.crureader;

import android.content.Context;

import com.example.rmatt.crureader.bo.GDocument.GDocument;
import com.example.rmatt.crureader.bo.GPage.GPage;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.InputStream;

/**
 * Created by rmatt on 10/24/2016.
 */

public class XMLUtil {

    public static GPage parseGPage(Context context, String xmlFileName) throws Exception {
        InputStream testIS = context.getResources().getAssets().open(xmlFileName);
        Serializer serializer = new Persister();
        GPage GPage = serializer.read(GPage.class, testIS);
        return GPage;
    }


    public static GDocument parseGDocument(Context appContext, String baseXml) throws Exception {

        InputStream testIS = appContext.getResources().getAssets().open(baseXml);
        Serializer serializer = new Persister();
        GDocument GDocument = serializer.read(GDocument.class, testIS);
        return GDocument;
    }
}
