package org.cru.godtools.xml.model;

import com.google.common.collect.ImmutableSet;

import org.ccci.gto.android.common.util.XmlPullParserUtils;
import org.cru.godtools.xml.R;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import androidx.annotation.DimenRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static org.cru.godtools.xml.Constants.XMLNS_ANALYTICS;
import static org.cru.godtools.xml.Constants.XMLNS_TRACT;

public final class Hero extends Base implements Parent, Styles {
    static final String XML_HERO = "hero";
    private static final String XML_HEADING = "heading";

    @NonNull
    private Collection<AnalyticsEvent> mAnalyticsEvents = ImmutableSet.of();

    @Nullable
    private Text mHeading;

    @NonNull
    private final List<Content> mContent = new ArrayList<>();

    private Hero(@NonNull final Base parent) {
        super(parent);
    }

    @DimenRes
    @Override
    public int getTextSize() {
        return R.dimen.text_size_hero;
    }

    @NonNull
    public Collection<AnalyticsEvent> getAnalyticsEvents() {
        return mAnalyticsEvents;
    }

    @Nullable
    public Text getHeading() {
        return mHeading;
    }

    @NonNull
    @Override
    public List<Content> getContent() {
        return mContent;
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
                case XMLNS_ANALYTICS:
                    switch (parser.getName()) {
                        case AnalyticsEvent.XML_EVENTS:
                            mAnalyticsEvents = AnalyticsEvent.fromEventsXml(parser);
                            continue;
                    }
                    break;
                case XMLNS_TRACT:
                    switch (parser.getName()) {
                        case XML_HEADING:
                            mHeading = Text.fromNestedXml(this, parser, XMLNS_TRACT, XML_HEADING);
                            continue;
                    }
                    break;
            }

            // try parsing this child element as a content node
            final Content content = Content.Companion.fromXml(this, parser);
            if (content != null) {
                if (!content.isIgnored()) {
                    mContent.add(content);
                }
                continue;
            }

            // skip unrecognized nodes
            XmlPullParserUtils.skipTag(parser);
        }

        return this;
    }
}
