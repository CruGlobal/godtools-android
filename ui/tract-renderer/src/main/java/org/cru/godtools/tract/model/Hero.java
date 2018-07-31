package org.cru.godtools.tract.model;

import android.support.annotation.DimenRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.google.common.collect.ImmutableSet;

import org.ccci.gto.android.common.util.XmlPullParserUtils;
import org.cru.godtools.tract.R;
import org.cru.godtools.tract.R2;
import org.cru.godtools.tract.model.AnalyticsEvent.Trigger;
import org.cru.godtools.tract.model.Page.PageViewHolder;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import butterknife.BindView;

import static org.cru.godtools.tract.Constants.XMLNS_ANALYTICS;
import static org.cru.godtools.tract.Constants.XMLNS_TRACT;

public final class Hero extends Base implements Parent, Styles {
    static final String XML_HERO = "hero";
    private static final String XML_HEADING = "heading";

    @Nullable
    Text mHeading;

    @NonNull
    Collection<AnalyticsEvent> mAnalyticsEvents = ImmutableSet.of();

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
            final Content content = Content.fromXml(this, parser);
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

    public static class HeroViewHolder extends Parent.ParentViewHolder<Hero> {
        @BindView(R2.id.hero_heading)
        TextView mHeading;

        @Nullable
        private List<Runnable> mPendingAnalyticsEvents;

        HeroViewHolder(@NonNull final View root, @Nullable final BaseViewHolder parentViewHolder) {
            super(Hero.class, root, parentViewHolder);
        }

        @NonNull
        public static HeroViewHolder forView(@NonNull final View root,
                                             @Nullable final PageViewHolder parentViewHolder) {
            final HeroViewHolder holder = forView(root, HeroViewHolder.class);
            return holder != null ? holder : new HeroViewHolder(root, parentViewHolder);
        }

        /* BEGIN lifecycle */

        @Override
        void onBind() {
            super.onBind();
            mRoot.setVisibility(mModel != null ? View.VISIBLE : View.GONE);
            bindHeading();
        }

        @Override
        void onVisible() {
            super.onVisible();
            if (mModel != null) {
                mPendingAnalyticsEvents =
                        triggerAnalyticsEvents(mModel.mAnalyticsEvents, Trigger.VISIBLE, Trigger.DEFAULT);
            }
        }

        @Override
        void onHidden() {
            super.onHidden();
            if (mPendingAnalyticsEvents != null) {
                cancelPendingAnalyticsEvents(mPendingAnalyticsEvents);
            }
        }

        /* END lifecycle */

        private void bindHeading() {
            final Text heading = mModel != null ? mModel.mHeading : null;
            Text.bind(heading, mHeading, R.dimen.text_size_hero_heading, Styles.getPrimaryColor(mModel));
            mHeading.setVisibility(heading != null ? View.VISIBLE : View.GONE);
        }
    }
}
