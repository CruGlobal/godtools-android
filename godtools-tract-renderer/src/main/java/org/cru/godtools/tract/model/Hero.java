package org.cru.godtools.tract.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.ccci.gto.android.common.util.XmlPullParserUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.cru.godtools.tract.Constants.XMLNS_TRACT;

public final class Hero extends Base {
    static final String XML_HERO = "hero";
    private static final String XML_HEADING = "heading";

    private static final double DEFAULT_HEADING_TEXT_SCALE = 3.0;

    @Nullable
    private Text mHeading;

    @NonNull
    private List<Content> mContent = new ArrayList<>();

    private Hero(@NonNull final Base parent) {
        super(parent);
    }

    @NonNull
    static Hero fromXml(@NonNull final Base parent, @NonNull final XmlPullParser parser)
            throws IOException, XmlPullParserException {
        return new Hero(parent).parse(parser);
    }

    @NonNull
    private Hero parse(@NonNull final XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, XMLNS_TRACT, XML_HERO);

        // process any child elements
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            // process recognized nodes
            switch (parser.getNamespace()) {
                case XMLNS_TRACT:
                    switch (parser.getName()) {
                        case XML_HEADING:
                            mHeading = Text.fromNestedXml(this, parser, XMLNS_TRACT, XML_HEADING);
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
