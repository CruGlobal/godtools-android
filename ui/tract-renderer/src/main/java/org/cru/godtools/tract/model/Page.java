package org.cru.godtools.tract.model;

import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.annotation.WorkerThread;
import android.support.v4.util.ArraySet;
import android.support.v4.util.Pools;
import android.view.View;

import com.annimon.stream.IntPair;
import com.annimon.stream.Optional;
import com.annimon.stream.Stream;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import org.ccci.gto.android.common.util.XmlPullParserUtils;
import org.cru.godtools.base.model.Event;
import org.cru.godtools.tract.R2;
import org.cru.godtools.tract.model.Card.CardViewHolder;
import org.cru.godtools.tract.widget.PageContentLayout;
import org.cru.godtools.tract.widget.ScaledPicassoImageView;
import org.cru.godtools.tract.widget.ScaledPicassoImageView.ScaleType;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import butterknife.BindView;

import static org.cru.godtools.tract.Constants.XMLNS_MANIFEST;
import static org.cru.godtools.tract.Constants.XMLNS_TRACT;
import static org.cru.godtools.tract.model.CallToAction.XML_CALL_TO_ACTION;
import static org.cru.godtools.tract.model.Card.XML_CARD;
import static org.cru.godtools.tract.model.Header.XML_HEADER;
import static org.cru.godtools.tract.model.Hero.XML_HERO;
import static org.cru.godtools.tract.model.Modal.XML_MODAL;
import static org.cru.godtools.tract.model.Utils.parseColor;
import static org.cru.godtools.tract.model.Utils.parseScaleType;

public final class Page extends Base implements Styles, Parent {
    static final String XML_PAGE = "page";
    private static final String XML_MANIFEST_FILENAME = "filename";
    private static final String XML_MANIFEST_SRC = "src";
    private static final String XML_CARDS = "cards";
    private static final String XML_MODALS = "modals";
    private static final String XML_CARD_TEXT_COLOR = "card-text-color";

    @ColorInt
    private static final int DEFAULT_BACKGROUND_COLOR = Color.TRANSPARENT;
    private static final ScaleType DEFAULT_BACKGROUND_IMAGE_SCALE_TYPE = ScaleType.FILL_X;
    private static final int DEFAULT_BACKGROUND_IMAGE_GRAVITY = ImageGravity.CENTER;

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
    private ScaleType mBackgroundImageScaleType = DEFAULT_BACKGROUND_IMAGE_SCALE_TYPE;

    @Nullable
    private Header mHeader;
    @Nullable
    private Hero mHero;
    private List<Card> mCards = ImmutableList.of();
    private List<Modal> mModals = ImmutableList.of();
    @NonNull
    private CallToAction mCallToAction;

    private Page(@NonNull final Manifest manifest, final int position) {
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
    protected Page getPage() {
        return this;
    }

    public int getPosition() {
        return mPosition;
    }

    boolean isLastPage() {
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
        return mPrimaryColor != null ? mPrimaryColor : Styles.getPrimaryColor(getStylesParent());
    }

    @ColorInt
    @Override
    public int getPrimaryTextColor() {
        return mPrimaryTextColor != null ? mPrimaryTextColor : Styles.getPrimaryTextColor(getStylesParent());
    }

    @ColorInt
    @Override
    public int getTextColor() {
        return mTextColor != null ? mTextColor : Styles.getTextColor(getStylesParent());
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
    static Resource getBackgroundImageResource(@Nullable final Page page) {
        return page != null ? page.getResource(page.mBackgroundImage) : null;
    }

    static int getBackgroundImageGravity(@Nullable final Page page) {
        return page != null ? page.mBackgroundImageGravity : DEFAULT_BACKGROUND_IMAGE_GRAVITY;
    }

    @NonNull
    static ScaleType getBackgroundImageScaleType(@Nullable final Page page) {
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
        mBackgroundImageGravity = ImageGravity.parse(parser, XML_BACKGROUND_IMAGE_GRAVITY, mBackgroundImageGravity);
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
                            mCallToAction = CallToAction.fromXml(this, parser);
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

    @NonNull
    public static PageViewHolder getViewHolder(@NonNull final View root) {
        final PageViewHolder holder = BaseViewHolder.forView(root, PageViewHolder.class);
        return holder != null ? holder : new PageViewHolder(root);
    }

    public static class PageViewHolder extends Parent.ParentViewHolder<Page>
            implements CardViewHolder.Callbacks, PageContentLayout.OnActiveCardListener {
        public interface Callbacks {
            void onUpdateActiveCard(@Nullable Card card);
        }

        @BindView(R2.id.page)
        View mPageView;
        @BindView(R2.id.background_image)
        ScaledPicassoImageView mBackgroundImage;

        @BindView(R2.id.page_content_layout)
        PageContentLayout mPageContentLayout;

        @BindView(R2.id.hero)
        View mHero;

        private boolean mBindingCards = false;
        private boolean mNeedsCardsRebind = false;
        @NonNull
        private Card[] mCards = new Card[0];
        private Set<String> mVisibleCards = new ArraySet<>();

        @NonNull
        private final Hero.HeroViewHolder mHeroViewHolder;
        @NonNull
        private final Pools.Pool<CardViewHolder> mRecycledCardViewHolders = new Pools.SimplePool<>(3);
        @NonNull
        private CardViewHolder[] mCardViewHolders = new CardViewHolder[0];

        @Nullable
        private Callbacks mCallbacks;

        PageViewHolder(@NonNull final View root) {
            super(Page.class, root, null);
            mPageContentLayout.setActiveCardListener(this);
            mHeroViewHolder = Hero.getViewHolder(mHero, this);
        }

        /* BEGIN lifecycle */

        @Override
        void onBind() {
            super.onBind();
            bindPage();
            bindHero();
            updateDisplayedCards();
        }

        public void onContentEvent(@NonNull final Event event) {
            checkForCardEvent(event);
        }

        @Override
        public void onActiveCardChanged(@Nullable final View activeCard) {
            if (!mBindingCards) {
                final Optional<Card> card =
                        Optional.ofNullable(BaseViewHolder.forView(activeCard, CardViewHolder.class))
                                .map(BaseViewHolder::getModel);

                // only process if we have explicitly visible cards
                if (mVisibleCards.size() > 0) {
                    // generate a set containing the id of the current active card
                    final Set<String> id = card
                            .map(Card::getId)
                            .map(ImmutableSet::of)
                            .orElseGet(ImmutableSet::of);

                    // remove any non-matching ids from the visible cards set
                    final Set<String> diff = Sets.difference(mVisibleCards, id);
                    if (diff.size() > 0) {
                        mVisibleCards.removeAll(diff);
                        updateDisplayedCards();
                    }
                }

                if (mCallbacks != null) {
                    mCallbacks.onUpdateActiveCard(card.orElse(null));
                }
            }
        }

        @Override
        public void onDismissCard(@NonNull final CardViewHolder holder) {
            if (holder.mRoot == mPageContentLayout.getActiveCard()) {
                mPageContentLayout.changeActiveCard(null, true);
            }
        }

        @Override
        public void onToggleCard(@NonNull final CardViewHolder holder) {
            mPageContentLayout
                    .changeActiveCard(holder.mRoot != mPageContentLayout.getActiveCard() ? holder.mRoot : null, true);
        }

        /* END lifecycle */

        public void setCallbacks(@Nullable final Callbacks callbacks) {
            mCallbacks = callbacks;
        }

        @Nullable
        public Card getActiveCard() {
            final CardViewHolder holder = forView(mPageContentLayout.getActiveCard(), CardViewHolder.class);
            if (holder == null) {
                return null;
            }
            return holder.mModel;
        }

        private boolean isCardVisible(@NonNull final Card card) {
            return !card.isHidden() || mVisibleCards.contains(card.getId());
        }

        private void updateDisplayedCards() {
            mCards = Optional.ofNullable(mModel).stream()
                    .map(Page::getCards)
                    .flatMap(Stream::of)
                    .filter(this::isCardVisible)
                    .toArray(Card[]::new);

            bindCards();
        }

        private void bindPage() {
            mPageView.setBackgroundColor(Page.getBackgroundColor(mModel));
            Resource.bindBackgroundImage(mBackgroundImage, getBackgroundImageResource(mModel),
                                         getBackgroundImageScaleType(mModel), getBackgroundImageGravity(mModel));
        }

        private void bindHero() {
            mHeroViewHolder.bind(mModel != null ? mModel.getHero() : null);
        }

        @UiThread
        private void bindCards() {
            // short-circuit since we are already binding cards
            if (mBindingCards) {
                mNeedsCardsRebind = true;
                return;
            }
            mBindingCards = true;
            mNeedsCardsRebind = false;

            // map old view holders to new location
            final CardViewHolder[] holders = new CardViewHolder[mCards.length];
            View activeCard = null;
            int lastNewPos = -1;
            for (final CardViewHolder holder : mCardViewHolders) {
                final Card card = holder.getModel();
                final String id = card != null ? card.getId() : null;
                final int newPos = Stream.of(mCards).indexed()
                        .filter(c -> c.getSecond().getId().equals(id))
                        .findFirst()
                        .mapToInt(IntPair::getFirst)
                        .orElse(-1);

                // store the ViewHolder at the correct new location
                if (newPos == -1) {
                    // recycle this view holder
                    mPageContentLayout.removeView(holder.mRoot);
                    holder.bind(null);
                    mRecycledCardViewHolders.release(holder);
                } else {
                    holders[newPos] = holder;

                    // is this the active card? if so track it to restore it after we finish binding
                    if (activeCard == null && mPageContentLayout.getActiveCard() == holder.mRoot) {
                        activeCard = holder.mRoot;
                    }

                    if (lastNewPos > newPos) {
                        // remove this view for now, we will re-add it shortly
                        mPageContentLayout.removeView(holder.mRoot);
                    } else {
                        lastNewPos = newPos;
                    }
                }
            }

            // bind and create any needed view holders
            for (int pos = 0; pos < holders.length; pos++) {
                // create any missing view holders
                if (holders[pos] == null) {
                    holders[pos] = mRecycledCardViewHolders.acquire();
                    if (holders[pos] == null) {
                        holders[pos] = Card.createViewHolder(mPageContentLayout, this);
                    }
                }

                // add views to container if they aren't already there
                if (holders[pos].mRoot.getParent() != mPageContentLayout) {
                    mPageContentLayout.addCard(holders[pos].mRoot, pos);
                }

                // bind data
                holders[pos].bind(mCards[pos]);
            }

            // replace the list of active card view holders
            mCardViewHolders = holders;

            // finished binding cards
            mBindingCards = false;

            // restore the active card
            if (activeCard != null) {
                mPageContentLayout.changeActiveCard(activeCard, false);
            } else {
                // trigger onActiveCard in case the active card changed during binding
                onActiveCardChanged(mPageContentLayout.getActiveCard());
            }

            // rebind cards if a request to bind happened while we were already binding
            if (mNeedsCardsRebind) {
                bindCards();
            }
        }

        private void displayCard(@NonNull final Card card) {
            final String cardId = card.getId();
            if (card.isHidden()) {
                mVisibleCards.add(cardId);
                updateDisplayedCards();
            }

            // navigate to this specified card
            for (int i = 0; i < mCards.length; i++) {
                if (mCards[i].getId().equals(cardId)) {
                    mPageContentLayout.changeActiveCard(i, true);
                    return;
                }
            }

        }

        private void checkForCardEvent(@NonNull final Event event) {
            // send event to current card
            final CardViewHolder holder =
                    BaseViewHolder.forView(mPageContentLayout.getActiveCard(), CardViewHolder.class);
            if (holder != null) {
                holder.onContentEvent(event);
            }

            // check for card display event
            if (mModel != null) {
                Stream.of(mModel.getCards())
                        .filter(c -> c.getListeners().contains(event.id))
                        .findFirst()
                        .ifPresent(this::displayCard);
            }
        }
    }
}
