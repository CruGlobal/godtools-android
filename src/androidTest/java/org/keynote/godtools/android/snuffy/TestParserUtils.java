package org.keynote.godtools.android.snuffy;

import android.content.Context;
import android.content.res.AssetManager;
import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;
import android.util.Xml;

import org.w3c.dom.Element;
import org.xmlpull.v1.XmlPullParser;

import javax.xml.parsers.DocumentBuilderFactory;

public class TestParserUtils {
    public static XmlPullParser getParserForTestAsset(@NonNull final String name) throws Exception {
        final Context context = InstrumentationRegistry.getContext();
        final AssetManager assets = context.getAssets();

        // initialize pull parser
        final XmlPullParser parser = Xml.newPullParser();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
        parser.setInput(assets.open("tests/" + name), "UTF-8");
        parser.nextTag();

        return parser;
    }

    public static Element getRootElementForTestAsset(@NonNull final String name) throws Exception {
        final Context context = InstrumentationRegistry.getContext();
        final AssetManager assets = context.getAssets();

        return DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(assets.open("tests/" + name))
                .getDocumentElement();
    }
}
