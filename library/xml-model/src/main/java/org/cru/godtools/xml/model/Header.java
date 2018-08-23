package org.cru.godtools.xml.model;

import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.annotation.DimenRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.ccci.gto.android.common.util.XmlPullParserUtils;
import org.cru.godtools.xml.R;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

import static org.cru.godtools.xml.Constants.XMLNS_TRACT;
import static org.cru.godtools.xml.model.Utils.parseColor;

public final class Header extends Base implements Styles {
    static final String XML_HEADER = "header";
    private static final String XML_NUMBER = "number";
    private static final String XML_TITLE = "title";

    @Nullable
    @ColorInt
    private Integer mBackgroundColor = null;
    @Nullable
    private Text mNumber;
    @Nullable
    private Text mTitle;

    private Header(@NonNull final Page parent) {
        super(parent);
    }

    @Nullable
    public Text getNumber() {
        return mNumber;
    }

    @Nullable
    public Text getTitle() {
        return mTitle;
    }

    @ColorInt
    @Override
    public int getTextColor() {
        return getPrimaryTextColor();
    }

    @DimenRes
    @Override
    public int getTextSize() {
        return R.dimen.text_size_header;
    }

    @ColorInt
    public int getBackgroundColor() {
        return mBackgroundColor != null ? mBackgroundColor : getPage().getPrimaryColor();
    }

    public static int getBackgroundColor(@Nullable final Header header) {
        return header != null ? header.getBackgroundColor() : Color.TRANSPARENT;
    }

    static Header fromXml(@NonNull final Page parent, @NonNull final XmlPullParser parser)
            throws IOException, XmlPullParserException {
        return new Header(parent).parse(parser);
    }

    private Header parse(@NonNull final XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, XMLNS_TRACT, XML_HEADER);

        mBackgroundColor = parseColor(parser, XML_BACKGROUND_COLOR, mBackgroundColor);

        // process any child elements
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            // process recognized nodes
            switch (parser.getNamespace()) {
                case XMLNS_TRACT:
                    switch (parser.getName()) {
                        case XML_NUMBER:
                            mNumber = Text.fromNestedXml(this, parser, XMLNS_TRACT, XML_NUMBER);
                            continue;
                        case XML_TITLE:
                            mTitle = Text.fromNestedXml(this, parser, XMLNS_TRACT, XML_TITLE);
                            continue;
                    }
                    break;
            }

            // skip unrecognized nodes
            XmlPullParserUtils.skipTag(parser);
        }

        return this;
    }
}
