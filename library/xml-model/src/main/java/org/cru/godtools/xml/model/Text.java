package org.cru.godtools.xml.model;

import android.view.Gravity;

import org.ccci.gto.android.common.util.NumberUtils;
import org.ccci.gto.android.common.util.XmlPullParserUtils;
import org.jetbrains.annotations.Contract;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

import androidx.annotation.ColorInt;
import androidx.annotation.DimenRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.annotation.WorkerThread;

import static org.cru.godtools.xml.Constants.XMLNS_CONTENT;
import static org.cru.godtools.xml.model.Utils.parseColor;

public final class Text extends Content {
    static final String XML_TEXT = "text";
    private static final String XML_TEXT_ALIGN = "text-align";
    private static final String XML_TEXT_ALIGN_START = "start";
    private static final String XML_TEXT_ALIGN_CENTER = "center";
    private static final String XML_TEXT_ALIGN_END = "end";
    private static final String XML_TEXT_SCALE = "text-scale";

    private static final double DEFAULT_TEXT_SCALE = 1.0;

    public enum Align {
        START(Gravity.START), CENTER(Gravity.CENTER_HORIZONTAL), END(Gravity.END);

        public static final Align DEFAULT = START;

        public final int mGravity;

        Align(final int gravity) {
            mGravity = gravity;
        }

        @Nullable
        @Contract("_, !null -> !null")
        static Align parse(@Nullable final String value, @Nullable final Align defValue) {
            if (value != null) {
                switch (value) {
                    case XML_TEXT_ALIGN_START:
                        return START;
                    case XML_TEXT_ALIGN_CENTER:
                        return CENTER;
                    case XML_TEXT_ALIGN_END:
                        return END;
                }
            }
            return defValue;
        }
    }

    @Nullable
    private Align mTextAlign = null;
    @ColorInt
    @Nullable
    private Integer mTextColor = null;
    @Nullable
    private Double mTextScale = null;

    @Nullable
    @VisibleForTesting
    String mText;

    private Text(@NonNull final Base parent) {
        super(parent);
    }

    @NonNull
    public Align getTextAlign() {
        return mTextAlign != null ? mTextAlign : Styles.getTextAlign(getStylesParent());
    }

    @NonNull
    public static Align getTextAlign(@Nullable final Text text) {
        return text != null ? text.getTextAlign() : Align.DEFAULT;
    }

    @Override
    public int getTextColor() {
        return mTextColor != null ? mTextColor : Styles.getTextColor(getStylesParent());
    }

    @ColorInt
    public int getTextColor(@ColorInt final int defColor) {
        return mTextColor != null ? mTextColor : defColor;
    }

    @ColorInt
    public static int getTextColor(@Nullable final Text text) {
        return text != null ? text.getTextColor() : Styles.getTextColor(null);
    }

    private double getTextScale() {
        return mTextScale != null ? mTextScale : DEFAULT_TEXT_SCALE;
    }

    public static double getTextScale(@Nullable final Text text) {
        return text != null ? text.getTextScale() : DEFAULT_TEXT_SCALE;
    }

    @Nullable
    public static String getText(@Nullable final Text text) {
        return text != null ? text.mText : null;
    }

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

    @WorkerThread
    private Text parse(@NonNull final XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, XMLNS_CONTENT, XML_TEXT);
        parseAttrs(parser);
        mText = XmlPullParserUtils.safeNextText(parser);
        return this;
    }

    @Override
    protected void parseAttrs(@NonNull final XmlPullParser parser) {
        super.parseAttrs(parser);
        mTextAlign = Align.parse(parser.getAttributeValue(null, XML_TEXT_ALIGN), mTextAlign);
        mTextColor = parseColor(parser, XML_TEXT_COLOR, mTextColor);
        mTextScale = NumberUtils.toDouble(parser.getAttributeValue(null, XML_TEXT_SCALE), mTextScale);
    }

    @ColorInt
    public static int defaultTextColor(@Nullable final Text text) {
        return Styles.getTextColor(getStylesParent(text));
    }

    @DimenRes
    public static int textSize(@Nullable final Text text) {
        return Styles.getTextSize(getStylesParent(text));
    }
}
