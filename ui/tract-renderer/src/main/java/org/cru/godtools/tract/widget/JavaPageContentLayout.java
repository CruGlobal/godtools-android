package org.cru.godtools.tract.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

import org.cru.godtools.base.Settings;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.core.view.GestureDetectorCompat;
import androidx.core.view.NestedScrollingParent;
import androidx.core.view.NestedScrollingParentHelper;
import dagger.hilt.android.AndroidEntryPoint;

import static org.cru.godtools.tract.widget.PageContentLayout.LayoutParams.CHILD_TYPE_CALL_TO_ACTION;
import static org.cru.godtools.tract.widget.PageContentLayout.LayoutParams.CHILD_TYPE_CALL_TO_ACTION_TIP;
import static org.cru.godtools.tract.widget.PageContentLayout.LayoutParams.CHILD_TYPE_CARD;
import static org.cru.godtools.tract.widget.PageContentLayout.LayoutParams.CHILD_TYPE_HERO;
import static org.cru.godtools.tract.widget.PageContentLayout.LayoutParams.CHILD_TYPE_UNKNOWN;

@AndroidEntryPoint
public class JavaPageContentLayout extends PageContentLayout implements NestedScrollingParent  {
    private static final int DEFAULT_GUTTER_SIZE = 16;
    private static final int FLING_SCALE_FACTOR = 20;

    private static final long BOUNCE_ANIMATION_DELAY_INITIAL = 2000;
    private static final long BOUNCE_ANIMATION_DELAY = 7000;
    private static final long BOUNCE_ANIMATION_DURATION_FIRST_BOUNCE = 400;

    private static final int MSG_BOUNCE_ANIMATION = 1;

    private int mDefaultGutterSize;
    private int mGutterSize = 0;

    private final PageLayoutHandler mHandler = new PageLayoutHandler(this);
    @Inject
    Settings mSettings;
    private final GestureDetectorCompat mGestureDetector;
    private final GestureDetector.OnGestureListener mGestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onFling(final MotionEvent e1, final MotionEvent e2, final float velocityX,
                               final float velocityY) {
            // ignore flings when the initial event is in the gutter
            if (isEventInGutter(e1)) {
                return false;
            }

            return flingCard(velocityY);
        }
    };
    private final NestedScrollingParentHelper mParentHelper;

    private final Animator.AnimatorListener mAnimationListener = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(final Animator animation) {
            if (activeAnimation == animation) {
                activeAnimation = null;
                updateChildrenOffsetsAndAlpha();
                dispatchActiveCardChanged();
            }
        }
    };

    private final Animator.AnimatorListener mBounceAnimationListener = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(final Animator animation) {
            if (activeAnimation == animation) {
                activeAnimation = null;
                updateChildrenOffsetsAndAlpha();
            }
        }
    };

    // region Initialization
    public JavaPageContentLayout(@NonNull final Context context) {
        this(context, null);
    }

    public JavaPageContentLayout(@NonNull final Context context, @Nullable final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public JavaPageContentLayout(@NonNull final Context context, @Nullable final AttributeSet attrs,
                                 final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mGestureDetector = new GestureDetectorCompat(context, mGestureListener);
        mParentHelper = new NestedScrollingParentHelper(this);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public JavaPageContentLayout(@NonNull final Context context, @Nullable final AttributeSet attrs,
                                 final int defStyleAttr, final int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mGestureDetector = new GestureDetectorCompat(context, mGestureListener);
        mParentHelper = new NestedScrollingParentHelper(this);
        init();
    }

    private void init() {
        mDefaultGutterSize = (int) (DEFAULT_GUTTER_SIZE * getResources().getDisplayMetrics().density);
    }
    // endregion Initialization

    // region Lifecycle
    @Override
    protected void onRestoreInstanceState(final Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        final SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());

        changeActiveCard(ss.getActiveCardPosition(), false);
        setBounceFirstCard(ss.isBounceFirstCard());
    }

    @Override
    public void onViewAdded(final View child) {
        super.onViewAdded(child);
        if (getChildType(child) == CHILD_TYPE_CARD) {
            totalCards++;
        }
        updateActiveCardPosition(false);
        updateChildrenOffsetsAndAlpha();
    }

    @Override
    public void onViewRemoved(final View child) {
        super.onViewRemoved(child);
        if (getChildType(child) == CHILD_TYPE_CARD) {
            totalCards--;
        }
        if (activeCard != child) {
            updateActiveCardPosition(false);
            updateChildrenOffsetsAndAlpha();
        } else {
            changeActiveCard(getChildAt(activeCardPosition + cardPositionOffset - 1), false);
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        return new SavedState(activeCardPosition, isBounceFirstCard(), super.onSaveInstanceState());
    }
    // endregion Lifecycle

    // region Touch Events
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

    private boolean isEventInGutter(@NonNull final MotionEvent event) {
        return event.getY() > getHeight() - mGutterSize;
    }
    // endregion Touch Events

    // region NestedScrollingParent
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
    // endregion NestedScrollingParent

    boolean flingCard(final float velocityY) {
        final int minVelocity =
                ViewConfiguration.get(getContext()).getScaledMinimumFlingVelocity() * FLING_SCALE_FACTOR;
        if (velocityY >= minVelocity && activeCardPosition >= 0) {
            mSettings.setFeatureDiscovered(Settings.FEATURE_TRACT_CARD_SWIPED);
            changeActiveCard(activeCardPosition - 1, true);
            return true;
        }
        if (velocityY <= 0 - minVelocity && cardPositionOffset + activeCardPosition < getChildCount() - 1) {
            changeActiveCard(activeCardPosition + 1, true);
            mSettings.setFeatureDiscovered(Settings.FEATURE_TRACT_CARD_SWIPED);
            return true;
        }
        return false;
    }

    public void changeActiveCard(final int cardPosition, final boolean animate) {
        changeActiveCard(getChildAt(cardPositionOffset + cardPosition), animate);
    }

    @UiThread
    public void changeActiveCard(@Nullable final View view, final boolean animate) {
        if (view != null && view.getParent() != this) {
            throw new IllegalArgumentException("can't change the active view to a view that isn't a child");
        }

        // update the active card
        final View oldActiveCard = activeCard;
        activeCard = view;
        if (activeCard != null) {
            if (getChildType(activeCard) != CHILD_TYPE_CARD) {
                activeCard = null;
            }
        }

        if (oldActiveCard != activeCard) {
            updateActiveCardPosition(false);

            if (animate) {
                final Animator oldAnimation = activeAnimation;
                activeAnimation = buildAnimation();
                if (oldAnimation != null) {
                    oldAnimation.cancel();
                }
                activeAnimation.start();
            } else {
                // stop any running animation
                final Animator oldAnimation = activeAnimation;
                activeAnimation = null;
                if (oldAnimation != null) {
                    oldAnimation.cancel();
                }

                updateChildrenOffsetsAndAlpha();
                dispatchActiveCardChanged();
            }
        } else {
            updateActiveCardPosition(true);
        }
    }

    private void updateActiveCardPosition(final boolean updateOffsets) {
        final int oldPosition = activeCardPosition;
        activeCardPosition = indexOfChild(activeCard) - cardPositionOffset;
        if (activeCardPosition < 0) {
            activeCard = null;
            activeCardPosition = -1;
        }

        if (updateOffsets && oldPosition != activeCardPosition) {
            updateChildrenOffsetsAndAlpha();
            dispatchActiveCardChanged();
        }
    }

    @Nullable
    public View getActiveCard() {
        return activeCard;
    }

    public int getActiveCardPosition() {
        return activeCardPosition;
    }

    private int getChildType(final View view) {
        final ViewGroup.LayoutParams lp = view.getLayoutParams();
        if (lp instanceof LayoutParams) {
            return ((LayoutParams) lp).childType;
        }
        return CHILD_TYPE_UNKNOWN;
    }

    @NonNull
    @SuppressWarnings("checkstyle:AvoidNestedBlocks")
    private Animator buildAnimation() {
        // build individual animations
        final List<Animator> offset = new ArrayList<>();
        final List<Animator> fadeIn = new ArrayList<>();
        final List<Animator> show = new ArrayList<>();
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            switch (getChildType(child)) {
                case CHILD_TYPE_HERO:
                case CHILD_TYPE_CARD:
                    // position offset animation only
                    final int targetY = getChildTargetY(i);
                    if (child.getY() != targetY) {
                        offset.add(ObjectAnimator.ofFloat(child, View.Y, getChildTargetY(i)));
                    }
                    break;
                case CHILD_TYPE_CALL_TO_ACTION: {
                    // alpha animation only
                    final float targetAlpha = getChildTargetAlpha(child);
                    if (child.getAlpha() != targetAlpha) {
                        final Animator animation = ObjectAnimator.ofFloat(child, View.ALPHA, targetAlpha);
                        if (targetAlpha > 0) {
                            fadeIn.add(animation);
                        } else {
                            // fading out the call to action can happen at the same time as offset animations
                            offset.add(animation);
                        }
                    }
                    break;
                }
                case CHILD_TYPE_CALL_TO_ACTION_TIP: {
                    // alpha animation only
                    final float targetAlpha = getChildTargetAlpha(child);
                    if (child.getAlpha() != targetAlpha) {
                        final Animator animation = ObjectAnimator.ofFloat(child, View.ALPHA, targetAlpha);
                        animation.setDuration(0);
                        if (targetAlpha > 0) {
                            show.add(animation);
                        } else {
                            // hiding the call to action tip can happen at the same time as other animations
                            offset.add(animation);
                        }
                    }
                    break;
                }
            }
        }

        // build final animation
        final AnimatorSet animation = new AnimatorSet();

        // play each group together
        animation.playTogether(fadeIn);
        animation.playTogether(offset);
        animation.playTogether(show);

        // chain groups in proper sequence
        if (!offset.isEmpty() && !fadeIn.isEmpty()) {
            animation.play(fadeIn.get(0)).after(offset.get(0));
        }
        if (!fadeIn.isEmpty() && !show.isEmpty()) {
            animation.play(show.get(0)).after(fadeIn.get(0));
        }

        // set a few overall animation parameters
        animation.setInterpolator(new DecelerateInterpolator());
        animation.addListener(mAnimationListener);
        return animation;
    }

    // region Card Bounce Animation
    @UiThread
    @Override
    public void setBounceFirstCard(final boolean animate) {
        super.setBounceFirstCard(animate);
        if (animate) {
            mHandler.enqueueBounce(BOUNCE_ANIMATION_DELAY_INITIAL);
        } else {
            mHandler.cancelBounce();
        }
    }

    /**
     * This method will start a bounce Animation if the layout is in a supported state for the animation.
     */
    @UiThread
    void bounceFirstCard() {
        // short-circuit if the first card isn't being displayed
        if (activeCard != null) {
            return;
        }

        // short-circuit if another animation is running
        if (activeAnimation != null) {
            return;
        }

        // animate the first card child
        for (int i = 0; i < getChildCount(); i++) {
            final View child = getChildAt(i);
            switch (getChildType(child)) {
                case CHILD_TYPE_CARD:
                    activeAnimation = buildBounceAnimation(child);
                    activeAnimation.start();
                    return; // Stop after first Card View
            }
        }
    }

    /**
     * This method will create a new instance of the bounce animation.
     *
     * @param view the Card View
     */
    @NonNull
    @UiThread
    private Animator buildBounceAnimation(@NonNull final View view) {
        final Animator animation = ObjectAnimator.ofFloat(view, View.Y, view.getY() - bounceHeight);
        animation.setInterpolator(bounceInterpolator);
        animation.setDuration(bounceInterpolator.getTotalDuration(BOUNCE_ANIMATION_DURATION_FIRST_BOUNCE));
        animation.addListener(mBounceAnimationListener);
        return animation;
    }
    // endregion Card Bounce Animation

    // region View layout logic
    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        final int count = getChildCount();

        // measure the call to action view first
        int callToActionHeight = 0;
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                if (getChildType(child) == CHILD_TYPE_CALL_TO_ACTION) {
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

        // update Gutter Size
        final int measuredHeight = getMeasuredHeight();
        final int maxGutterSize = measuredHeight / 10;
        mGutterSize = Math.min(maxGutterSize, mDefaultGutterSize);
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

    @SuppressWarnings("checkstyle:RightCurly")
    private int getChildTargetY(final int position) {
        final int parentTop = getPaddingTop();
        final int parentBottom = getMeasuredHeight() - getPaddingBottom();

        final View child = getChildAt(position);
        if (child != null) {
            final LayoutParams lp = (LayoutParams) child.getLayoutParams();
            switch (lp.childType) {
                case CHILD_TYPE_HERO:
                    // we are displaying the hero
                    if (activeCardPosition < 0) {
                        return child.getTop();
                    }
                    // we are displaying a card, so hide the hero
                    else {
                        return 0 - parentBottom;
                    }
                case CHILD_TYPE_CARD:
                    // no cards currently active, so stack the cards
                    if (activeCardPosition < 0) {
                        return parentBottom - lp.cardStackOffset - lp.siblingStackOffset;
                    }

                    // this is a previous card
                    final int activePosition = cardPositionOffset + activeCardPosition;
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
                case CHILD_TYPE_CALL_TO_ACTION:
                case CHILD_TYPE_CALL_TO_ACTION_TIP:
                default:
                    return child.getTop();
            }
        }

        return parentTop;
    }

    private float getChildTargetAlpha(@Nullable final View child) {
        if (child != null) {
            final int childType = getChildType(child);
            if (childType == CHILD_TYPE_CALL_TO_ACTION || childType == CHILD_TYPE_CALL_TO_ACTION_TIP) {
                return activeCardPosition + 1 >= totalCards ? 1 : 0;
            }
        }
        return 1;
    }

    @UiThread
    void updateChildrenOffsetsAndAlpha() {
        // update the child position if we aren't animating
        if (activeAnimation == null) {
            int count = getChildCount();
            for (int i = 0; i < count; i++) {
                final View child = getChildAt(i);
                child.setY(getChildTargetY(i));

                final int childType = getChildType(child);
                if (getChildType(child) == CHILD_TYPE_CALL_TO_ACTION || childType == CHILD_TYPE_CALL_TO_ACTION_TIP) {
                    child.setAlpha(getChildTargetAlpha(child));
                }
            }
        }
    }
    // endregion View layout logic

    /**
     * This Handler is what is used to created the delayed post of BOUNCE_ANIMATION_RUNNABLE
     */
    private static class PageLayoutHandler extends Handler {
        private final WeakReference<JavaPageContentLayout> mPageContentLayout;

        PageLayoutHandler(@NonNull final JavaPageContentLayout layout) {
            super(Looper.getMainLooper());
            mPageContentLayout = new WeakReference<>(layout);
        }

        void enqueueBounce(final long delay) {
            if (!hasMessages(MSG_BOUNCE_ANIMATION)) {
                sendEmptyMessageDelayed(MSG_BOUNCE_ANIMATION, delay);
            }
        }

        void cancelBounce() {
            removeMessages(MSG_BOUNCE_ANIMATION);
        }

        @UiThread
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_BOUNCE_ANIMATION:
                    final JavaPageContentLayout layout = mPageContentLayout.get();
                    if (layout != null && layout.isBounceFirstCard()) {
                        layout.bounceFirstCard();
                        enqueueBounce(BOUNCE_ANIMATION_DELAY);
                    }
                    break;
            }
        }
    }
}
