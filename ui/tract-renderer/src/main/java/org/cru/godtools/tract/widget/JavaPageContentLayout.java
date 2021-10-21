package org.cru.godtools.tract.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import org.cru.godtools.base.Settings;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GestureDetectorCompat;
import androidx.core.view.NestedScrollingParent;
import androidx.core.view.NestedScrollingParentHelper;
import dagger.hilt.android.AndroidEntryPoint;

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

    public int getActiveCardPosition() {
        return activeCardPosition;
    }
}
