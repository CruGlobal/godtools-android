package org.cru.godtools.tract.viewmodel;

import android.content.SharedPreferences;
import android.view.View;

import com.annimon.stream.IntPair;
import com.annimon.stream.Optional;
import com.annimon.stream.Stream;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import org.cru.godtools.api.model.NavigationEvent;
import org.cru.godtools.base.Settings;
import org.cru.godtools.base.model.Event;
import org.cru.godtools.tract.R2;
import org.cru.godtools.tract.widget.PageContentLayout;
import org.cru.godtools.xml.model.Card;
import org.cru.godtools.xml.model.Page;

import java.util.Set;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.collection.ArraySet;
import androidx.core.util.Pools;
import butterknife.BindView;

import static org.cru.godtools.base.Settings.FEATURE_TRACT_CARD_CLICKED;
import static org.cru.godtools.base.Settings.FEATURE_TRACT_CARD_SWIPED;
import static org.cru.godtools.base.Settings.PREF_FEATURE_DISCOVERED;

public class PageViewHolder extends ParentViewHolder<Page>
        implements PageContentLayout.OnActiveCardListener, CardViewHolder.Callbacks,
        SharedPreferences.OnSharedPreferenceChangeListener {
    public interface Callbacks {
        void onUpdateActiveCard(@Nullable Card card);

        void goToNextPage();
    }

    @NonNull
    private final Settings mSettings;

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
    private final HeroViewHolder mHeroViewHolder;
    @Nullable
    private CardViewHolder mActiveCardViewHolder;
    @NonNull
    private final Pools.Pool<CardViewHolder> mRecycledCardViewHolders = new Pools.SimplePool<>(3);
    @NonNull
    private CardViewHolder[] mCardViewHolders = new CardViewHolder[0];

    @Nullable
    private Callbacks mCallbacks;

    PageViewHolder(@NonNull final View root) {
        super(Page.class, root, null);
        mSettings = Settings.Companion.getInstance(root.getContext());

        mPageContentLayout.setActiveCardListener(this);
        mHeroViewHolder = HeroViewHolder.forView(mHero, this);
    }

    @NonNull
    public static PageViewHolder forView(@NonNull final View root) {
        final PageViewHolder holder = forView(root, PageViewHolder.class);
        return holder != null ? holder : new PageViewHolder(root);
    }

    // region Lifecycle Events

    @Override
    protected void onBind() {
        super.onBind();
        mHeroViewHolder.bind(mModel != null ? mModel.getHero() : null);
        updateDisplayedCards();
    }

    @Override
    void onVisible() {
        super.onVisible();
        mSettings.registerOnSharedPreferenceChangeListener(this);

        // cascade event to currently visible hero or card
        if (mActiveCardViewHolder != null) {
            mActiveCardViewHolder.markVisible();
        } else {
            mHeroViewHolder.markVisible();
        }

        updateBounceAnimation();
    }

    @Override
    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
        switch (key) {
            case PREF_FEATURE_DISCOVERED + FEATURE_TRACT_CARD_CLICKED:
            case PREF_FEATURE_DISCOVERED + FEATURE_TRACT_CARD_SWIPED:
                updateBounceAnimation();
                break;
        }
    }

    @Override
    @CallSuper
    public void onContentEvent(@NonNull final Event event) {
        super.onContentEvent(event);
        checkForCardEvent(event);
        propagateEventToChildren(event);
    }

    public void onBroadcastEvent(@NonNull final NavigationEvent event) {
        if (mModel != null && Integer.valueOf(mModel.getPosition()).equals(event.getPage())) {
            final Card card = event.getCard() != null ? mModel.getCards().get(event.getCard()) : null;
            if (card != null) {
                displayCard(card);
            } else {
                mPageContentLayout.changeActiveCard(null, true);
            }
        }
    }

    @Override
    public void onActiveCardChanged(@Nullable final View activeCard) {
        if (!mBindingCards) {
            final CardViewHolder old = mActiveCardViewHolder;
            mActiveCardViewHolder = BaseViewHolder.forView(activeCard, CardViewHolder.class);
            hideHiddenCardsThatArentActive();
            updateVisibleCard(old);

            if (mCallbacks != null) {
                final Optional<Card> card = Optional.ofNullable(mActiveCardViewHolder)
                        .map(BaseViewHolder::getModel);
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
        mSettings.setFeatureDiscovered(Settings.FEATURE_TRACT_CARD_CLICKED);
        mPageContentLayout
                .changeActiveCard(holder.mRoot != mPageContentLayout.getActiveCard() ? holder.mRoot : null, true);
    }

    @Override
    public void onNextCard() {
        mPageContentLayout.changeActiveCard(mPageContentLayout.getActiveCardPosition() + 1, true);
    }

    @Override
    public void onPreviousCard() {
        mPageContentLayout.changeActiveCard(mPageContentLayout.getActiveCardPosition() - 1, true);
    }

    @Override
    void onHidden() {
        super.onHidden();
        mSettings.unregisterOnSharedPreferenceChangeListener(this);

        // cascade event to currently visible hero or card
        if (mActiveCardViewHolder != null) {
            mActiveCardViewHolder.markHidden();
        } else {
            mHeroViewHolder.markHidden();
        }

        updateBounceAnimation();
    }

    // endregion Lifecycle Events

    public void setCallbacks(@Nullable final Callbacks callbacks) {
        mCallbacks = callbacks;
    }

    @Nullable
    public Card getActiveCard() {
        return mActiveCardViewHolder != null ? mActiveCardViewHolder.mModel : null;
    }

    private boolean isCardVisible(@NonNull final Card card) {
        return !card.isHidden() || mVisibleCards.contains(card.getId());
    }

    @Override
    protected void updateLayoutDirection() {
    }

    private void updateDisplayedCards() {
        mCards = Optional.ofNullable(mModel).stream()
                .map(Page::getCards)
                .flatMap(Stream::of)
                .filter(this::isCardVisible)
                .toArray(Card[]::new);

        bindCards();
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
        final View invalid = mPageContentLayout; // We just need a non-null placeholder value that can't be a card view
        View activeCard = mPageContentLayout.getActiveCard() != null ? invalid : null;
        final CardViewHolder[] holders = new CardViewHolder[mCards.length];
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
                if (activeCard == invalid && mPageContentLayout.getActiveCard() == holder.mRoot) {
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
                    holders[pos] = CardViewHolder.create(mPageContentLayout, this);
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
        if (activeCard != invalid) {
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

    private void updateVisibleCard(@Nullable final CardViewHolder old) {
        // update visibility state as necessary
        if (mVisible && old != mActiveCardViewHolder) {
            if (old != null) {
                old.markHidden();
            } else {
                mHeroViewHolder.markHidden();
            }

            if (mActiveCardViewHolder != null) {
                mActiveCardViewHolder.markVisible();
            } else {
                mHeroViewHolder.markVisible();
            }
        }
    }

    private void hideHiddenCardsThatArentActive() {
        // only process if we have explicitly visible cards
        if (mVisibleCards.size() > 0) {
            final Optional<Card> card = Optional.ofNullable(mActiveCardViewHolder)
                    .map(BaseViewHolder::getModel);

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
    }

    private void propagateEventToChildren(@NonNull final Event event) {
        if (mActiveCardViewHolder != null) {
            mActiveCardViewHolder.onContentEvent(event);
        } else {
            mHeroViewHolder.onContentEvent(event);
        }
    }

    private void checkForCardEvent(@NonNull final Event event) {
        // check for card display event
        if (mModel != null) {
            Stream.of(mModel.getCards())
                    .filter(c -> c.getListeners().contains(event.id))
                    .findFirst()
                    .ifPresent(this::displayCard);
        }
    }

    private void updateBounceAnimation() {
        // we bounce the first card if the page is visible and the user hasn't opened a card before
        mPageContentLayout.setBounceFirstCard(mVisible && !(mSettings.isFeatureDiscovered(FEATURE_TRACT_CARD_CLICKED) ||
                mSettings.isFeatureDiscovered(FEATURE_TRACT_CARD_SWIPED)));
    }
}
