package org.cru.godtools.xml.model;

import android.graphics.Color;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import org.ccci.gto.android.common.util.XmlPullParserUtils;
import org.cru.godtools.base.model.Event;
import org.cru.godtools.xml.R;
import org.cru.godtools.xml.model.Text.Align;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import androidx.annotation.DimenRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import static org.cru.godtools.xml.Constants.XMLNS_TRACT;

public final class Modal extends Base implements Parent, Styles {
    static final String XML_MODAL = "modal";
    private static final String XML_TITLE = "title";

    private static final Align DEFAULT_TEXT_ALIGN = Align.CENTER;

    private final int mPosition;

    @NonNull
    private Set<Event.Id> mListeners = ImmutableSet.of();
    @NonNull
    private Set<Event.Id> mDismissListeners = ImmutableSet.of();

    @Nullable
    private Text mTitle;

    @NonNull
    private List<Content> mContent = ImmutableList.of();

    @VisibleForTesting
    Modal(@NonNull final Base parent, final int position) {
        super(parent);
        mPosition = position;
    }

    @NonNull
    public String getId() {
        return getPage().getId() + "-" + mPosition;
    }

    @NonNull
    public Set<Event.Id> getListeners() {
        return mListeners;
    }

    @NonNull
    public Set<Event.Id> getDismissListeners() {
        return mDismissListeners;
    }

    @Nullable
    public Text getTitle() {
        return mTitle;
    }

    @NonNull
    @Override
    public List<Content> getContent() {
        return mContent;
    }

    @Override
    public int getTextColor() {
        return Color.WHITE;
    }

    @Override
    public int getPrimaryColor() {
        return Color.TRANSPARENT;
    }

    @Override
    public int getPrimaryTextColor() {
        return Color.WHITE;
    }

    @Override
    public int getButtonColor() {
        return Color.WHITE;
    }

    @DimenRes
    @Override
    public int getTextSize() {
        return R.dimen.text_size_modal;
    }

    @NonNull
    @Override
    public Align getTextAlign() {
        return DEFAULT_TEXT_ALIGN;
    }

    @NonNull
    static Modal fromXml(@NonNull final Base parent, @NonNull final XmlPullParser parser, final int position)
            throws IOException, XmlPullParserException {
        return new Modal(parent, position).parse(parser);
    }

    @NonNull
    private Modal parse(@NonNull final XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, XMLNS_TRACT, XML_MODAL);

        mListeners = parseEvents(parser, XML_LISTENERS);
        mDismissListeners = parseEvents(parser, XML_DISMISS_LISTENERS);

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
