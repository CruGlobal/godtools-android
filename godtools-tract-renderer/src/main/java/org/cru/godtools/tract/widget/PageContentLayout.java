package org.cru.godtools.tract.widget;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.os.ParcelableCompat;
import android.support.v4.os.ParcelableCompatCreatorCallbacks;
import android.support.v4.view.AbsSavedState;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

import org.ccci.gto.android.common.animation.SimpleAnimatorListener;
import org.cru.godtools.tract.R;
import org.cru.godtools.tract.util.ViewUtils;

import java.util.ArrayList;
import java.util.List;

import static android.widget.FrameLayout.LayoutParams.UNSPECIFIED_GRAVITY;
import static org.ccci.gto.android.common.base.Constants.INVALID_ID_RES;
import static org.cru.godtools.tract.widget.PageContentLayout.LayoutParams.CHILD_TYPE_CALL_TO_ACTION;
import static org.cru.godtools.tract.widget.PageContentLayout.LayoutParams.CHILD_TYPE_CARD;
import static org.cru.godtools.tract.widget.PageContentLayout.LayoutParams.CHILD_TYPE_HERO;

public class PageContentLayout extends FrameLayout implements NestedScrollingParent,
        ViewTreeObserver.OnGlobalLayoutListener {
    private final int FLING_SCALE_FACTOR = 10;

    public interface OnActiveCardListener {
        void onActiveCardChanged(@Nullable View activeCard);
    }

    private final GestureDetectorCompat mGestureDetector;
    private final GestureDetector.OnGestureListener mGestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onFling(final MotionEvent e1, final MotionEvent e2, final float velocityX,
                               final float velocityY) {
            return flingCard(velocityY);
        }
    };
    private final NestedScrollingParentHelper mParentHelper;

    @Nullable
    private OnActiveCardListener mActiveCardListener;

    private int mCardPositionOffset = 2;
    @Nullable
    private View mActiveCard;
    private int mActiveCardPosition = 0;

    @Nullable
    Animator mAnimation;
    private final Animator.AnimatorListener mAnimationListener = new SimpleAnimatorListener() {
        @Override
        public void onAnimationEnd(final Animator animation) {
            if (mAnimation == animation) {
                mAnimation = null;
                updateChildrenOffsetsAndAlpha();
                dispatchActiveCardChanged();
            }
        }
    };

    public PageContentLayout(@NonNull final Context context) {
        this(context, null);
    }

    public PageContentLayout(@NonNull final Context context, @Nullable final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PageContentLayout(@NonNull final Context context, @Nullable final AttributeSet attrs,
                             final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mGestureDetector = new GestureDetectorCompat(context, mGestureListener);
        mParentHelper = new NestedScrollingParentHelper(this);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PageContentLayout(@NonNull final Context context, @Nullable final AttributeSet attrs,
                             final int defStyleAttr, final int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mGestureDetector = new GestureDetectorCompat(context, mGestureListener);
        mParentHelper = new NestedScrollingParentHelper(this);
    }

    /* BEGIN lifecycle */

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    @Override
    protected void onRestoreInstanceState(final Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        final SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());

        changeActiveCard(ss.activeCardPosition, false);
    }

    @Override
    public void onGlobalLayout() {
        // XXX: This is to fix a chicken and egg bug. When measuring views we want to have access to card offsets.
        // XXX: To get card offsets we need to access the actual layout of nodes. to get the actual layout of nodes we
        // XXX: need to measure the views first.
        // XXX: So, to work around this problem we only calculate offsets after a view has been laid out at least once,
        // XXX: and double check our offsets after any layout pass.
        boolean changed = false;
        for (int i = 0; i < getChildCount(); i++) {
            changed = calculateCardOffsets(getChildAt(i)) || changed;
        }
        if (changed) {
            invalidate();
            requestLayout();
        }
    }

    @Override
    public void onViewAdded(final View child) {
        super.onViewAdded(child);
        updateActiveCardPosition(false);
        updateChildrenOffsetsAndAlpha();
    }

    @Override
    public void onViewRemoved(final View child) {
        super.onViewRemoved(child);
        if (mActiveCard != child) {
            updateActiveCardPosition(false);
            updateChildrenOffsetsAndAlpha();
        } else {
            changeActiveCard(getChildAt(mActiveCardPosition + mCardPositionOffset - 1), false);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(final MotionEvent ev) {
        return mGestureDetector.onTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(@NonNull final MotionEvent event) {
        if (mGestureDetector.onTouchEvent(event)) {
            return true;
        } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
            // we always consume the down event if it reaches us so that we can continue to process future events
            return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final SavedState state = new SavedState(super.onSaveInstanceState());
        state.activeCardPosition = mActiveCardPosition;
        return state;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getViewTreeObserver().removeGlobalOnLayoutListener(this);
    }

    /* END lifecycle */

    /* BEGIN NestedScrollingParent methods */

    @Override
    public boolean onStartNestedScroll(final View child, final View target, final int nestedScrollAxes) {
        // we return true so that we will get the onNestedFling calls from descendant NestedScrollingChild
        return true;
    }

    @Override
    public void onNestedScrollAccepted(final View child, final View target, final int axes) {
        mParentHelper.onNestedScrollAccepted(child, target, axes);
    }

    @Override
    public void onNestedPreScroll(final View target, final int dx, final int dy, final int[] consumed) {}

    @Override
    public void onNestedScroll(final View target, final int dxConsumed, final int dyConsumed, final int dxUnconsumed,
                               final int dyUnconsumed) {}

    @Override
    public void onStopNestedScroll(final View child) {
        mParentHelper.onStopNestedScroll(child);
    }

    @Override
    public boolean onNestedPreFling(final View target, final float velocityX, final float velocityY) {
        return false;
    }

    @Override
    public boolean onNestedFling(final View target, final float velocityX, final float velocityY,
                                 final boolean consumed) {
        return flingCard(velocityY * -1);
    }

    @Override
    public int getNestedScrollAxes() {
        return mParentHelper.getNestedScrollAxes();
    }

    /* END NestedScrollingParent methods */

    boolean flingCard(final float velocityY) {
        final int minVelocity =
                ViewConfiguration.get(getContext()).getScaledMinimumFlingVelocity() * FLING_SCALE_FACTOR;
        if (velocityY >= minVelocity && mActiveCardPosition >= 0) {
            changeActiveCard(mActiveCardPosition - 1, true);
            return true;
        }
        if (velocityY <= 0 - minVelocity && mCardPositionOffset + mActiveCardPosition < getChildCount() - 1) {
            changeActiveCard(mActiveCardPosition + 1, true);
            return true;
        }

        return false;
    }

    public void setActiveCardListener(@Nullable final OnActiveCardListener listener) {
        mActiveCardListener = listener;
    }

    void dispatchActiveCardChanged() {
        // only dispatch change active card callback if we aren't animating
        if (mActiveCardListener != null && mAnimation == null) {
            mActiveCardListener.onActiveCardChanged(mActiveCard);
        }
    }

    public void changeActiveCard(final int cardPosition, final boolean animate) {
        changeActiveCard(getChildAt(mCardPositionOffset + cardPosition), animate);
    }

    public void changeActiveCard(@Nullable final View view, final boolean animate) {
        if (view != null && view.getParent() != this) {
            throw new IllegalArgumentException("can't change the active view to a view that isn't a child");
        }

        // update the active card
        final View oldActiveCard = mActiveCard;
        mActiveCard = view;
        if (mActiveCard != null) {
            final LayoutParams lp = (LayoutParams) mActiveCard.getLayoutParams();
            if (lp.childType != CHILD_TYPE_CARD) {
                mActiveCard = null;
            }
        }

        if (oldActiveCard != mActiveCard) {
            updateActiveCardPosition(false);

            if (animate) {
                final Animator oldAnimation = mAnimation;
                mAnimation = buildAnimation();
                if (oldAnimation != null) {
                    oldAnimation.cancel();
                }
                mAnimation.start();
            }

            updateChildrenOffsetsAndAlpha();
            dispatchActiveCardChanged();
        } else {
            updateActiveCardPosition(true);
        }
    }

    private void updateActiveCardPosition(final boolean updateOffsets) {
        final int oldPosition = mActiveCardPosition;
        mActiveCardPosition = indexOfChild(mActiveCard) - mCardPositionOffset;
        if (mActiveCardPosition < 0) {
            mActiveCard = null;
            mActiveCardPosition = -1;
        }

        if (updateOffsets && oldPosition != mActiveCardPosition) {
            updateChildrenOffsetsAndAlpha();
            dispatchActiveCardChanged();
        }
    }

    @Nullable
    public View getActiveCard() {
        return mActiveCard;
    }

    public int getActiveCardPosition() {
        return mActiveCardPosition;
    }

    @NonNull
    private Animator buildAnimation() {
        // build individual animations
        final List<Animator> offset = new ArrayList<>();
        final List<Animator> fadeIn = new ArrayList<>();
        final List<Animator> fadeOut = new ArrayList<>();
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            final LayoutParams lp = (LayoutParams) child.getLayoutParams();
            switch (lp.childType) {
                case CHILD_TYPE_HERO:
                case CHILD_TYPE_CARD:
                    // position offset animation only
                    final int targetY = getChildTargetY(i);
                    if (child.getY() != targetY) {
                        offset.add(ObjectAnimator.ofFloat(child, View.Y, getChildTargetY(i)));
                    }
                    break;
                case CHILD_TYPE_CALL_TO_ACTION:
                    // alpha animation only
                    final float targetAlpha = getChildTargetAlpha(child);
                    if (child.getAlpha() != targetAlpha) {
                        final Animator animation = ObjectAnimator.ofFloat(child, View.ALPHA, targetAlpha);
                        if (targetAlpha > 0) {
                            fadeIn.add(animation);
                        } else {
                            fadeOut.add(animation);
                        }
                    }
                    break;
            }
        }

        // build final animation
        final AnimatorSet animation = new AnimatorSet();

        // play each group together
        animation.playTogether(fadeIn);
        animation.playTogether(offset);
        animation.playTogether(fadeOut);

        // chain groups in proper sequence
        AnimatorSet.Builder builder = null;
        if (!fadeOut.isEmpty()) {
            builder = animation.play(fadeOut.get(0));
        }
        if (!offset.isEmpty()) {
            final Animator first = offset.get(0);
            if (builder != null) {
                builder.before(first);
            } else {
                builder = animation.play(first);
            }
        }
        if (!fadeIn.isEmpty()) {
            if (builder != null) {
                builder.before(fadeIn.get(0));
            }
        }

        // set a few overall animation parameters
        animation.setInterpolator(new DecelerateInterpolator());
        animation.addListener(mAnimationListener);
        return animation;
    }

    @Override
    protected boolean checkLayoutParams(final ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    @Override
    public LayoutParams generateLayoutParams(final AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    @Override
    protected LayoutParams generateLayoutParams(final ViewGroup.LayoutParams p) {
        if (p instanceof LayoutParams) {
            return new LayoutParams((LayoutParams) p);
        } else if (p instanceof MarginLayoutParams) {
            return new LayoutParams((MarginLayoutParams) p);
        } else if (p != null) {
            return new LayoutParams(p);
        }

        return generateDefaultLayoutParams();
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        final int count = getChildCount();

        // measure the call to action view first
        int callToActionHeight = 0;
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                final LayoutParams lp = (LayoutParams) child.getLayoutParams();
                if (lp.childType == CHILD_TYPE_CALL_TO_ACTION) {
                    // measure and track the call to action height
                    measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
                    callToActionHeight = child.getMeasuredHeight();
                    break;
                }
            }
        }

        // track the next card, this is next and not previous because we are walking children backwards
        LayoutParams nextCardLp = null;
        int cardStackHeight = 0;

        int maxHeight = 0;
        int maxWidth = 0;
        int childState = 0;

        // measure all children (we iterate backwards to calculate card stack height
        // XXX: we currently end up re-measuring the call to action view
        for (int i = count - 1; i >= 0; i--) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                final LayoutParams lp = (LayoutParams) child.getLayoutParams();

                // determine how much height is used by subsequent views
                int heightUsed = 0;
                switch (lp.childType) {
                    case CHILD_TYPE_CARD:
                        heightUsed = Math.max(callToActionHeight, nextCardLp != null ? nextCardLp.cardPeekOffset : 0);
                        break;
                    case CHILD_TYPE_HERO:
                        if (nextCardLp != null) {
                            heightUsed = cardStackHeight + nextCardLp.cardPaddingOffset;
                        } else {
                            heightUsed = callToActionHeight;
                        }
                        break;
                }

                measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, heightUsed);
                maxWidth = Math.max(maxWidth, child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin);
                maxHeight =
                        Math.max(maxHeight, child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin + heightUsed);
                childState = combineMeasuredStates(childState, child.getMeasuredState());

                // handle some specific sizing based on view type
                switch (lp.childType) {
                    case CHILD_TYPE_CARD:
                        calculateCardOffsets(child);
                        lp.siblingStackOffset = cardStackHeight;
                        cardStackHeight += lp.cardStackOffset - lp.cardPaddingOffset;
                        nextCardLp = lp;
                        break;
                }
            }
        }

        // Account for padding too
        maxWidth += getPaddingLeft() + getPaddingRight();
        maxHeight += getPaddingTop() + getPaddingBottom();

        // Check against our minimum height and width
        maxHeight = Math.max(maxHeight, getSuggestedMinimumHeight());
        maxWidth = Math.max(maxWidth, getSuggestedMinimumWidth());

        setMeasuredDimension(resolveSizeAndState(maxWidth, widthMeasureSpec, childState),
                             resolveSizeAndState(maxHeight, heightMeasureSpec,
                                                 childState << MEASURED_HEIGHT_STATE_SHIFT));
    }

    private boolean calculateCardOffsets(@NonNull final View child) {
        // only update card offsets if the child has been laid out and is a ViewGroup
        if (ViewCompat.isLaidOut(child) && child instanceof ViewGroup) {
            final LayoutParams lp = (LayoutParams) child.getLayoutParams();

            // calculate the current offsets
            final int cardPaddingOffset;
            final int cardPeekOffset;
            final int cardStackOffset;
            if (lp.childType == CHILD_TYPE_CARD) {
                final View paddingView =
                        lp.cardPaddingViewTop != INVALID_ID_RES ? child.findViewById(lp.cardPaddingViewTop) : null;
                final View peekView =
                        lp.cardPeekViewTop != INVALID_ID_RES ? child.findViewById(lp.cardPeekViewTop) : null;
                final View stackView =
                        lp.cardStackViewTop != INVALID_ID_RES ? child.findViewById(lp.cardStackViewTop) : null;

                cardPaddingOffset = paddingView != null ? ViewUtils.getTopOffset((ViewGroup) child, paddingView) : 0;
                cardPeekOffset = peekView != null ? ViewUtils.getTopOffset((ViewGroup) child, peekView) : 0;
                cardStackOffset = stackView != null ? ViewUtils.getTopOffset((ViewGroup) child, stackView) : 0;
            } else {
                cardPaddingOffset = 0;
                cardPeekOffset = 0;
                cardStackOffset = 0;
            }

            // only update values if any changed
            if (lp.cardPaddingOffset != cardPaddingOffset || lp.cardPeekOffset != cardPeekOffset ||
                    lp.cardStackOffset != cardStackOffset) {
                lp.cardPaddingOffset = cardPaddingOffset;
                lp.cardPeekOffset = cardPeekOffset;
                lp.cardStackOffset = cardStackOffset;
                return true;
            }
        }

        return false;
    }

    @Override
    protected void onLayout(final boolean changed, final int left, final int top, final int right, final int bottom) {
        // calculate the bounds we can draw in
        final int parentLeft = getPaddingLeft();
        final int parentRight = right - left - getPaddingRight();
        final int parentTop = getPaddingTop();
        final int parentBottom = bottom - top - getPaddingBottom();

        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            // only layout children that aren't gone
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                layoutFullyVisibleChild(child, parentLeft, parentTop, parentRight, parentBottom);
            }
        }

        updateChildrenOffsetsAndAlpha();
    }

    private void layoutFullyVisibleChild(final View child, final int parentLeft, final int parentTop,
                                         final int parentRight, final int parentBottom) {
        final LayoutParams lp = (LayoutParams) child.getLayoutParams();

        final int width = child.getMeasuredWidth();
        final int height = child.getMeasuredHeight();

        int childLeft = parentLeft + lp.leftMargin;
        int childTop;

        int gravity = lp.gravity;
        if (gravity == UNSPECIFIED_GRAVITY) {
            gravity = Gravity.TOP;
        }

        switch (gravity & Gravity.VERTICAL_GRAVITY_MASK) {
            case Gravity.BOTTOM:
                childTop = parentBottom - height - lp.bottomMargin;
                break;
            default:
                childTop = parentTop + lp.topMargin;
        }

        child.layout(childLeft, childTop, childLeft + width, childTop + height);
    }

    private int getChildTargetY(final int position) {
        final int parentTop = getPaddingTop();
        final int parentBottom = getMeasuredHeight() - getPaddingBottom();

        final View child = getChildAt(position);
        if (child != null) {
            final LayoutParams lp = (LayoutParams) child.getLayoutParams();
            switch (lp.childType) {
                case CHILD_TYPE_HERO:
                    // we are displaying the hero
                    if (mActiveCardPosition < 0) {
                        return child.getTop();
                    }
                    // we are displaying a card, so hide the hero
                    else {
                        return 0 - parentBottom;
                    }
                case CHILD_TYPE_CALL_TO_ACTION:
                    return child.getTop();
                case CHILD_TYPE_CARD:
                    // no cards currently active, so stack the cards
                    if (mActiveCardPosition < 0) {
                        return parentBottom - lp.cardStackOffset - lp.siblingStackOffset;
                    }

                    // this is a previous card
                    final int activePosition = mCardPositionOffset + mActiveCardPosition;
                    if (position < activePosition) {
                        return 0 - parentBottom;
                    }
                    // this is the currently displayed card
                    else if (position == activePosition) {
                        return child.getTop();
                    }
                    // this is the next card in the stack
                    else if (position - 1 == activePosition) {
                        return parentBottom - lp.cardPeekOffset;
                    }
                    // otherwise, card is off the bottom
                    else {
                        return getMeasuredHeight() - getPaddingTop();
                    }
            }
        }

        return parentTop;
    }

    private float getChildTargetAlpha(@Nullable final View child) {
        if (child != null) {
            final LayoutParams lp = (LayoutParams) child.getLayoutParams();
            if (lp.childType == CHILD_TYPE_CALL_TO_ACTION) {
                return mActiveCardPosition + mCardPositionOffset >= getChildCount() - 1 ? 1 : 0;
            }
        }
        return 1;
    }

    void updateChildrenOffsetsAndAlpha() {
        // update the child position if we aren't animating
        if (mAnimation == null) {
            int count = getChildCount();
            for (int i = 0; i < count; i++) {
                final View child = getChildAt(i);
                child.setY(getChildTargetY(i));

                final LayoutParams lp = (LayoutParams) child.getLayoutParams();
                if (lp.childType == CHILD_TYPE_CALL_TO_ACTION) {
                    child.setAlpha(getChildTargetAlpha(child));
                }
            }
        }
    }

    protected static class SavedState extends AbsSavedState {
        int activeCardPosition;

        public SavedState(Parcel in, ClassLoader loader) {
            super(in, loader);
            activeCardPosition = in.readInt();
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(activeCardPosition);
        }

        public static final Creator<SavedState> CREATOR = ParcelableCompat.newCreator(
                new ParcelableCompatCreatorCallbacks<SavedState>() {
                    @Override
                    public SavedState createFromParcel(Parcel in, ClassLoader loader) {
                        return new SavedState(in, loader);
                    }

                    @Override
                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                });
    }

    public static class LayoutParams extends FrameLayout.LayoutParams {
        public static final int CHILD_TYPE_UNKNOWN = 0;
        public static final int CHILD_TYPE_HERO = 1;
        public static final int CHILD_TYPE_CARD = 2;
        public static final int CHILD_TYPE_CALL_TO_ACTION = 3;

        public int childType = CHILD_TYPE_UNKNOWN;

        @IdRes
        public int cardPaddingViewTop = INVALID_ID_RES;
        @IdRes
        public int cardPeekViewTop = INVALID_ID_RES;
        @IdRes
        public int cardStackViewTop = INVALID_ID_RES;

        // card peek heights
        int cardPaddingOffset = 0;
        int cardStackOffset = 0;
        int cardPeekOffset = 0;
        int siblingStackOffset = 0;

        public LayoutParams(final Context c, final AttributeSet attrs) {
            super(c, attrs);
            final TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.PageContentLayout_Layout);

            childType = a.getInt(R.styleable.PageContentLayout_Layout_layout_childType, childType);

            // get the views that are used to calculate the various peek heights
            cardPaddingViewTop =
                    a.getResourceId(R.styleable.PageContentLayout_Layout_layout_card_padding_toTopOf, INVALID_ID_RES);
            cardPeekViewTop =
                    a.getResourceId(R.styleable.PageContentLayout_Layout_layout_card_peek_toTopOf, INVALID_ID_RES);
            cardStackViewTop =
                    a.getResourceId(R.styleable.PageContentLayout_Layout_layout_card_stack_toTopOf, INVALID_ID_RES);

            a.recycle();
        }

        public LayoutParams(final int width, final int height) {
            super(width, height);
        }

        public LayoutParams(@NonNull final ViewGroup.LayoutParams p) {
            super(p);
        }

        public LayoutParams(@NonNull final MarginLayoutParams source) {
            super(source);
            if (source instanceof FrameLayout.LayoutParams) {
                this.gravity = ((FrameLayout.LayoutParams) source).gravity;
            }
        }

        public LayoutParams(@NonNull final LayoutParams p) {
            this((MarginLayoutParams) p);
            this.childType = p.childType;
        }
    }
}
