package org.keynote.godtools.android.snuffy.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import com.google.common.collect.ImmutableSet;

import org.ccci.gto.android.common.util.XmlPullParserUtils;
import org.keynote.godtools.android.snuffy.ParserUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.Set;

public class GtPage {
    static final String XML_PAGE = "page";
    static final String XML_ABOUT = "about";

    private static final String XML_ATTR_FILENAME = "filename";
    private static final String XML_ATTR_THUMBNAIL = "thumb";
    private static final String XML_ATTR_LISTENERS = "listeners";

    private boolean mLoaded = false;

    private String mFileName;
    private String mThumb;
    private String mDescription;
    @NonNull
    private Set<String> mListeners = ImmutableSet.of();

    @Nullable
    private GtFollowupModal mFollowupModal;

    private GtPage() {}

    public boolean isLoaded() {
        return mLoaded;
    }

    public String getFileName() {
        return mFileName;
    }

    public String getThumb() {
        return mThumb;
    }

    public String getDescription() {
        return mDescription;
    }

    @NonNull
    public Set<String> getListeners() {
        return mListeners;
    }

    @Nullable
    public GtFollowupModal getFollowupModal() {
        return mFollowupModal;
    }

    @WorkerThread
    static GtPage fromManifestXml(final XmlPullParser parser)
            throws IOException, XmlPullParserException {
        return new GtPage().parseManifestXml(parser);
    }

    private GtPage parseManifestXml(final XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, null);
        XmlPullParserUtils.requireAnyName(parser, XML_PAGE, XML_ABOUT);

        // parse the attributes for this page
        mFileName = parser.getAttributeValue(null, XML_ATTR_FILENAME);
        if (mFileName == null) {
            throw new XmlPullParserException("Package XML does not have a filename defined for a page", parser, null);
        }
        mThumb = parser.getAttributeValue(null, XML_ATTR_THUMBNAIL);
        mListeners = ParserUtils.parseEvents(parser.getAttributeValue(null, XML_ATTR_LISTENERS));
        mDescription = XmlPullParserUtils.safeNextText(parser);

        return this;
    }

    @WorkerThread
    public void parsePageXml(@NonNull final XmlPullParser parser) throws IOException, XmlPullParserException {
        try {
            if (mLoaded) {
                throw new IllegalStateException("Page already loaded!");
            }

            parser.require(XmlPullParser.START_TAG, null, XML_PAGE);

            // loop until we reach the matching end tag for this element
            while (parser.next() != XmlPullParser.END_TAG) {
                // skip anything that isn't a start tag for an element
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }

                // process recognized elements
                switch (parser.getName()) {
                    case GtFollowupModal.XML_FOLLOWUP_MODAL:
                        if (mFollowupModal != null) {
                            throw new XmlPullParserException("Package XML has more than 1 Followup Modal defined",
                                                             parser, null);
                        }
                        mFollowupModal = GtFollowupModal.fromXml(parser);
                        break;
                    default:
                        // skip unrecognized nodes
                        XmlPullParserUtils.skipTag(parser);
                }
            }
        } finally {
            mLoaded = true;
        }
    }
}
