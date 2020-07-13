package org.cru.godtools.tract.viewmodel;

import android.view.View;

import com.annimon.stream.Optional;

import org.cru.godtools.tract.databinding.TractPageBinding;
import org.cru.godtools.tract.ui.controller.CardController;
import org.cru.godtools.tract.widget.PageContentLayout;
import org.cru.godtools.xml.model.Card;
import org.cru.godtools.xml.model.Page;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public abstract class PageViewHolder extends BaseViewHolder<Page>
        implements PageContentLayout.OnActiveCardListener, CardController.Callbacks {
    public interface Callbacks {
        void onUpdateActiveCard(@Nullable Card card);

        void goToNextPage();
    }

    protected boolean mBindingCards = false;

    @Nullable
    protected CardController mActiveCardViewHolder;

    @Nullable
    private Callbacks mCallbacks;

    public PageViewHolder(@NonNull final TractPageBinding binding) {
        super(Page.class, binding.getRoot(), null);
    }

    // region Lifecycle Events

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

    @Override
    protected void updateLayoutDirection() {
    }

    protected abstract void updateVisibleCard(@Nullable final CardController old);

    protected abstract void hideHiddenCardsThatArentActive();
}
