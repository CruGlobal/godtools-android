package org.cru.godtools.tract.viewmodel;

import android.view.View;

import com.annimon.stream.IntPair;
import com.annimon.stream.Optional;
import com.annimon.stream.Stream;

import org.cru.godtools.tract.R2;
import org.cru.godtools.tract.databinding.TractPageBinding;
import org.cru.godtools.tract.ui.controller.CardController;
import org.cru.godtools.tract.widget.PageContentLayout;
import org.cru.godtools.xml.model.Card;
import org.cru.godtools.xml.model.Page;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.core.util.Pools;
import butterknife.BindView;

public abstract class PageViewHolder extends BaseViewHolder<Page>
        implements PageContentLayout.OnActiveCardListener, CardController.Callbacks {
    public interface Callbacks {
        void onUpdateActiveCard(@Nullable Card card);

        void goToNextPage();
    }

    @BindView(R2.id.page_content_layout)
    PageContentLayout mPageContentLayout;

    private boolean mBindingCards = false;
    private boolean mNeedsCardsRebind = false;
    @NonNull
    protected Card[] mCards = new Card[0];

    @Nullable
    protected CardController mActiveCardViewHolder;
    @NonNull
    private final Pools.Pool<CardController> mRecycledCardViewHolders = new Pools.SimplePool<>(3);
    @NonNull
    private CardController[] mCardViewHolders = new CardController[0];

    @Nullable
    private Callbacks mCallbacks;

    public PageViewHolder(@NonNull final TractPageBinding binding) {
        super(Page.class, binding.getRoot(), null);

        mPageContentLayout.setActiveCardListener(this);
    }

    // region Lifecycle Events

    @Override
    protected void onBind() {
        super.onBind();
        updateDisplayedCards();
    }

    @Override
    public void onActiveCardChanged(@Nullable final View activeCard) {
        if (!mBindingCards) {
            final CardController old = mActiveCardViewHolder;
            mActiveCardViewHolder = BaseViewHolder.forView(activeCard, CardController.class);
            hideHiddenCardsThatArentActive();
            updateVisibleCard(old);

            if (mCallbacks != null) {
                final Optional<Card> card = Optional.ofNullable(mActiveCardViewHolder)
                        .map(BaseViewHolder::getModel);
                mCallbacks.onUpdateActiveCard(card.orElse(null));
            }
        }
    }

    // endregion Lifecycle Events

    public void setCallbacks(@Nullable final Callbacks callbacks) {
        mCallbacks = callbacks;
    }

    protected abstract boolean isCardVisible(@NonNull Card card);

    @Override
    protected void updateLayoutDirection() {
    }

    protected void updateDisplayedCards() {
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
        final CardController[] holders = new CardController[mCards.length];
        int lastNewPos = -1;
        for (final CardController holder : mCardViewHolders) {
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
                    holders[pos] = new CardController(mPageContentLayout, this);
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

    protected abstract void updateVisibleCard(@Nullable final CardController old);

    protected abstract void hideHiddenCardsThatArentActive();
}
