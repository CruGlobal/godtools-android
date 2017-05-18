package org.cru.godtools.tract.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import org.ccci.gto.android.common.util.XmlPullParserUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

import static org.cru.godtools.tract.Constants.XMLNS_MANIFEST;

public class Page extends Base {
    static final String XML_PAGE = "page";
    private static final String XML_MANIFEST_SRC = "src";

    @Nullable
    private String mLocalFileName;

    @NonNull
    @WorkerThread
    static Page fromManifestXml(@NonNull final Manifest manifest, @NonNull final XmlPullParser parser)
            throws XmlPullParserException, IOException {
        final Page page = new Page(manifest);
        page.parseManifestXml(parser);
        return page;
    }

    private Page(@NonNull final Manifest manifest) {
        super(manifest);
    }

    @Nullable
    public String getLocalFileName() {
        return mLocalFileName;
    }

    @WorkerThread
    private void parseManifestXml(@NonNull final XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, XMLNS_MANIFEST, XML_PAGE);

        mLocalFileName = parser.getAttributeValue(null, XML_MANIFEST_SRC);

        // discard any nested nodes
        XmlPullParserUtils.skipTag(parser);
    }
}
