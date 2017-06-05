package org.cru.godtools.tract.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import org.cru.godtools.tract.R;

import static org.cru.godtools.tract.widget.PageContentLayout.LayoutParams.CHILD_TYPE_CALL_TO_ACTION;
import static org.cru.godtools.tract.widget.PageContentLayout.LayoutParams.CHILD_TYPE_CARD;
import static org.cru.godtools.tract.widget.PageContentLayout.LayoutParams.CHILD_TYPE_HERO;

public class PageContentLayout extends LinearLayoutCompat implements NestedScrollView.OnScrollChangeListener {
    int mScrollY = 0;

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

    @Override
    public void onScrollChange(final NestedScrollView v, final int scrollX, final int scrollY, final int oldScrollX,
                               final int oldScrollY) {
        mScrollY = scrollY;
        if (updateCardOverlap()) {
            requestLayout();
        }
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
        return new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    @Override
    protected LayoutParams generateLayoutParams(final ViewGroup.LayoutParams p) {
        if (p instanceof LayoutParams) {
            return new LayoutParams((LayoutParams) p);
        } else if (p instanceof LinearLayoutCompat.LayoutParams) {
            return new LayoutParams((LinearLayoutCompat.LayoutParams) p);
        } else if (p instanceof MarginLayoutParams) {
            return new LayoutParams((MarginLayoutParams) p);
        } else if (p != null) {
            return new LayoutParams(p);
        }

        return generateDefaultLayoutParams();
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        // perform measure to determine call-to-action height (if we have a call-to-action view)
        final View callToAction = getCallToActionView();
        if (callToAction != null) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }

        // update card & hero heights
        final int parentHeight = getParentMeasuredHeight();
        final boolean cardsUpdated = updateCardHeights(parentHeight);
        final boolean heroUpdated = updateHeroHeight(parentHeight);
        final boolean cardsOverlapUpdated = updateCardOverlap();

        // perform actual measure if needed
        if (callToAction == null || cardsUpdated || heroUpdated || cardsOverlapUpdated) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    private boolean updateHeroHeight(final int parentHeight) {
        if (parentHeight <= 0) {
            return false;
        }

        // short-circuit if we don't have any children
        final int count = getChildCount();
        if (count == 0) {
            return false;
        }

        // sum up card peek heights from all cards
        int heightOffset = 0;
        boolean cardSeen = false;
        for (int i = 1; i < count; ++i) {
            final View child = getChildAt(i);
            final LayoutParams lp = (LayoutParams) child.getLayoutParams();
            if (lp.childType == CHILD_TYPE_CARD) {
                if (!cardSeen) {
                    // only adjust for padding of first card
                    heightOffset = +lp.cardPeekPadding;
                }
                heightOffset += lp.cardPeekHeight;
                cardSeen = true;
            }
        }

        // if we don't have cards just offset height based on call to action height
        if (!cardSeen) {
            heightOffset = getCallToActionMeasuredHeight();
        }

        // update the height of the hero
        final View child = getChildAt(0);
        final LayoutParams lp = (LayoutParams) child.getLayoutParams();
        if (lp.childType == CHILD_TYPE_HERO && lp.dynamicHeight) {
            final int calculatedHeight = Math.max(parentHeight - heightOffset, ViewCompat.getMinimumHeight(child));
            if (calculatedHeight != lp.height) {
                lp.height = calculatedHeight;
                return true;
            }
        }

        return false;
    }

    /**
     * @return true if any card layout was updated, false otherwise.
     */
    private boolean updateCardHeights(final int parentHeight) {
        // short-circuit if the parent has no height
        if (parentHeight <= 0) {
            return false;
        }

        // update card heights if we have any and calculate card label height
        final int callToActionHeight = getCallToActionMeasuredHeight();
        final int height = Math.max(0, parentHeight - callToActionHeight);
        final int count = getChildCount();
        boolean layoutUpdated = false;
        for (int i = 1; i < count; ++i) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                final LayoutParams lp = (LayoutParams) child.getLayoutParams();
                if (lp.childType == CHILD_TYPE_CARD) {
                    if (lp.dynamicHeight) {
                        if (lp.height != height) {
                            lp.height = height;
                            layoutUpdated = true;
                        }
                    }
                }
            }
        }

        return layoutUpdated;
    }

    private boolean updateCardOverlap() {
        final int scrollY = mScrollY;

        // calculate the initial scroll percentage
        int initialHeight = getParentMeasuredHeight();
        final int count = getChildCount();
        if (count > 0) {
            final View hero = getChildAt(0);
            final LayoutParams lp = (LayoutParams) hero.getLayoutParams();
            if (lp.childType == CHILD_TYPE_HERO) {
                initialHeight = lp.height;
            }
        }
        final float scrollPercentage =
                initialHeight > 0 ? Math.max(0f, ((float) initialHeight - (float) scrollY) / (float) initialHeight) :
                        0f;

        boolean firstChild = true;
        boolean layoutUpdated = false;
        View previous = null;
        for (int i = 0; i < count; ++i) {
            final View child = getChildAt(i);

            // skip missing children
            if (child.getVisibility() == GONE) {
                continue;
            }

            final LayoutParams lp = (LayoutParams) child.getLayoutParams();
            if (lp.childType == CHILD_TYPE_CARD) {
                // calculate the top margin
                int topMargin = 0;
                if (previous != null) {
                    final LayoutParams plp = (LayoutParams) previous.getLayoutParams();
                    topMargin = 0 - lp.height + lp.cardPeekPadding - plp.cardPeekPadding + plp.cardPeekHeight;
                    if (firstChild) {
                        topMargin *= scrollPercentage;
                        firstChild = false;
                    } else if (scrollY > initialHeight) {
                        topMargin = 0;
                    }
                }

                // update the top margin
                if (lp.topMargin != topMargin) {
                    lp.topMargin = topMargin;
                    layoutUpdated = true;
                }

                // track previous sibling
                previous = child;
            } else {
                // reset previous if this wasn't a card
                previous = null;
            }
        }

        return layoutUpdated;
    }

    private int getParentMeasuredHeight() {
        final ViewParent parent = getParent();
        return parent instanceof View ? ((View) parent).getMeasuredHeight() : 0;
    }

    @Nullable
    private View getCallToActionView() {
        // call to action view can only be the last child (if it exists)
        final int count = getChildCount();
        if (count > 1) {
            final View child = getChildAt(count - 1);
            final LayoutParams lp = (LayoutParams) child.getLayoutParams();
            if (lp.childType == CHILD_TYPE_CALL_TO_ACTION) {
                return child;
            }
        }

        return null;
    }

    private int getCallToActionMeasuredHeight() {
        final View view = getCallToActionView();
        return view != null ? view.getMeasuredHeight() : 0;
    }

    public static class LayoutParams extends LinearLayoutCompat.LayoutParams {
        public static final int CHILD_TYPE_UNKNOWN = 0;
        public static final int CHILD_TYPE_HERO = 1;
        public static final int CHILD_TYPE_CARD = 2;
        public static final int CHILD_TYPE_CALL_TO_ACTION = 3;

        public int childType = CHILD_TYPE_UNKNOWN;
        public boolean dynamicHeight = false;

        // card peek heights
        public int cardPeekPadding = 0;
        public int cardPeekHeight = 0;

        public LayoutParams(final Context c, final AttributeSet attrs) {
            super(c, attrs);
            final TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.PageContentLayout_Layout);

            childType = a.getInt(R.styleable.PageContentLayout_Layout_layout_childType, childType);
            dynamicHeight = a.getBoolean(R.styleable.PageContentLayout_Layout_layout_dynamicHeight, dynamicHeight);

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
        }

        public LayoutParams(@NonNull final LinearLayoutCompat.LayoutParams source) {
            super(source);
        }

        public LayoutParams(@NonNull final LayoutParams source) {
            super(source);
        }
    }
}
