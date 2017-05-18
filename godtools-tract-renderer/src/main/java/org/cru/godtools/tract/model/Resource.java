package org.cru.godtools.tract.model;

import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

import org.ccci.gto.android.common.util.XmlPullParserUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

import static org.cru.godtools.tract.Constants.XMLNS_MANIFEST;

public class Resource extends Base {
    static final String XML_RESOURCE = "resource";

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

    @WorkerThread
    private void parse(@NonNull final XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, XMLNS_MANIFEST, XML_RESOURCE);

        // discard any nested nodes
        XmlPullParserUtils.skipTag(parser);
    }
}
