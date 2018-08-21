package org.cru.godtools.tract.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import org.ccci.gto.android.common.util.XmlPullParserUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

import static org.cru.godtools.tract.Constants.XMLNS_CONTENT;

public final class Image extends Content {
    static final String XML_IMAGE = "image";
    private static final String XML_RESOURCE = "resource";

    @Nullable
    private String mResource = null;

    private Image(@NonNull final Base parent) {
        super(parent);
    }

    @Nullable
    static Resource getResource(@Nullable final Image image) {
        return image != null ? image.getResource(image.mResource) : null;
    }

    @WorkerThread
    static Image fromXml(@NonNull final Base parent, @NonNull final XmlPullParser parser)
            throws IOException, XmlPullParserException {
        return new Image(parent).parse(parser);
    }

    @WorkerThread
    private Image parse(@NonNull final XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, XMLNS_CONTENT, XML_IMAGE);
        parseAttrs(parser);
        XmlPullParserUtils.skipTag(parser);
        return this;
    }

    @Override
    void parseAttrs(@NonNull final XmlPullParser parser) {
        super.parseAttrs(parser);
        mResource = parser.getAttributeValue(null, XML_RESOURCE);
    }
}
