package org.keynote.godtools.android.snuffy.model;

import android.graphics.Typeface;
import android.support.annotation.ColorInt;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.widget.TextView;

import com.google.common.base.Strings;

import org.keynote.godtools.android.utils.TypefaceUtils;
import org.w3c.dom.Element;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static org.ccci.gto.android.common.util.NumberUtils.toInteger;
import static org.keynote.godtools.android.snuffy.ParserUtils.safeParseColor;

public abstract class GtTextModel extends GtModel {
    private static final String XML_ATTR_TEXT_SIZE = "size";
    private static final String XML_ATTR_TEXT_COLOR = "color";
    private static final String XML_ATTR_TEXT_STYLE = "modifier";
    private static final String XML_ATTR_TEXT_ALIGN = "textalign";

    private static final String XML_ATTR_TEXT_STYLE_NORMAL = "normal";
    private static final String XML_ATTR_TEXT_STYLE_BOLD = "bold";
    private static final String XML_ATTR_TEXT_STYLE_ITALIC = "italics";
    private static final String XML_ATTR_TEXT_STYLE_BOLD_ITALIC = "bold-italics";

    private static final String XML_ATTR_TEXT_ALIGN_LEFT = "left";
    private static final String XML_ATTR_TEXT_ALIGN_RIGHT = "right";
    private static final String XML_ATTR_TEXT_ALIGN_CENTER = "center";

    @IntDef({Typeface.NORMAL, Typeface.BOLD, Typeface.ITALIC, Typeface.BOLD_ITALIC})
    @Retention(RetentionPolicy.SOURCE)
    private @interface Style {}

    @Nullable
    private Integer mTextSize = null;
    @ColorInt
    @Nullable
    private Integer mTextColor;
    @Style
    private int mTextStyle = Typeface.NORMAL;
    private int mTextAlign = Gravity.NO_GRAVITY;

    GtTextModel(@NonNull final GtModel model) {
        super(model);
    }

    @Nullable
    public Integer getTextSize() {
        return mTextSize;
    }

    @ColorInt
    @Nullable
    public Integer getTextColor() {
        return mTextColor;
    }

    @Style
    public int getTextStyle() {
        return mTextStyle;
    }

    public int getTextAlign() {
        return mTextAlign;
    }

    void applyTextStyles(@NonNull final TextView view) {
        TypefaceUtils.setTypeface(view, getManifest().getLanguage(), getTextStyle());
        final Integer color = getTextColor();
        if (color != null) {
            view.setTextColor(getTextColor());
        }
        final int align = getTextAlign();
        if (align != Gravity.NO_GRAVITY) {
            view.setGravity(align);
        }
    }

    void parseTextAttrs(@NonNull final XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, null);

        mTextSize = toInteger(parser.getAttributeValue(null, XML_ATTR_TEXT_SIZE), mTextSize);
        mTextColor = safeParseColor(parser.getAttributeValue(null, XML_ATTR_TEXT_COLOR), null);
        mTextStyle = parseTextStyle(parser.getAttributeValue(null, XML_ATTR_TEXT_STYLE));
        mTextAlign = parseTextAlign(parser.getAttributeValue(null, XML_ATTR_TEXT_ALIGN));
    }

    @Deprecated
    void parseTextAttrs(@NonNull final Element node) {
        mTextSize = toInteger(node.getAttribute(XML_ATTR_TEXT_SIZE), mTextSize);
        mTextColor = safeParseColor(node.getAttribute(XML_ATTR_TEXT_COLOR), null);
        mTextStyle = parseTextStyle(node.getAttribute(XML_ATTR_TEXT_STYLE));
        mTextAlign = parseTextAlign(node.getAttribute(XML_ATTR_TEXT_ALIGN));
    }

    @Style
    private static int parseTextStyle(@Nullable final String rawStyle) {
        switch (Strings.nullToEmpty(rawStyle)) {
            case XML_ATTR_TEXT_STYLE_BOLD:
                return Typeface.BOLD;
            case XML_ATTR_TEXT_STYLE_ITALIC:
                return Typeface.ITALIC;
            case XML_ATTR_TEXT_STYLE_BOLD_ITALIC:
                return Typeface.BOLD_ITALIC;
            case XML_ATTR_TEXT_STYLE_NORMAL:
            default:
                return Typeface.NORMAL;
        }
    }

    private static int parseTextAlign(@Nullable final String rawAlign) {
        switch (Strings.nullToEmpty(rawAlign)) {
            case XML_ATTR_TEXT_ALIGN_LEFT:
                return Gravity.START;
            case XML_ATTR_TEXT_ALIGN_RIGHT:
                return Gravity.END;
            case XML_ATTR_TEXT_ALIGN_CENTER:
                return Gravity.CENTER_HORIZONTAL;
            default:
                return Gravity.NO_GRAVITY;
        }
    }
}
