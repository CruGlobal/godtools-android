package org.cru.godtools.xml.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import org.ccci.gto.android.common.util.XmlPullParserUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

import static org.cru.godtools.xml.Constants.XMLNS_MANIFEST;

public class Category extends Base {
    static final String XML_CATEGORY = "category";
    private static final String XML_ID = "id";
    private static final String XML_BANNER = "banner";

    @Nullable
    private String mId;
    @Nullable
    private String mBanner;

    @NonNull
    @WorkerThread
    static Category fromXml(@NonNull final Manifest manifest, @NonNull final XmlPullParser parser)
            throws XmlPullParserException, IOException {
        final Category category = new Category(manifest);
        category.parse(parser);
        return category;
    }

    private Category(@NonNull final Manifest manifest) {
        super(manifest);
    }

    @Nullable
    public Resource getBanner() {
        return getResource(mBanner);
    }

    @Nullable
    public static Resource getBanner(@Nullable final Category category) {
        return category != null ? category.getBanner() : null;
    }

    @WorkerThread
    private void parse(@NonNull final XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, XMLNS_MANIFEST, XML_CATEGORY);

        mId = parser.getAttributeValue(null, XML_ID);
        mBanner = parser.getAttributeValue(null, XML_BANNER);

        // discard any nested nodes
        XmlPullParserUtils.skipTag(parser);
    }
}
