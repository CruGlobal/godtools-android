package org.cru.godtools.tract.viewmodel;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.cru.godtools.base.model.Event;
import org.cru.godtools.tract.R2;
import org.cru.godtools.tract.databinding.TractContentCardBinding;
import org.cru.godtools.tract.ui.controller.ParentController;
import org.cru.godtools.xml.model.AnalyticsEvent.Trigger;
import org.cru.godtools.xml.model.Card;

import java.util.List;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import butterknife.OnClick;
import butterknife.Optional;

@UiThread
public final class CardViewHolder extends ParentController<Card> {
    public interface Callbacks {
        void onToggleCard(@NonNull CardViewHolder holder);

        void onDismissCard(@NonNull CardViewHolder holder);

        void onNextCard();

        void onPreviousCard();
    }

    @NonNull
    private final TractContentCardBinding mBinding;

    @Nullable
    private List<Runnable> mPendingAnalyticsEvents;
    @Nullable
    private Callbacks mCallbacks;

    private CardViewHolder(@NonNull final TractContentCardBinding binding,
                           @Nullable final PageViewHolder pageViewHolder) {
        super(Card.class, binding.getRoot(), pageViewHolder);
        mBinding = binding;
        if (pageViewHolder != null) {
            setCallbacks(pageViewHolder);
        }
    }

    @NonNull
    public static CardViewHolder create(@NonNull final ViewGroup parent,
                                        @Nullable final PageViewHolder pageViewHolder) {
        return new CardViewHolder(
                TractContentCardBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false),
                pageViewHolder);
    }

    // region Lifecycle Events

    @Override
    @CallSuper
    protected void onBind() {
        super.onBind();
        mBinding.setModel(getModel());
    }

    @Override
    protected void onVisible() {
        super.onVisible();
        if (mModel != null) {
            mPendingAnalyticsEvents =
                    triggerAnalyticsEvents(mModel.getAnalyticsEvents(), Trigger.VISIBLE, Trigger.DEFAULT);
        }
    }

    @Override
    @CallSuper
    public void onContentEvent(@NonNull final Event event) {
        super.onContentEvent(event);
        checkForDismissEvent(event);
    }

    @Override
    protected void onHidden() {
        super.onHidden();
        if (mPendingAnalyticsEvents != null) {
            cancelPendingAnalyticsEvents(mPendingAnalyticsEvents);
        }
    }

    // endregion Lifecycle Events

    @NonNull
    @Override
    protected LinearLayout getContentContainer() {
        return mBinding.content;
    }

    public void setCallbacks(@Nullable final Callbacks callbacks) {
        mCallbacks = callbacks;
    }

    private void checkForDismissEvent(@NonNull final Event event) {
        if (mModel != null) {
            // check for card dismiss event
            if (mModel.getDismissListeners().contains(event.id)) {
                dismissCard();
            }
        }
    }

    private void dismissCard() {
        if (mCallbacks != null) {
            mCallbacks.onDismissCard(this);
        }
    }

    @Optional
    @OnClick(R2.id.action_toggle)
    void toggleCard() {
        if (mCallbacks != null) {
            mCallbacks.onToggleCard(this);
        }
    }

    @Optional
    @OnClick(R2.id.next_card)
    void nextCard() {
        if (mCallbacks != null) {
            mCallbacks.onNextCard();
        }
    }

    @Optional
    @OnClick(R2.id.previous_card)
    void previousCard() {
        if (mCallbacks != null) {
            mCallbacks.onPreviousCard();
        }
    }
}
