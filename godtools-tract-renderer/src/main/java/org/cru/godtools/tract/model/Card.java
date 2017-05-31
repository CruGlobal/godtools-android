package org.cru.godtools.tract.model;

import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.ccci.gto.android.common.util.XmlPullParserUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.cru.godtools.tract.Constants.XMLNS_TRACT;
import static org.cru.godtools.tract.model.Utils.parseColor;

public final class Card extends Base {
    static final String XML_CARD = "card";
    private static final String XML_LABEL = "label";

    @Nullable
    @ColorInt
    private Integer mBackgroundColor = null;

    @Nullable
    private Text mLabel;

    @NonNull
    private final List<Content> mContent = new ArrayList<>();

    private Card(@NonNull final Base parent) {
        super(parent);
    }

    @ColorInt
    private int getBackgroundColor() {
        // XXX: this may need to inherit differently
        return mBackgroundColor != null ? mBackgroundColor : Manifest.getBackgroundColor(getManifest());
    }

    @ColorInt
    static int getBackgroundColor(@Nullable final Card card) {
        // XXX: this may need to inherit differently
        return card != null ? card.getBackgroundColor() : Manifest.getBackgroundColor(null);
    }

    @NonNull
    static Card fromXml(@NonNull final Base parent, @NonNull final XmlPullParser parser)
            throws IOException, XmlPullParserException {
        return new Card(parent).parse(parser);
    }

    @NonNull
    private Card parse(@NonNull final XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, XMLNS_TRACT, XML_CARD);

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
                        case XML_LABEL:
                            mLabel = Text.fromNestedXml(this, parser, XMLNS_TRACT, XML_LABEL);
                            continue;
                    }
                    break;
            }

            // try parsing this child element as a content node
            final Content content = Content.fromXml(this, parser);
            if (content != null) {
                mContent.add(content);
                continue;
            }

            // skip unrecognized nodes
            XmlPullParserUtils.skipTag(parser);
        }

        return this;
    }
}
