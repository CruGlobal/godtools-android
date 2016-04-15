package org.keynote.godtools.android.snuffy.model;

import android.support.annotation.NonNull;

import com.google.common.collect.ImmutableList;

import org.ccci.gto.android.common.util.XmlPullParserUtils;
import org.keynote.godtools.android.model.Followup;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GtFollowupModal {
    static final String XML_FOLLOWUP_MODAL = "followup-modal";
    private static final String XML_FALLBACK = "fallback";
    private static final String XML_TITLE = "followup-title";
    private static final String XML_BODY = "followup-body";

    private static final String XML_ATTR_FOLLOWUP_ID = "followup-id";

    @NonNull
    private final GtPage mPage;

    private long mFollowupId = Followup.INVALID_ID;
    private String mTitle;
    private String mBody;
    private final List<GtInputField> mInputFields = new ArrayList<>();
    private GtButtonPair mButtonPair;

    private GtFollowupModal(@NonNull final GtPage page) {
        mPage = page;
    }

    @NonNull
    public GtPage getPage() {
        return mPage;
    }

    public long getFollowupId() {
        return mFollowupId;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getBody() {
        return mBody;
    }

    public List<GtInputField> getInputFields() {
        return ImmutableList.copyOf(mInputFields);
    }

    public GtButtonPair getButtonPair() {
        return mButtonPair;
    }

    @NonNull
    static GtFollowupModal fromXml(@NonNull final GtPage page, @NonNull final XmlPullParser parser)
            throws IOException, XmlPullParserException {
        final GtFollowupModal followup = new GtFollowupModal(page);
        followup.parse(parser);
        return followup;
    }

    private void parse(@NonNull final XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, XML_FOLLOWUP_MODAL);

        try {
            mFollowupId = Long.parseLong(parser.getAttributeValue(null, XML_ATTR_FOLLOWUP_ID));
        } catch (final Exception suppressed) {
            mFollowupId = Followup.INVALID_ID;
        }

        // loop until we reach the matching end tag for this element
        while (parser.next() != XmlPullParser.END_TAG) {
            // skip anything that isn't a start tag for an element
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            // process recognized elements
            switch (parser.getName()) {
                case XML_FALLBACK:
                    parseFallback(parser);
                    break;
                default:
                    // skip unrecognized nodes
                    XmlPullParserUtils.skipTag(parser);
            }
        }
    }

    private void parseFallback(@NonNull final XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, XML_FALLBACK);

        // loop until we reach the matching end tag for this element
        while (parser.next() != XmlPullParser.END_TAG) {
            // skip anything that isn't a start tag for an element
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            // process recognized elements
            switch (parser.getName()) {
                case XML_TITLE:
                    mTitle = XmlPullParserUtils.safeNextText(parser);
                    break;
                case XML_BODY:
                    mBody = XmlPullParserUtils.safeNextText(parser);
                    break;
                case GtInputField.XML_INPUT_FIELD:
                    mInputFields.add(GtInputField.fromXml(parser));
                    break;
                case GtButtonPair.XML_BUTTON_PAIR:
                    mButtonPair = GtButtonPair.fromXml(mPage, parser);
                    break;
                default:
                    // skip unrecognized nodes
                    XmlPullParserUtils.skipTag(parser);
            }
        }
    }
}
