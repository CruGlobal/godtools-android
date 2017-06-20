package org.cru.godtools.tract.model;

import android.support.annotation.DimenRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.ccci.gto.android.common.util.XmlPullParserUtils;
import org.cru.godtools.tract.R;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;

import static org.cru.godtools.tract.Constants.XMLNS_TRACT;

public final class Hero extends Base implements Styles {
    static final String XML_HERO = "hero";
    private static final String XML_HEADING = "heading";

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

    public static void bind(@Nullable final Hero hero, @Nullable final View view) {
        if (view != null) {
            final Text heading;
            if (hero != null) {
                view.setVisibility(View.VISIBLE);
                heading = hero.mHeading;
            } else {
                view.setVisibility(View.GONE);
                heading = null;
            }
            bindHeading(heading, ButterKnife.findById(view, R.id.hero_heading));
            bindContent(hero, ButterKnife.findById(view, R.id.hero_content));
        }
    }

    private static void bindHeading(@Nullable final Text heading, @Nullable final TextView view) {
        if (view != null) {
            if (heading != null) {
                view.setVisibility(View.VISIBLE);
                Text.bind(heading, view, heading.getPrimaryColor(), R.dimen.text_size_hero_heading);
            } else {
                view.setVisibility(View.GONE);
            }
        }
    }

    private static void bindContent(@Nullable final Hero hero, @Nullable final LinearLayout container) {
        if (container != null) {
            container.removeAllViews();
            if (hero != null) {
                Content.renderAll(container, hero.mContent);
            }
        }
    }
}
