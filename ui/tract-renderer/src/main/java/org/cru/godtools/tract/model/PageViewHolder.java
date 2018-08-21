package org.cru.godtools.tract.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v4.util.ArraySet;
import android.support.v4.util.Pools;
import android.support.v4.view.ViewCompat;
import android.view.View;

import com.annimon.stream.IntPair;
import com.annimon.stream.Optional;
import com.annimon.stream.Stream;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import org.cru.godtools.base.model.Event;
import org.cru.godtools.tract.R2;
import org.cru.godtools.tract.viewmodel.ResourceViewUtils;
import org.cru.godtools.tract.widget.PageContentLayout;
import org.cru.godtools.tract.widget.ScaledPicassoImageView;

import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;

public class PageViewHolder extends ParentViewHolder<Page>
        implements Card.CardViewHolder.Callbacks, PageContentLayout.OnActiveCardListener,
        CallToActionViewHolder.Callbacks {
    public interface Callbacks {
        void onUpdateActiveCard(@Nullable Card card);

        void goToNextPage();
    }

    @BindView(R2.id.page)
    View mPageView;
    @BindView(R2.id.background_image)
    ScaledPicassoImageView mBackgroundImage;

    @BindView(R2.id.page_content_layout)
    PageContentLayout mPageContentLayout;

    @BindView(R2.id.header)
    View mHeader;
    @BindView(R2.id.hero)
    View mHero;
    @BindView(R2.id.call_to_action)
    View mCallToAction;

    @BindViews({R2.id.background_image, R2.id.initial_page_content})
    List<View> mLayoutDirectionViews;

    private boolean mBindingCards = false;
    private boolean mNeedsCardsRebind = false;
    @NonNull
    private Card[] mCards = new Card[0];
    private Set<String> mVisibleCards = new ArraySet<>();

    @NonNull
    private final Header.HeaderViewHolder mHeaderViewHolder;
    @NonNull
    private final Hero.HeroViewHolder mHeroViewHolder;
    @Nullable
    private Card.CardViewHolder mVisibleCardViewHolder;
    @NonNull
    private final Pools.Pool<Card.CardViewHolder> mRecycledCardViewHolders = new Pools.SimplePool<>(3);
    @NonNull
    private Card.CardViewHolder[] mCardViewHolders = new Card.CardViewHolder[0];
    @NonNull
    private final CallToActionViewHolder mCallToActionViewHolder;

    @Nullable
    private Callbacks mCallbacks;

    PageViewHolder(@NonNull final View root) {
        super(Page.class, root, null);
        mPageContentLayout.setActiveCardListener(this);
        mHeaderViewHolder = Header.HeaderViewHolder.forView(mHeader, this);
        mHeroViewHolder = Hero.HeroViewHolder.forView(mHero, this);
        mCallToActionViewHolder = CallToActionViewHolder.forView(mCallToAction, this);
        mCallToActionViewHolder.setCallbacks(this);
    }

    @NonNull
    public static PageViewHolder forView(@NonNull final View root) {
        final PageViewHolder holder = forView(root, PageViewHolder.class);
        return holder != null ? holder : new PageViewHolder(root);
    }

    // region Lifecycle Events

    @Override
    void onBind() {
        super.onBind();
        bindPage();
        mHeaderViewHolder.bind(mModel != null ? mModel.getHeader() : null);
        mHeroViewHolder.bind(mModel != null ? mModel.getHero() : null);
        updateDisplayedCards();
        mCallToActionViewHolder.bind(mModel != null ? mModel.getCallToAction() : null);
    }

    @Override
    void onVisible() {
        super.onVisible();

        // cascade event to currently visible hero or card
        if (mVisibleCardViewHolder != null) {
            mVisibleCardViewHolder.markVisible();
        } else {
            mHeroViewHolder.markVisible();
        }
    }

    public void onContentEvent(@NonNull final Event event) {
        checkForCardEvent(event);
    }

    @Override
    public void onActiveCardChanged(@Nullable final View activeCard) {
        if (!mBindingCards) {
            final Card.CardViewHolder cardViewHolder = BaseViewHolder.forView(activeCard, Card.CardViewHolder.class);
            final Optional<Card> card = Optional.ofNullable(cardViewHolder)
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

            updateVisibleCard(cardViewHolder);
        }
    }

    @Override
    public void onDismissCard(@NonNull final Card.CardViewHolder holder) {
        if (holder.mRoot == mPageContentLayout.getActiveCard()) {
            mPageContentLayout.changeActiveCard(null, true);
        }
    }

    @Override
    public void onToggleCard(@NonNull final Card.CardViewHolder holder) {
        mPageContentLayout
                .changeActiveCard(holder.mRoot != mPageContentLayout.getActiveCard() ? holder.mRoot : null, true);
    }

    @Override
    void onHidden() {
        super.onHidden();

        // cascade event to currently visible hero or card
        if (mVisibleCardViewHolder != null) {
            mVisibleCardViewHolder.markHidden();
        } else {
            mHeroViewHolder.markHidden();
        }
    }

    // endregion Lifecycle Events

    public void setCallbacks(@Nullable final Callbacks callbacks) {
        mCallbacks = callbacks;
    }

    @Nullable
    public Card getActiveCard() {
        final Card.CardViewHolder holder = forView(mPageContentLayout.getActiveCard(), Card.CardViewHolder.class);
        if (holder == null) {
            return null;
        }
        return holder.mModel;
    }

    private boolean isCardVisible(@NonNull final Card card) {
        return !card.isHidden() || mVisibleCards.contains(card.getId());
    }

    @Override
    protected void updateLayoutDirection() {
        // HACK: the root view should inherit it's layout direction so the call-to-action view can inherit as well.
        ViewCompat.setLayoutDirection(mRoot, ViewCompat.LAYOUT_DIRECTION_INHERIT);

        // force the layout direction for any other views that do care
        final int dir = Page.getLayoutDirection(mModel);
        ButterKnife.apply(mLayoutDirectionViews, (v, i) -> ViewCompat.setLayoutDirection(v, dir));
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
        ResourceViewUtils.bindBackgroundImage(mBackgroundImage, Page.getBackgroundImageResource(mModel),
                                              Page.getBackgroundImageScaleType(mModel),
                                              Page.getBackgroundImageGravity(mModel));
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
        final Card.CardViewHolder[] holders = new Card.CardViewHolder[mCards.length];
        View activeCard = null;
        int lastNewPos = -1;
        for (final Card.CardViewHolder holder : mCardViewHolders) {
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

    @Override
    public void goToNextPage() {
        if (mCallbacks != null) {
            mCallbacks.goToNextPage();
        }
    }

    private void updateVisibleCard(@Nullable final Card.CardViewHolder visibleCardViewHolder) {
        // update the visible card view holder
        final Card.CardViewHolder old = mVisibleCardViewHolder;
        mVisibleCardViewHolder = visibleCardViewHolder;

        // update state as necessary
        if (mVisible && mVisibleCardViewHolder != old) {
            if (old != null) {
                old.markHidden();
            } else {
                mHeroViewHolder.markHidden();
            }

            if (mVisibleCardViewHolder != null) {
                mVisibleCardViewHolder.markVisible();
            } else {
                mHeroViewHolder.markVisible();
            }
        }
    }

    private void checkForCardEvent(@NonNull final Event event) {
        // send event to current card
        final Card.CardViewHolder holder =
                BaseViewHolder.forView(mPageContentLayout.getActiveCard(), Card.CardViewHolder.class);
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
