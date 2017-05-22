package org.cru.godtools.tract.model;

import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.widget.TextView;

import org.ccci.gto.android.common.util.NumberUtils;
import org.ccci.gto.android.common.util.XmlPullParserUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

import static android.util.TypedValue.COMPLEX_UNIT_SP;
import static org.cru.godtools.tract.Constants.XMLNS_CONTENT;
import static org.cru.godtools.tract.model.Utils.parseColor;

public final class Text extends Content {
    private static final String XML_TEXT = "text";
    private static final String XML_TEXT_SCALE = "text-scale";

    public static final float BASE_TEXT_SIZE = 16;
    public static final double DEFAULT_TEXT_SCALE = 1.0;

    @ColorInt
    @Nullable
    private Integer mTextColor = null;
    @Nullable
    private Double mTextScale = null;

    @Nullable
    private String mText;

    static Text fromXml(@NonNull final Base parent, @NonNull final XmlPullParser parser)
            throws IOException, XmlPullParserException {
        return new Text(parent).parse(parser);
    }

    @Nullable
    static Text fromNestedXml(@NonNull final Base parent, @NonNull final XmlPullParser parser,
                              @Nullable final String parentNamespace, @NonNull final String parentName)
            throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, parentNamespace, parentName);

        // process any child elements
        Text text = null;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            // process recognized nodes
            switch (parser.getNamespace()) {
                case XMLNS_CONTENT:
                    switch (parser.getName()) {
                        case XML_TEXT:
                            text = fromXml(parent, parser);
                            continue;
                    }
                    break;
            }

            // skip unrecognized nodes
            XmlPullParserUtils.skipTag(parser);
        }

        return text;
    }

    Text(@NonNull final Base parent) {
        super(parent);
    }

    @ColorInt
    public int getTextColor(@ColorInt final int defColor) {
        return mTextColor != null ? mTextColor : defColor;
    }

    private double getTextScale(final double defScale) {
        return mTextScale != null ? mTextScale : defScale;
    }

    @Nullable
    public String getText() {
        return mText;
    }

    @WorkerThread
    private Text parse(@NonNull final XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, XMLNS_CONTENT, XML_TEXT);

        mTextColor = parseColor(parser, XML_TEXT_COLOR, mTextColor);
        mTextScale = NumberUtils.toDouble(parser.getAttributeValue(null, XML_TEXT_SCALE), mTextScale);

        mText = XmlPullParserUtils.safeNextText(parser);
        return this;
    }
}
