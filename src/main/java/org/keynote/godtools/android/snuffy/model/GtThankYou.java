package org.keynote.godtools.android.snuffy.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.keynote.godtools.android.snuffy.ParserUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class GtThankYou extends GtPage {
    public static final String XML_THANK_YOU = "thank-you";

    private GtThankYou(@NonNull final GtFollowupModal modal, @NonNull final String uniqueId) {
        super(modal, "thankyou-" + uniqueId);
    }

    @Nullable
    public GtPage getParentPage() {
        return getParentModel().getPage();
    }

    @Nullable
    @Override
    public Integer getBackgroundColor() {
        Integer color = super.getBackgroundColor();
        if (color == null) {
            final GtPage parent = getParentPage();
            if (parent != null) {
                color = parent.getBackgroundColor();
            }
        }
        return color;
    }

    @Nullable
    @Override
    public String getBackground() {
        String background = super.getBackground();
        if (background == null) {
            final GtPage parent = getParentPage();
            if (parent != null) {
                background = parent.getBackground();
            }
        }
        return background;
    }

    @Nullable
    @Override
    public String getWatermark() {
        String watermark = super.getWatermark();
        if (watermark == null) {
            final GtPage parent = getParentPage();
            if (parent != null) {
                watermark = parent.getWatermark();
            }
        }
        return watermark;
    }

    @NonNull
    static GtThankYou fromXml(@NonNull final GtFollowupModal modal, @NonNull final String uniqueId,
                              @NonNull final XmlPullParser parser)
            throws IOException, XmlPullParserException {
        final GtThankYou thankYou = new GtThankYou(modal, uniqueId);
        thankYou.parse(parser);
        return thankYou;
    }

    private void parse(@NonNull final XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, XML_THANK_YOU);

        mListeners = ParserUtils.parseEvents(parser.getAttributeValue(null, XML_ATTR_LISTENERS),
                                             getManifest().getPackageCode());

        // parse the content
        parseContentXml(parser);
    }
}
