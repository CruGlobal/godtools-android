package org.cru.godtools.xml.model;

import android.graphics.Color;

import com.annimon.stream.Stream;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import org.ccci.gto.android.common.util.XmlPullParserUtils;
import org.cru.godtools.base.model.Event;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.annotation.WorkerThread;

import static org.cru.godtools.xml.Constants.XMLNS_MANIFEST;
import static org.cru.godtools.xml.Constants.XMLNS_TRACT;
import static org.cru.godtools.xml.model.CallToAction.XML_CALL_TO_ACTION;
import static org.cru.godtools.xml.model.Card.XML_CARD;
import static org.cru.godtools.xml.model.Header.XML_HEADER;
import static org.cru.godtools.xml.model.Hero.XML_HERO;
import static org.cru.godtools.xml.model.Modal.XML_MODAL;
import static org.cru.godtools.xml.model.Utils.parseColor;
import static org.cru.godtools.xml.model.Utils.parseScaleType;

public final class Page extends Base implements Styles, Parent {
    static final String XML_PAGE = "page";
    private static final String XML_MANIFEST_FILENAME = "filename";
    private static final String XML_MANIFEST_SRC = "src";
    private static final String XML_CARDS = "cards";
    private static final String XML_MODALS = "modals";
    private static final String XML_CARD_TEXT_COLOR = "card-text-color";

    @ColorInt
    private static final int DEFAULT_BACKGROUND_COLOR = Color.TRANSPARENT;
    private static final ImageScaleType DEFAULT_BACKGROUND_IMAGE_SCALE_TYPE = ImageScaleType.FILL_X;
    private static final int DEFAULT_BACKGROUND_IMAGE_GRAVITY = ImageGravityKt.CENTER;

    private final int mPosition;

    @Nullable
    private String mFileName;
    @Nullable
    private String mLocalFileName;
    private boolean mPageXmlParsed = false;

    @NonNull
    private Set<Event.Id> mListeners = ImmutableSet.of();

    @Nullable
    @ColorInt
    private Integer mPrimaryColor = null;
    @Nullable
    @ColorInt
    private Integer mPrimaryTextColor = null;
    @Nullable
    @ColorInt
    private Integer mTextColor = null;
    @Nullable
    @ColorInt
    private Integer mCardTextColor = null;
    @ColorInt
    private int mBackgroundColor = DEFAULT_BACKGROUND_COLOR;
    @Nullable
    private String mBackgroundImage;
    private int mBackgroundImageGravity = DEFAULT_BACKGROUND_IMAGE_GRAVITY;
    @NonNull
    private ImageScaleType mBackgroundImageScaleType = DEFAULT_BACKGROUND_IMAGE_SCALE_TYPE;

    @Nullable
    private Header mHeader;
    @Nullable
    private Hero mHero;
    private List<Card> mCards = ImmutableList.of();
    private List<Modal> mModals = ImmutableList.of();
    @NonNull
    private CallToAction mCallToAction;

    @VisibleForTesting
    Page(@NonNull final Manifest manifest, final int position) {
        super(manifest);
        mPosition = position;
        mCallToAction = new CallToAction(this);
    }

    @NonNull
    public String getId() {
        if (mFileName != null) {
            return mFileName;
        }

        return getManifest().getCode() + "-" + mPosition;
    }

    @NonNull
    @Override
    public Page getPage() {
        return this;
    }

    public int getPosition() {
        return mPosition;
    }

    public boolean isLastPage() {
        return mPosition == getManifest().getPages().size() - 1;
    }

    @Nullable
    public String getLocalFileName() {
        return mLocalFileName;
    }

    @NonNull
    public Set<Event.Id> getListeners() {
        return mListeners;
    }

    @ColorInt
    @Override
    public int getPrimaryColor() {
        return mPrimaryColor != null ? mPrimaryColor : StylesKt.getPrimaryColor(getStylesParent());
    }

    @ColorInt
    @Override
    public int getPrimaryTextColor() {
        return mPrimaryTextColor != null ? mPrimaryTextColor : StylesKt.getPrimaryTextColor(getStylesParent());
    }

    @ColorInt
    @Override
    public int getTextColor() {
        return mTextColor != null ? mTextColor : StylesKt.getTextColor(getStylesParent());
    }

    @ColorInt
    int getCardTextColor() {
        return mCardTextColor != null ? mCardTextColor : getTextColor();
    }

    @ColorInt
    public static int getBackgroundColor(@Nullable final Page page) {
        return page != null ? page.mBackgroundColor : DEFAULT_BACKGROUND_COLOR;
    }

    @Nullable
    public static Resource getBackgroundImageResource(@Nullable final Page page) {
        return page != null ? page.getResource(page.mBackgroundImage) : null;
    }

    public static int getBackgroundImageGravity(@Nullable final Page page) {
        return page != null ? page.mBackgroundImageGravity : DEFAULT_BACKGROUND_IMAGE_GRAVITY;
    }

    @NonNull
    public static ImageScaleType getBackgroundImageScaleType(@Nullable final Page page) {
        return page != null ? page.mBackgroundImageScaleType : DEFAULT_BACKGROUND_IMAGE_SCALE_TYPE;
    }

    @Nullable
    public Header getHeader() {
        return mHeader;
    }

    @Nullable
    public Hero getHero() {
        return mHero;
    }

    @NonNull
    @Override
    public List<Content> getContent() {
        return ImmutableList.of();
    }

    @NonNull
    public List<Card> getCards() {
        return mCards;
    }

    @NonNull
    public List<Modal> getModals() {
        return mModals;
    }

    @Nullable
    public Modal findModal(@Nullable final String id) {
        return Stream.of(mModals)
                .filter(m -> m.getId().equalsIgnoreCase(id))
                .findFirst().orElse(null);
    }

    @NonNull
    public CallToAction getCallToAction() {
        return mCallToAction;
    }

    @NonNull
    @WorkerThread
    static Page fromManifestXml(@NonNull final Manifest manifest, final int position,
                                @NonNull final XmlPullParser parser)
            throws XmlPullParserException, IOException {
        return new Page(manifest, position).parseManifestXml(parser);
    }

    @WorkerThread
    private Page parseManifestXml(@NonNull final XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, XMLNS_MANIFEST, XML_PAGE);

        mFileName = parser.getAttributeValue(null, XML_MANIFEST_FILENAME);
        mLocalFileName = parser.getAttributeValue(null, XML_MANIFEST_SRC);

        // discard any nested nodes
        XmlPullParserUtils.skipTag(parser);

        return this;
    }

    @WorkerThread
    public void parsePageXml(@NonNull final XmlPullParser parser) throws IOException, XmlPullParserException {
        // make sure we haven't parsed this page XML already
        if (mPageXmlParsed) {
            throw new IllegalStateException("Page XML already parsed");
        }
        parser.require(XmlPullParser.START_TAG, XMLNS_TRACT, XML_PAGE);

        mListeners = parseEvents(parser, XML_LISTENERS);
        mPrimaryColor = parseColor(parser, XML_PRIMARY_COLOR, mPrimaryColor);
        mPrimaryTextColor = parseColor(parser, XML_PRIMARY_TEXT_COLOR, mPrimaryTextColor);
        mTextColor = parseColor(parser, XML_TEXT_COLOR, mTextColor);
        mCardTextColor = parseColor(parser, XML_CARD_TEXT_COLOR, mCardTextColor);
        mBackgroundColor = parseColor(parser, XML_BACKGROUND_COLOR, mBackgroundColor);
        mBackgroundImage = parser.getAttributeValue(null, XML_BACKGROUND_IMAGE);
        mBackgroundImageGravity = ImageGravityKt.parse(parser, XML_BACKGROUND_IMAGE_GRAVITY, mBackgroundImageGravity);
        mBackgroundImageScaleType = parseScaleType(parser, XML_BACKGROUND_IMAGE_SCALE_TYPE, mBackgroundImageScaleType);

        // process any child elements
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            // process recognized nodes
            switch (parser.getNamespace()) {
                case XMLNS_TRACT:
                    switch (parser.getName()) {
                        case XML_HEADER:
                            mHeader = Header.fromXml(this, parser);
                            continue;
                        case XML_HERO:
                            mHero = Hero.fromXml(this, parser);
                            continue;
                        case XML_CARDS:
                            parseCardsXml(parser);
                            continue;
                        case XML_MODALS:
                            parseModalsXml(parser);
                            continue;
                        case XML_CALL_TO_ACTION:
                            mCallToAction = new CallToAction(this, parser);
                            continue;
                    }
                    break;
            }

            // skip unrecognized nodes
            XmlPullParserUtils.skipTag(parser);
        }

        // mark page XML as parsed
        mPageXmlParsed = true;
    }

    private void parseCardsXml(@NonNull final XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, XMLNS_TRACT, XML_CARDS);

        // process any child elements
        final List<Card> cards = new ArrayList<>();
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            // process recognized nodes
            switch (parser.getNamespace()) {
                case XMLNS_TRACT:
                    switch (parser.getName()) {
                        case XML_CARD:
                            cards.add(Card.fromXml(this, parser, cards.size()));
                            continue;
                    }
                    break;
            }

            // skip unrecognized nodes
            XmlPullParserUtils.skipTag(parser);
        }
        mCards = ImmutableList.copyOf(cards);
    }

    private void parseModalsXml(@NonNull final XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, XMLNS_TRACT, XML_MODALS);

        // process any child elements
        final List<Modal> modals = new ArrayList<>();
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            // process recognized nodes
            switch (parser.getNamespace()) {
                case XMLNS_TRACT:
                    switch (parser.getName()) {
                        case XML_MODAL:
                            modals.add(Modal.fromXml(this, parser, modals.size()));
                            continue;
                    }
                    break;
            }

            // skip unrecognized nodes
            XmlPullParserUtils.skipTag(parser);
        }
        mModals = ImmutableList.copyOf(modals);
    }
}
