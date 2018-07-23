package org.cru.godtools.tract.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.annotation.WorkerThread;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.common.collect.ImmutableSet;

import org.ccci.gto.android.common.util.XmlPullParserUtils;
import org.cru.godtools.base.model.Event;
import org.cru.godtools.tract.R;
import org.cru.godtools.tract.R2;
import org.cru.godtools.tract.model.AnalyticsEvent.Trigger;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import butterknife.BindView;
import butterknife.OnClick;

import static org.cru.godtools.tract.Constants.XMLNS_ANALYTICS;
import static org.cru.godtools.tract.Constants.XMLNS_CONTENT;
import static org.cru.godtools.tract.model.Text.XML_TEXT;

public final class Link extends Content {
    static final String XML_LINK = "link";

    @NonNull
    Collection<AnalyticsEvent> mAnalyticsEvents = ImmutableSet.of();

    @NonNull
    Set<Event.Id> mEvents = ImmutableSet.of();

    @Nullable
    Text mText;

    private Link(@NonNull final Base parent) {
        super(parent);
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
    void parseAttrs(@NonNull final XmlPullParser parser) {
        super.parseAttrs(parser);
        mEvents = parseEvents(parser, XML_EVENTS);
    }

    @NonNull
    @Override
    LinkViewHolder createViewHolder(@NonNull final ViewGroup parent, @Nullable final BaseViewHolder parentViewHolder) {
        return new LinkViewHolder(parent, parentViewHolder);
    }

    @UiThread
    static final class LinkViewHolder extends BaseViewHolder<Link> {
        @BindView(R2.id.content_link)
        TextView mLink;

        LinkViewHolder(@NonNull final ViewGroup parent, @Nullable final BaseViewHolder parentViewHolder) {
            super(Link.class, parent, R.layout.tract_content_link, parentViewHolder);
        }

        /* BEGIN lifecycle */

        @Override
        void onBind() {
            super.onBind();
            bindText();
        }

        /* END lifecycle */

        private void bindText() {
            final Text text = mModel != null ? mModel.mText : null;
            Text.bind(text, mLink, null, Styles.getPrimaryColor(Base.getStylesParent(mModel)));
        }

        @OnClick(R2.id.content_link)
        void click() {
            if (mModel != null) {
                sendEvents(mModel.mEvents);
                triggerAnalyticsEvents(mModel.mAnalyticsEvents, Trigger.SELECTED, Trigger.DEFAULT);
            }
        }
    }
}
