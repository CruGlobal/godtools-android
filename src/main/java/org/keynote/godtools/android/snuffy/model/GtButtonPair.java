package org.keynote.godtools.android.snuffy.model;

import android.support.annotation.NonNull;

import org.ccci.gto.android.common.util.XmlPullParserUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class GtButtonPair {
    static final String XML_BUTTON_PAIR = "button-pair";

    private GtButton mPositiveButton;
    private GtButton mNegativeButton;

    private GtButtonPair() {}

    public GtButton getPositiveButton() {
        return mPositiveButton;
    }

    public GtButton getNegativeButton() {
        return mNegativeButton;
    }

    @NonNull
    static GtButtonPair fromXml(@NonNull final XmlPullParser parser) throws IOException, XmlPullParserException {
        return new GtButtonPair().parse(parser);
    }

    @NonNull
    private GtButtonPair parse(@NonNull final XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, XML_BUTTON_PAIR);

        // loop until we reach the matching end tag for this element
        while (parser.next() != XmlPullParser.END_TAG) {
            // skip anything that isn't a start tag for an element
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            // process recognized elements
            switch (parser.getName()) {
                case GtButton.XML_POSITIVE_BUTTON:
                    if (mPositiveButton != null) {
                        throw new XmlPullParserException(
                                "XML has more than 1 " + GtButton.XML_POSITIVE_BUTTON + " defined", parser, null);
                    }
                    mPositiveButton = GtButton.fromXml(parser);
                    break;
                case GtButton.XML_NEGATIVE_BUTTON:
                    if (mNegativeButton != null) {
                        throw new XmlPullParserException(
                                "XML has more than 1 " + GtButton.XML_NEGATIVE_BUTTON + " defined", parser, null);
                    }
                    mNegativeButton = GtButton.fromXml(parser);
                    break;
                default:
                    // skip unrecognized nodes
                    XmlPullParserUtils.skipTag(parser);
            }
        }

        return this;
    }
}
