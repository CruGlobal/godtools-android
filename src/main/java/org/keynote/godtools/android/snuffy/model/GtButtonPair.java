package org.keynote.godtools.android.snuffy.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;

import org.ccci.gto.android.common.util.XmlPullParserUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class GtButtonPair extends GtModel {
    static final String XML_BUTTON_PAIR = "button-pair";

    private GtButton mPositiveButton;
    private GtButton mNegativeButton;

    private GtButtonPair(@NonNull final GtModel parent) {
        super(parent);
    }

    public GtButton getPositiveButton() {
        return mPositiveButton;
    }

    public GtButton getNegativeButton() {
        return mNegativeButton;
    }

    @Nullable
    @Override
    public View render(@NonNull final ViewGroup root, final double scale, final boolean attachToRoot) {
        // TODO
        return null;
    }

    @NonNull
    public static GtButtonPair fromXml(@NonNull final GtModel parent, @NonNull final XmlPullParser parser)
            throws IOException, XmlPullParserException {
        final GtButtonPair buttonPair = new GtButtonPair(parent);
        buttonPair.parse(parser);
        return buttonPair;
    }

    private void parse(@NonNull final XmlPullParser parser) throws IOException, XmlPullParserException {
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
                    mPositiveButton = GtButton.fromXml(this, parser);
                    break;
                case GtButton.XML_NEGATIVE_BUTTON:
                    if (mNegativeButton != null) {
                        throw new XmlPullParserException(
                                "XML has more than 1 " + GtButton.XML_NEGATIVE_BUTTON + " defined", parser, null);
                    }
                    mNegativeButton = GtButton.fromXml(this, parser);
                    break;
                default:
                    // skip unrecognized nodes
                    XmlPullParserUtils.skipTag(parser);
            }
        }
    }
}
