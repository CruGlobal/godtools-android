package org.cru.godtools.tract.viewmodel;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.cru.godtools.base.model.Event;
import org.cru.godtools.base.tool.model.view.ResourceViewUtilsKt;
import org.cru.godtools.base.tool.model.view.TextViewUtils;
import org.cru.godtools.base.tool.widget.SimpleScaledPicassoImageView;
import org.cru.godtools.base.util.LocaleUtils;
import org.cru.godtools.tract.R;
import org.cru.godtools.tract.R2;
import org.cru.godtools.tract.databinding.TractContentCardBinding;
import org.cru.godtools.tract.ui.controller.ParentController;
import org.cru.godtools.xml.model.AnalyticsEvent.Trigger;
import org.cru.godtools.xml.model.Card;
import org.cru.godtools.xml.model.CardKt;
import org.cru.godtools.xml.model.StylesKt;
import org.cru.godtools.xml.model.Text;

import java.util.List;
import java.util.Locale;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.cardview.widget.CardView;
import butterknife.BindView;
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

    @BindView(R2.id.background_image)
    SimpleScaledPicassoImageView mBackgroundView;
    @BindView(R2.id.card)
    CardView mCardView;
    @BindView(R2.id.label)
    TextView mLabel;
    @BindView(R2.id.label_divider)
    View mDivider;
    @BindView(R2.id.next_card)
    TextView mNextCardView;
    @BindView(R2.id.card_position)
    TextView mCardPositionView;
    @BindView(R2.id.previous_card)
    TextView mPreviousCardView;

    @BindView(R2.id.content)
    LinearLayout mContent;

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
        bindBackground();
        bindLabel();
        bindCardNavigation();
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
        return mContent;
    }

    public void setCallbacks(@Nullable final Callbacks callbacks) {
        mCallbacks = callbacks;
    }

    private void bindBackground() {
        mCardView.setCardBackgroundColor(CardKt.getBackgroundColor(mModel));
        ResourceViewUtilsKt.bindBackgroundImage(mBackgroundView, CardKt.getBackgroundImageResource(mModel),
                                                CardKt.getBackgroundImageScaleType(mModel),
                                                CardKt.getBackgroundImageGravity(mModel));
    }

    private void bindLabel() {
        final Text label = mModel != null ? mModel.getLabel() : null;
        TextViewUtils.bind(label, mLabel, R.dimen.text_size_card_label, StylesKt.getPrimaryColor(mModel));
        mDivider.setBackgroundColor(StylesKt.getTextColor(mModel));
    }

    private void bindCardNavigation() {
        int cardPositionCount = mModel != null ? mModel.getPosition() + 1 : 1;
        Locale locale = mModel != null ? mModel.getManifest().getLocale() : Locale.getDefault();
        int cardCount = getCardCount();
        String positionText = String.format(locale, "%d/%d", cardPositionCount, cardCount);
        mCardPositionView.setText(positionText);
        if (isCardHidden(mModel)) {
            mPreviousCardView.setVisibility(View.INVISIBLE);
            mPreviousCardView.setEnabled(false);
            mCardPositionView.setVisibility(View.INVISIBLE);
            mNextCardView.setVisibility(View.INVISIBLE);
            mNextCardView.setEnabled(false);
            return;
        }
        Context localContext = LocaleUtils.localizeContextIfPossible(mCardView.getContext(), locale);
        if (cardPositionCount == 1) {
            mPreviousCardView.setVisibility(View.INVISIBLE);
            mPreviousCardView.setEnabled(false);
        } else {
            mPreviousCardView.setVisibility(View.VISIBLE);
            mPreviousCardView.setEnabled(true);
            mPreviousCardView.setText(localContext.getString(R.string.tract_card_previous));
        }

        if (cardPositionCount == cardCount) {
            mNextCardView.setVisibility(View.INVISIBLE);
            mNextCardView.setEnabled(false);
        } else {
            mNextCardView.setVisibility(View.VISIBLE);
            mNextCardView.setEnabled(true);
            mNextCardView.setText(localContext.getString(R.string.tract_card_next));
        }

    }

    private boolean isCardHidden(Card card) {
        if (card != null) {
            return card.isHidden();
        }
        return false;
    }

    private int getCardCount() {
        int count = 0;
        if (mModel != null) {
            for (Card card : mModel.getPage().getCards()) {
                if (isCardHidden(card)) {
                    continue;
                }
                count++;
            }
        }
        return count;
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
