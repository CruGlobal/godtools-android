package org.cru.godtools.xml.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import org.ccci.gto.android.common.util.XmlPullParserUtils;
import org.cru.godtools.base.model.Event;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static org.cru.godtools.xml.Constants.XMLNS_ANALYTICS;
import static org.cru.godtools.xml.Constants.XMLNS_CONTENT;

public final class Tab extends Base implements Parent {
    static final String XML_TAB = "tab";
    private static final String XML_LABEL = "label";

    private final int mPosition;

    @NonNull
    private Collection<AnalyticsEvent> mAnalyticsEvents = ImmutableSet.of();

    @NonNull
    private Set<Event.Id> mListeners = ImmutableSet.of();

    @Nullable
    private Text mLabel;
    @NonNull
    private List<Content> mContent = ImmutableList.of();

    private Tab(@NonNull final Tabs parent, final int position) {
        super(parent);
        mPosition = position;
    }

    @NonNull
    public String getId() {
        return Integer.toString(mPosition);
    }

    public int getPosition() {
        return mPosition;
    }

    @NonNull
    public Collection<AnalyticsEvent> getAnalyticsEvents() {
        return mAnalyticsEvents;
    }

    @NonNull
    public Set<Event.Id> getListeners() {
        return mListeners;
    }

    @Nullable
    public Text getLabel() {
        return mLabel;
    }

    @NonNull
    @Override
    public List<Content> getContent() {
        return mContent;
    }

    @NonNull
    static Tab fromXml(@NonNull final Tabs parent, @NonNull final XmlPullParser parser, final int position)
            throws IOException, XmlPullParserException {
        return new Tab(parent, position).parse(parser);
    }

    @NonNull
    private Tab parse(@NonNull final XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, XMLNS_CONTENT, XML_TAB);
        mListeners = parseEvents(parser, XML_LISTENERS);

        // process any child elements
        final ImmutableList.Builder<Content> contentList = ImmutableList.builder();
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
                case XMLNS_CONTENT:
                    switch (parser.getName()) {
                        case XML_LABEL:
                            mLabel = Text.fromNestedXml(this, parser, XMLNS_CONTENT, XML_LABEL);
                            continue;
                    }
                    break;
            }

            // try parsing this child element as a content node
            final Content content = Content.Companion.fromXml(this, parser);
            if (content != null) {
                if (!content.isIgnored()) {
                    contentList.add(content);
                }
                continue;
            }

            // skip unrecognized nodes
            XmlPullParserUtils.skipTag(parser);
        }
        mContent = contentList.build();

        return this;
    }
}
