package org.cru.godtools.tract.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import org.cru.godtools.tract.R;
import org.cru.godtools.tract.util.ViewUtils;

import static android.widget.FrameLayout.LayoutParams.UNSPECIFIED_GRAVITY;
import static org.ccci.gto.android.common.base.Constants.INVALID_ID_RES;
import static org.cru.godtools.tract.widget.PageContentLayout.LayoutParams.CHILD_TYPE_CALL_TO_ACTION;
import static org.cru.godtools.tract.widget.PageContentLayout.LayoutParams.CHILD_TYPE_CARD;
import static org.cru.godtools.tract.widget.PageContentLayout.LayoutParams.CHILD_TYPE_HERO;

public class PageContentLayout extends FrameLayout {
    @Nullable
    private View mActiveView;
    private int mActivePosition = 0;

    public PageContentLayout(@NonNull final Context context) {
        this(context, null);
    }

    public PageContentLayout(@NonNull final Context context, @Nullable final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PageContentLayout(@NonNull final Context context, @Nullable final AttributeSet attrs,
                             final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PageContentLayout(@NonNull final Context context, @Nullable final AttributeSet attrs,
                             final int defStyleAttr, final int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    /* BEGIN lifecycle */

    @Override
    public void onViewAdded(final View child) {
        super.onViewAdded(child);
        changeActiveView(mActiveView, false);
    }

    @Override
    public void onViewRemoved(final View child) {
        super.onViewRemoved(child);
        changeActiveView(mActiveView != child ? mActiveView : getChildAt(mActivePosition - 1), false);
    }

    /* END lifecycle */

    public void addCardView(@NonNull final View view) {
        // find the insertion position
        int i;
        LOOP:
        for (i = getChildCount(); i > 0; --i) {
            switch (((LayoutParams) getChildAt(i - 1).getLayoutParams()).childType) {
                case CHILD_TYPE_HERO:
                case CHILD_TYPE_CARD:
                    break LOOP;
            }
        }
        addView(view, i);
        ((LayoutParams) view.getLayoutParams()).childType = CHILD_TYPE_CARD;
        view.requestLayout();
    }

    public void changeActivePosition(final int activeView, final boolean animate) {
        changeActiveView(getChildAt(activeView), animate);
    }

    public void changeActiveView(@Nullable final View view, final boolean animate) {
        if (view != null && view.getParent() != this) {
            throw new IllegalArgumentException("can't change the active view to a view that isn't a child");
        }

        // update the active view
        final View oldActiveView = mActiveView;
        mActiveView = view;
        if (view != null) {
            final LayoutParams lp = (LayoutParams) view.getLayoutParams();
            if (lp.childType == CHILD_TYPE_CALL_TO_ACTION) {
                mActiveView = getChildAt(indexOfChild(view) - 1);
            }
        }

        if (oldActiveView != mActiveView) {
            invalidate();
            requestLayout();
        }

        updateActivePosition();
    }

    private void updateActivePosition() {
        final int oldPosition = mActivePosition;
        mActivePosition = indexOfChild(mActiveView);
        if (mActivePosition == -1) {
            mActiveView = getChildAt(0);
            mActivePosition = 0;
        }

        if (oldPosition != mActivePosition) {
            invalidate();
            requestLayout();
        }
    }

    public int getActivePosition() {
        return mActivePosition;
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
        int count = getChildCount();
        int i = count - 1;

        // track the next card, this is next and not previous because we are walking children backwards
        LayoutParams nextCardLp = null;
        int callToActionHeight = 0;
        int cardStackHeight = 0;

        int maxHeight = 0;
        int maxWidth = 0;
        int childState = 0;

        for (; i >= 0; i--) {
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
                    case CHILD_TYPE_CALL_TO_ACTION:
                        // call-to-action can only be the last view
                        if (i == count - 1) {
                            callToActionHeight = child.getMeasuredHeight();
                        }
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

    private void calculateCardOffsets(@NonNull final View child) {
        final LayoutParams lp = (LayoutParams) child.getLayoutParams();

        if (child instanceof ViewGroup) {
            final View paddingView =
                    lp.cardPaddingViewTop != INVALID_ID_RES ? child.findViewById(lp.cardPaddingViewTop) : null;
            final View peekView = lp.cardPeekViewTop != INVALID_ID_RES ? child.findViewById(lp.cardPeekViewTop) : null;
            final View stackView =
                    lp.cardStackViewTop != INVALID_ID_RES ? child.findViewById(lp.cardStackViewTop) : null;

            lp.cardPaddingOffset = paddingView != null ? ViewUtils.getTopOffset((ViewGroup) child, paddingView) : 0;
            lp.cardPeekOffset = peekView != null ? ViewUtils.getTopOffset((ViewGroup) child, peekView) : 0;
            lp.cardStackOffset = stackView != null ? ViewUtils.getTopOffset((ViewGroup) child, stackView) : 0;
        } else {
            lp.cardPaddingOffset = 0;
            lp.cardPeekOffset = 0;
            lp.cardStackOffset = 0;
        }
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
                final LayoutParams lp = (LayoutParams) child.getLayoutParams();
                if (i < mActivePosition) {
                    layoutHiddenChild(child);
                } else if (i == mActivePosition) {
                    layoutFullyVisibleChild(child, parentLeft, parentTop, parentRight, parentBottom);
                } else if (lp.childType == CHILD_TYPE_CALL_TO_ACTION && mActivePosition >= count - 2) {
                    layoutFullyVisibleChild(child, parentLeft, parentTop, parentRight, parentBottom);
                } else if (lp.childType == CHILD_TYPE_CARD && mActivePosition == 0) {
                    layoutStackingCard(child, parentLeft, parentTop, parentRight, parentBottom);
                } else if (lp.childType == CHILD_TYPE_CARD && mActivePosition == i - 1) {
                    layoutPeekingCard(child, parentLeft, parentTop, parentRight, parentBottom);
                } else {
                    layoutHiddenChild(child);
                }
            }
        }
    }

    private void layoutHiddenChild(final View child) {
        // layout the child completely outside of our visible area
        child.layout(0 - child.getMeasuredWidth(), 0 - child.getMeasuredHeight(), 0, 0);
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

    private void layoutPeekingCard(final View child, final int parentLeft, final int parentTop,
                                   final int parentRight, final int parentBottom) {
        final LayoutParams lp = (LayoutParams) child.getLayoutParams();

        int childLeft = parentLeft + lp.leftMargin;
        int childTop = parentBottom - lp.cardPeekOffset;

        child.layout(childLeft, childTop, childLeft + child.getMeasuredWidth(), childTop + child.getMeasuredHeight());
    }

    private void layoutStackingCard(final View child, final int parentLeft, final int parentTop,
                                    final int parentRight, final int parentBottom) {
        final LayoutParams lp = (LayoutParams) child.getLayoutParams();

        int childLeft = parentLeft + lp.leftMargin;
        int childTop = parentBottom - lp.cardStackOffset - lp.siblingStackOffset;

        child.layout(childLeft, childTop, childLeft + child.getMeasuredWidth(), childTop + child.getMeasuredHeight());
    }

    public static class LayoutParams extends FrameLayout.LayoutParams {
        public static final int CHILD_TYPE_UNKNOWN = 0;
        public static final int CHILD_TYPE_HERO = 1;
        public static final int CHILD_TYPE_CARD = 2;
        public static final int CHILD_TYPE_CALL_TO_ACTION = 3;

        public int childType = CHILD_TYPE_UNKNOWN;
        public boolean dynamicHeight = false;

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
            dynamicHeight = a.getBoolean(R.styleable.PageContentLayout_Layout_layout_dynamicHeight, dynamicHeight);

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
