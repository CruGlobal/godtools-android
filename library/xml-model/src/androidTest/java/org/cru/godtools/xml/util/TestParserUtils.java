package org.cru.godtools.xml.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;

import androidx.annotation.NonNull;
import androidx.test.platform.app.InstrumentationRegistry;

public final class TestParserUtils {
    public static XmlPullParser getParserForTestAsset(@NonNull final String name) throws Exception {
        final Context context = InstrumentationRegistry.getInstrumentation().getContext();
        final AssetManager assets = context.getAssets();

        // initialize pull parser
        final XmlPullParser parser = Xml.newPullParser();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
        parser.setInput(assets.open("tests/" + name), "UTF-8");
        parser.nextTag();

        return parser;
    }
}
