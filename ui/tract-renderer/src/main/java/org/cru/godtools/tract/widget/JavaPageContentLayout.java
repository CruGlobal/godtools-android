package org.cru.godtools.tract.widget;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.DecelerateInterpolator;

import org.cru.godtools.base.Settings;

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

@AndroidEntryPoint
public class JavaPageContentLayout extends PageContentLayout implements NestedScrollingParent  {
    private static final int FLING_SCALE_FACTOR = 20;

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
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public JavaPageContentLayout(@NonNull final Context context, @Nullable final AttributeSet attrs,
                                 final int defStyleAttr, final int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mGestureDetector = new GestureDetectorCompat(context, mGestureListener);
        mParentHelper = new NestedScrollingParentHelper(this);
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
        return event.getY() > getHeight() - gutterSize;
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
                        offset.add(ObjectAnimator.ofFloat(child, View.Y, targetY));
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
        animation.addListener(cardChangeAnimationListener);
        return animation;
    }
}
