package org.cru.godtools.xml.model;

import com.google.common.collect.ImmutableSet;

import org.ccci.gto.android.common.util.XmlPullParserUtils;
import org.cru.godtools.base.model.Event;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.annotation.WorkerThread;

import static org.cru.godtools.xml.Constants.XMLNS_CONTENT;

public final class Image extends Content {
    static final String XML_IMAGE = "image";
    private static final String XML_RESOURCE = "resource";

    @Nullable
    @VisibleForTesting
    String mResourceName = null;
    @NonNull
    private Set<Event.Id> mEvents = ImmutableSet.of();

    private Image(@NonNull final Base parent) {
        super(parent);
    }

    @Nullable
    public static Resource getResource(@Nullable final Image image) {
        return image != null ? image.getResource(image.mResourceName) : null;
    }

    @NonNull
    public Set<Event.Id> getEvents() {
        return mEvents;
    }

    @WorkerThread
    static Image fromXml(@NonNull final Base parent, @NonNull final XmlPullParser parser)
            throws IOException, XmlPullParserException {
        return new Image(parent).parse(parser);
    }

    @WorkerThread
    private Image parse(@NonNull final XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, XMLNS_CONTENT, XML_IMAGE);
        parseAttrs(parser);
        XmlPullParserUtils.skipTag(parser);
        return this;
    }

    @Override
    void parseAttrs(@NonNull final XmlPullParser parser) {
        super.parseAttrs(parser);
        mResourceName = parser.getAttributeValue(null, XML_RESOURCE);
        mEvents = parseEvents(parser, XML_EVENTS);
    }
}
