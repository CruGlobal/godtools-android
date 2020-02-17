package org.cru.godtools.xml.model;

import com.google.common.collect.ImmutableSet;

import org.ccci.gto.android.common.util.XmlPullParserUtils;
import org.cru.godtools.base.model.Event;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import static org.cru.godtools.xml.Constants.XMLNS_ANALYTICS;
import static org.cru.godtools.xml.Constants.XMLNS_CONTENT;
import static org.cru.godtools.xml.model.Text.XML_TEXT;

public final class Link extends Content {
    static final String XML_LINK = "link";

    @NonNull
    private Collection<AnalyticsEvent> mAnalyticsEvents = ImmutableSet.of();

    @NonNull
    private Set<Event.Id> mEvents = ImmutableSet.of();

    @Nullable
    private Text mText;

    private Link(@NonNull final Base parent) {
        super(parent);
    }

    @NonNull
    public Collection<AnalyticsEvent> getAnalyticsEvents() {
        return mAnalyticsEvents;
    }

    @NonNull
    public Set<Event.Id> getEvents() {
        return mEvents;
    }

    @Nullable
    public Text getText() {
        return mText;
    }

    @WorkerThread
    static Link fromXml(@NonNull final Base parent, @NonNull final XmlPullParser parser)
            throws IOException, XmlPullParserException {
        return new Link(parent).parse(parser);
    }

    @WorkerThread
    private Link parse(@NonNull final XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, XMLNS_CONTENT, XML_LINK);
        parseAttrs(parser);

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
                case XMLNS_CONTENT:
                    switch (parser.getName()) {
                        case XML_TEXT:
                            mText = Text.fromXml(this, parser);
                            continue;
                    }
                    break;
            }

            // skip unrecognized nodes
            XmlPullParserUtils.skipTag(parser);
        }

        return this;
    }

    @Override
    protected void parseAttrs(@NonNull final XmlPullParser parser) {
        super.parseAttrs(parser);
        mEvents = parseEvents(parser, XML_EVENTS);
    }
}
