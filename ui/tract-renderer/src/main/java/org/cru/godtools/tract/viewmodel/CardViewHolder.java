package org.cru.godtools.tract.viewmodel;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.cru.godtools.base.model.Event;
import org.cru.godtools.base.tool.model.view.ResourceViewUtils;
import org.cru.godtools.base.tool.model.view.TextViewUtils;
import org.cru.godtools.base.util.LocaleUtils;
import org.cru.godtools.tract.R;
import org.cru.godtools.tract.R2;
import org.cru.godtools.tract.widget.TractPicassoImageView;
import org.cru.godtools.xml.model.AnalyticsEvent.Trigger;
import org.cru.godtools.xml.model.Card;
import org.cru.godtools.xml.model.Content;
import org.cru.godtools.xml.model.Form;
import org.cru.godtools.xml.model.Styles;
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
public final class CardViewHolder extends ParentViewHolder<Card> {
    public interface Callbacks {
        void onToggleCard(@NonNull CardViewHolder holder);

        void onDismissCard(@NonNull CardViewHolder holder);

        void onNextCard();

        void onPreviousCard();
    }

    @BindView(R2.id.background_image)
    TractPicassoImageView mBackgroundView;
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
        bindCardNavigation();
    }

    @Override
    void onVisible() {
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
        ResourceViewUtils.bindBackgroundImage(Card.getBackgroundImageResource(mModel), mBackgroundView,
                Card.getBackgroundImageScaleType(mModel),
                Card.getBackgroundImageGravity(mModel));
    }

    private void bindLabel() {
        final Text label = mModel != null ? mModel.getLabel() : null;
        TextViewUtils.bind(label, mLabel, R.dimen.text_size_card_label, Styles.getPrimaryColor(mModel));
        mDivider.setBackgroundColor(Styles.getTextColor(mModel));
    }

    private void bindCardNavigation() {
        int cardPositionCount = mModel != null ? mModel.getPosition() + 1 : 1;
        Locale locale = mModel != null ? mModel.getManifest().getLocale() : Locale.getDefault();
        int cardCount = getCardCount();
        String positionText = String.format(locale, "%d/%d", cardPositionCount, cardCount);
        mCardPositionView.setText(positionText);
        if (isPrayerForm(mModel) || isPrayerSelection(mModel)) {
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

    private boolean isPrayerForm(Card card) {
        if (card != null) {
            for (Content content : card.getContent()) {
                if (content instanceof Form) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isPrayerSelection(Card card) {
        if (card != null) {
            for (Event.Id dismissListener : card.getDismissListeners()) {
                if (dismissListener.name.contains("followup-form")) {
                    return true;
                }
            }
        }
        return false;
    }

    private int getCardCount() {
        int count = 0;
        if (mModel != null) {
            for (Card card : mModel.getPage().getCards()) {
                if (isPrayerForm(card) || isPrayerSelection(card)) {
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
