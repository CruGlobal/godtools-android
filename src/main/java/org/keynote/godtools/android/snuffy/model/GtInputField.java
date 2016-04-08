package org.keynote.godtools.android.snuffy.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.ccci.gto.android.common.util.XmlPullParserUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class GtInputField {
    public enum Type {
        EMAIL, TEXT;

        public static Type DEFAULT = TEXT;

        private static final String XML_ATTR_TYPE_EMAIL = "email";
        private static final String XML_ATTR_TYPE_TEXT = "text";

        static Type fromXmlAttr(@Nullable final String attr) {
            if (attr != null) {
                switch (attr) {
                    case XML_ATTR_TYPE_EMAIL:
                        return EMAIL;
                    case XML_ATTR_TYPE_TEXT:
                        return TEXT;
                }
            }

            return DEFAULT;
        }
    }

    static final String XML_INPUT_FIELD = "input-field";
    private static final String XML_LABEL = "input-label";
    private static final String XML_PLACEHOLDER = "input-placeholder";

    private static final String XML_ATTR_TYPE = "type";
    private static final String XML_ATTR_NAME = "name";

    @NonNull
    private Type mType = Type.DEFAULT;
    private String mName;
    private String mLabel;
    private String mPlaceholder;

    private GtInputField() {}

    @NonNull
    public Type getType() {
        return mType;
    }

    public String getName() {
        return mName;
    }

    public String getLabel() {
        return mLabel;
    }

    public String getPlaceholder() {
        return mPlaceholder;
    }

    @NonNull
    static GtInputField fromXml(@NonNull final XmlPullParser parser) throws IOException, XmlPullParserException {
        return new GtInputField().parse(parser);
    }

    @NonNull
    private GtInputField parse(@NonNull final XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, XML_INPUT_FIELD);

        mType = Type.fromXmlAttr(parser.getAttributeValue(null, XML_ATTR_TYPE));
        mName = parser.getAttributeValue(null, XML_ATTR_NAME);

        // loop until we reach the matching end tag for this element
        while (parser.next() != XmlPullParser.END_TAG) {
            // skip anything that isn't a start tag for an element
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            // process recognized elements
            switch (parser.getName()) {
                case XML_LABEL:
                    mLabel = XmlPullParserUtils.safeNextText(parser);
                    break;
                case XML_PLACEHOLDER:
                    mPlaceholder = XmlPullParserUtils.safeNextText(parser);
                    break;
                default:
                    // skip unrecognized nodes
                    XmlPullParserUtils.skipTag(parser);
            }
        }

        return this;
    }
}
