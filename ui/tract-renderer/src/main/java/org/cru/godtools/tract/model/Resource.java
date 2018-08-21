package org.cru.godtools.tract.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import org.ccci.gto.android.common.util.XmlPullParserUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

import static org.cru.godtools.xml.Constants.XMLNS_MANIFEST;

public final class Resource extends Base {
    static final String XML_RESOURCE = "resource";
    private static final String XML_FILENAME = "filename";
    private static final String XML_SRC = "src";

    @Nullable
    private String mName;
    @Nullable
    private String mLocalName;

    @NonNull
    @WorkerThread
    static Resource fromXml(@NonNull final Manifest manifest, @NonNull final XmlPullParser parser)
            throws XmlPullParserException, IOException {
        final Resource resource = new Resource(manifest);
        resource.parse(parser);
        return resource;
    }

    private Resource(@NonNull final Manifest manifest) {
        super(manifest);
    }

    @Nullable
    public String getName() {
        return mName;
    }

    @Nullable
    public String getLocalName() {
        return mLocalName;
    }

    @WorkerThread
    private void parse(@NonNull final XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, XMLNS_MANIFEST, XML_RESOURCE);

        mName = parser.getAttributeValue(null, XML_FILENAME);
        mLocalName = parser.getAttributeValue(null, XML_SRC);

        // discard any nested nodes
        XmlPullParserUtils.skipTag(parser);
    }
}
