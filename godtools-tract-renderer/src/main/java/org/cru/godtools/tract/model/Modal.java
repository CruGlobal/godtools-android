package org.cru.godtools.tract.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import org.ccci.gto.android.common.util.XmlPullParserUtils;
import org.cru.godtools.base.model.Event;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import static org.cru.godtools.tract.Constants.XMLNS_TRACT;

public final class Modal extends Base {
    static final String XML_MODAL = "modal";
    private static final String XML_TITLE = "title";
    private static final String XML_LISTENERS = "listeners";

    @NonNull
    private Set<Event.Id> mListeners = ImmutableSet.of();

    @Nullable
    Text mTitle;

    @NonNull
    private List<Content> mContent = ImmutableList.of();

    private Modal(@NonNull final Base parent) {
        super(parent);
    }

    @NonNull
    public Set<Event.Id> getListeners() {
        return mListeners;
    }

    @NonNull
    static Modal fromXml(@NonNull final Base parent, @NonNull final XmlPullParser parser)
            throws IOException, XmlPullParserException {
        return new Modal(parent).parse(parser);
    }

    @NonNull
    private Modal parse(@NonNull final XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, XMLNS_TRACT, XML_MODAL);

        mListeners = parseEvents(parser, XML_LISTENERS);

        // process any child elements
        final ImmutableList.Builder<Content> contentList = ImmutableList.builder();
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            // process recognized nodes
            switch (parser.getNamespace()) {
                case XMLNS_TRACT:
                    switch (parser.getName()) {
                        case XML_TITLE:
                            mTitle = Text.fromNestedXml(this, parser, XMLNS_TRACT, XML_TITLE);
                            continue;
                    }
                    break;
            }

            // try parsing this child element as a content node
            final Content content = Content.fromXml(this, parser);
            if (content != null) {
                contentList.add(content);
                continue;
            }

            // skip unrecognized nodes
            XmlPullParserUtils.skipTag(parser);
        }
        mContent = contentList.build();

        return this;
    }
}
