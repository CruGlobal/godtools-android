package org.keynote.godtools.android.snuffy.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.common.base.Predicates;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;

import org.ccci.gto.android.common.util.XmlPullParserUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.Set;

public class GtButton {
    public enum Mode {
        LINK, DEFAULT, PANEL;

        private static final String MODE_LINK = "link";

        @NonNull
        static Mode fromXmlAttr(@Nullable final String mode, @NonNull final Mode defValue) {
            if (mode != null) {
                switch (mode) {
                    case MODE_LINK:
                        return LINK;
                }
            }

            return defValue;
        }
    }

    static final String XML_BUTTON = "button";
    static final String XML_POSITIVE_BUTTON = "positive-button";
    static final String XML_NEGATIVE_BUTTON = "negative-button";
    static final String XML_LINK_BUTTON = "link-button";

    private static final String XML_ATTR_MODE = "mode";
    private static final String XML_ATTR_TAP_EVENTS = "tap-events";

    @NonNull
    private Mode mMode = Mode.DEFAULT;
    @NonNull
    private Set<String> mTapEvents = ImmutableSet.of();

    private GtButton() {}

    @NonNull
    public Mode getMode() {
        return mMode;
    }

    @NonNull
    static GtButton fromXml(@NonNull final XmlPullParser parser) throws IOException, XmlPullParserException {
        return new GtButton().parse(parser);
    }

    private GtButton parse(@NonNull final XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, null);
        XmlPullParserUtils
                .requireAnyName(parser, XML_BUTTON, XML_POSITIVE_BUTTON, XML_NEGATIVE_BUTTON, XML_LINK_BUTTON);

        switch (parser.getName()) {
            case XML_BUTTON:
                mMode = Mode.PANEL;
                break;
            case XML_LINK_BUTTON:
                mMode = Mode.LINK;
                break;
            default:
                mMode = Mode.DEFAULT;
        }
        mMode = Mode.fromXmlAttr(parser.getAttributeValue(null, XML_ATTR_MODE), mMode);
        mTapEvents = FluentIterable
                .of(TextUtils.split(Strings.nullToEmpty(parser.getAttributeValue(null, XML_ATTR_TAP_EVENTS)), ","))
                .filter(Predicates.containsPattern(".")).toSet();

        // loop until we reach the matching end tag for this element
        while (parser.next() != XmlPullParser.END_TAG) {
            // skip anything that isn't a start tag for an element
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            // process recognized elements
            switch (parser.getName()) {
//                case XML_TITLE:
//                    mTitle = XmlPullParserUtils.safeNextText(parser);
//                    break;
                default:
                    // skip unrecognized nodes
                    XmlPullParserUtils.skipTag(parser);
            }
        }

        return this;
    }
}
