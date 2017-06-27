package org.cru.godtools.tract.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.view.ViewGroup;

import com.google.common.collect.ImmutableList;

import org.ccci.gto.android.common.util.XmlPullParserUtils;
import org.cru.godtools.tract.R;
import org.cru.godtools.tract.model.Tabs.TabsViewHolder;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.List;

import static org.cru.godtools.tract.Constants.XMLNS_CONTENT;

public final class Tab extends Base implements Parent {
    static final String XML_TAB = "tab";
    private static final String XML_LABEL = "label";

    private final int mPosition;

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

        // process any child elements
        final ImmutableList.Builder<Content> contentList = ImmutableList.builder();
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            // process recognized nodes
            switch (parser.getNamespace()) {
                case XMLNS_CONTENT:
                    switch (parser.getName()) {
                        case XML_LABEL:
                            mLabel = Text.fromNestedXml(this, parser, XMLNS_CONTENT, XML_LABEL);
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

    @NonNull
    public static TabViewHolder createViewHolder(@NonNull final ViewGroup parent,
                                                 @Nullable final TabsViewHolder tabsViewHolder) {
        return new TabViewHolder(parent, tabsViewHolder);
    }

    @UiThread
    public static final class TabViewHolder extends ParentViewHolder<Tab> {
        TabViewHolder(@NonNull final ViewGroup parent, @Nullable final TabsViewHolder parentViewHolder) {
            super(Tab.class, parent, R.layout.tract_content_paragraph, parentViewHolder);
        }
    }
}
