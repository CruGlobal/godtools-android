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

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static org.cru.godtools.xml.Constants.XMLNS_ANALYTICS;
import static org.cru.godtools.xml.Constants.XMLNS_TRACT;
import static org.cru.godtools.xml.model.Utils.parseBoolean;
import static org.cru.godtools.xml.model.Utils.parseColor;
import static org.cru.godtools.xml.model.Utils.parseScaleType;

public final class Card extends Base implements Styles, Parent {
    static final String XML_CARD = "card";
    private static final String XML_LABEL = "label";
    private static final String XML_HIDDEN = "hidden";

    private static final ImageScaleType DEFAULT_BACKGROUND_IMAGE_SCALE_TYPE = ImageScaleType.FILL_X;
    private static final int DEFAULT_BACKGROUND_IMAGE_GRAVITY = ImageGravity.CENTER;

    private final int mPosition;

    private boolean mHidden = false;
    @NonNull
    private Set<Event.Id> mListeners = ImmutableSet.of();
    @NonNull
    private Set<Event.Id> mDismissListeners = ImmutableSet.of();

    @NonNull
    private Collection<AnalyticsEvent> mAnalyticsEvents = ImmutableSet.of();

    @Nullable
    @ColorInt
    private Integer mTextColor;

    @Nullable
    @ColorInt
    private Integer mBackgroundColor = null;
    @Nullable
    private String mBackgroundImage;
    private int mBackgroundImageGravity = DEFAULT_BACKGROUND_IMAGE_GRAVITY;
    @NonNull
    private ImageScaleType mBackgroundImageScaleType = DEFAULT_BACKGROUND_IMAGE_SCALE_TYPE;

    @Nullable
    private Text mLabel;

    @NonNull
    private List<Content> mContent = ImmutableList.of();

    private Card(@NonNull final Page parent, final int position) {
        super(parent);
        mPosition = position;
    }

    @NonNull
    public String getId() {
        return getPage().getId() + "-" + mPosition;
    }

    public int getPosition() {
        return mPosition;
    }

    public boolean isHidden() {
        return mHidden;
    }

    @NonNull
    public Set<Event.Id> getListeners() {
        return mListeners;
    }

    @NonNull
    public Set<Event.Id> getDismissListeners() {
        return mDismissListeners;
    }

    @NonNull
    public Collection<AnalyticsEvent> getAnalyticsEvents() {
        return mAnalyticsEvents;
    }

    @Nullable
    public Text getLabel() {
        return mLabel;
    }

    @Override
    public int getTextColor() {
        return mTextColor != null ? mTextColor : getPage().getCardTextColor();
    }

    @ColorInt
    private int getBackgroundColor() {
        // TODO: implement card-background-color on Page & Manifest
        return mBackgroundColor != null ? mBackgroundColor : Manifest.getBackgroundColor(getManifest());
    }

    @ColorInt
    public static int getBackgroundColor(@Nullable final Card card) {
        // TODO: implement card-background-color on Page & Manifest
        return card != null ? card.getBackgroundColor() : Manifest.getBackgroundColor(null);
    }

    public static Resource getBackgroundImageResource(@Nullable final Card card) {
        return card != null ? card.getResource(card.mBackgroundImage) : null;
    }

    public static int getBackgroundImageGravity(@Nullable final Card card) {
        return card != null ? card.mBackgroundImageGravity : DEFAULT_BACKGROUND_IMAGE_GRAVITY;
    }

    public static ImageScaleType getBackgroundImageScaleType(@Nullable final Card card) {
        return card != null ? card.mBackgroundImageScaleType : DEFAULT_BACKGROUND_IMAGE_SCALE_TYPE;
    }

    @NonNull
    @Override
    public List<Content> getContent() {
        return mContent;
    }

    @NonNull
    static Card fromXml(@NonNull final Page parent, @NonNull final XmlPullParser parser, final int position)
            throws IOException, XmlPullParserException {
        return new Card(parent, position).parse(parser);
    }

    @NonNull
    private Card parse(@NonNull final XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, XMLNS_TRACT, XML_CARD);

        mHidden = parseBoolean(parser.getAttributeValue(null, XML_HIDDEN), mHidden);
        mListeners = parseEvents(parser, XML_LISTENERS);
        mDismissListeners = parseEvents(parser, XML_DISMISS_LISTENERS);
        mTextColor = parseColor(parser, XML_TEXT_COLOR, mTextColor);
        mBackgroundColor = parseColor(parser, XML_BACKGROUND_COLOR, mBackgroundColor);
        mBackgroundImage = parser.getAttributeValue(null, XML_BACKGROUND_IMAGE);
        mBackgroundImageGravity = ImageGravity.parse(parser, XML_BACKGROUND_IMAGE_GRAVITY, mBackgroundImageGravity);
        mBackgroundImageScaleType = parseScaleType(parser, XML_BACKGROUND_IMAGE_SCALE_TYPE, mBackgroundImageScaleType);

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
                case XMLNS_TRACT:
                    switch (parser.getName()) {
                        case XML_LABEL:
                            mLabel = Text.fromNestedXml(this, parser, XMLNS_TRACT, XML_LABEL);
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
