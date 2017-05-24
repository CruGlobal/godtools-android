package org.cru.godtools.tract.model;

import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import org.ccci.gto.android.common.util.XmlPullParserUtils;
import org.cru.godtools.tract.R;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

import static org.cru.godtools.tract.Constants.XMLNS_TRACT;
import static org.cru.godtools.tract.model.Utils.parseColor;

public final class Header extends Base {
    static final String XML_HEADER = "header";
    private static final String XML_NUMBER = "number";
    private static final String XML_TITLE = "title";

    private static final double DEFAULT_NUMBER_TEXT_SCALE = 3.0;

    @Nullable
    @ColorInt
    private Integer mBackgroundColor = null;
    @Nullable
    private Text mNumber;
    @Nullable
    private Text mTitle;

    private Header(@NonNull final Base parent) {
        super(parent);
    }

    @ColorInt
    public int getBackgroundColor() {
        return mBackgroundColor != null ? mBackgroundColor : getPage().getPrimaryColor();
    }

    static Header fromXml(@NonNull final Base parent, @NonNull final XmlPullParser parser)
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

    public void bindNumber(@Nullable final TextView view) {
        if (view != null) {
            view.setVisibility(mNumber != null ? View.VISIBLE : View.GONE);
            final float textSize = view.getResources().getDimension(R.dimen.text_size_header);
            Text.bind(mNumber, view, getPage().getPrimaryTextColor(), textSize, DEFAULT_NUMBER_TEXT_SCALE);
        }
    }

    public void bindTitle(@Nullable final TextView view) {
        if (view != null) {
            view.setVisibility(mTitle != null ? View.VISIBLE : View.GONE);
            final float textSize = view.getResources().getDimension(R.dimen.text_size_header);
            Text.bind(mTitle, view, getPage().getPrimaryTextColor(), textSize);
        }
    }
}
