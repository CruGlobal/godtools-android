package org.cru.godtools.tract.model;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v7.widget.CardView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.cru.godtools.base.model.Event;
import org.cru.godtools.tract.R;
import org.cru.godtools.tract.R2;
import org.cru.godtools.tract.viewmodel.ResourceViewUtils;
import org.cru.godtools.tract.widget.TractPicassoImageView;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.Optional;

@UiThread
public final class CardViewHolder extends ParentViewHolder<Card> {
    public interface Callbacks {
        void onToggleCard(@NonNull CardViewHolder holder);

        void onDismissCard(@NonNull CardViewHolder holder);
    }

    @BindView(R2.id.background_image)
    TractPicassoImageView mBackgroundView;
    @BindView(R2.id.card)
    CardView mCardView;
    @BindView(R2.id.label)
    TextView mLabel;
    @BindView(R2.id.label_divider)
    View mDivider;

    @Nullable
    private List<Runnable> mPendingAnalyticsEvents;
    @Nullable
    private Callbacks mCallbacks;

    CardViewHolder(@NonNull final ViewGroup parent, @Nullable final PageViewHolder pageViewHolder) {
        super(Card.class, parent, R.layout.tract_content_card, pageViewHolder);
        if (pageViewHolder != null) {
            setCallbacks(pageViewHolder);
        }
    }

    @NonNull
    public static CardViewHolder create(@NonNull final ViewGroup parent,
                                        @Nullable final PageViewHolder pageViewHolder) {
        return new CardViewHolder(parent, pageViewHolder);
    }

    // region Lifecycle Events

    @Override
    @CallSuper
    void onBind() {
        super.onBind();
        bindBackground();
        bindLabel();
    }

    @Override
    void onVisible() {
        super.onVisible();
        if (mModel != null) {
            mPendingAnalyticsEvents =
                    triggerAnalyticsEvents(mModel.mAnalyticsEvents, AnalyticsEvent.Trigger.VISIBLE, AnalyticsEvent.Trigger.DEFAULT);
        }
    }

    public void onContentEvent(@NonNull final Event event) {
        checkForDismissEvent(event);
    }

    @Override
    void onHidden() {
        super.onHidden();
        if (mPendingAnalyticsEvents != null) {
            cancelPendingAnalyticsEvents(mPendingAnalyticsEvents);
        }
    }

    // endregion Lifecycle Events

    public void setCallbacks(@Nullable final Callbacks callbacks) {
        mCallbacks = callbacks;
    }

    private void bindBackground() {
        mCardView.setCardBackgroundColor(Card.getBackgroundColor(mModel));
        ResourceViewUtils.bindBackgroundImage(mBackgroundView, Card.getBackgroundImageResource(mModel),
                                              Card.getBackgroundImageScaleType(mModel),
                                              Card.getBackgroundImageGravity(mModel));
    }

    private void bindLabel() {
        final Text label = mModel != null ? mModel.getLabel() : null;
        Text.bind(label, mLabel, R.dimen.text_size_card_label, Styles.getPrimaryColor(mModel));
        mDivider.setBackgroundColor(Styles.getTextColor(mModel));
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
}
