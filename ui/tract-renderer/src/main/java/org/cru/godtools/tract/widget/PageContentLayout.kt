package org.cru.godtools.tract.widget

import android.animation.Animator
import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import androidx.annotation.AttrRes
import androidx.annotation.IdRes
import androidx.annotation.StyleRes
import androidx.core.content.withStyledAttributes
import androidx.core.view.children
import kotlinx.parcelize.Parcelize
import org.ccci.gto.android.common.base.Constants.INVALID_ID_RES
import org.ccci.gto.android.common.util.view.calculateTopOffset
import org.ccci.gto.android.common.util.view.calculateTopOffsetOrNull
import org.cru.godtools.tract.R
import org.cru.godtools.tract.animation.BounceInterpolator

private const val BOUNCE_ANIMATION_BOUNCES = 4
private const val BOUNCE_ANIMATION_BOUNCE_DECAY = 0.5

open class PageContentLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0,
    @StyleRes defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes), ViewTreeObserver.OnGlobalLayoutListener {
    // region Lifecycle
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        viewTreeObserver.addOnGlobalLayoutListener(this)
    }

    override fun onGlobalLayout() {
        // HACK: This is to fix a chicken and egg bug. When measuring views we want to have access to card offsets.
        //       To get card offsets we need to access the actual layout of nodes. to get the actual layout of nodes we
        //       need to measure the views first.
        //
        //       So, to work around this problem we only calculate offsets after a view has been laid out at least once,
        //       and double check our offsets after any layout pass.
        if (children.map { calculateCardOffsets(it) }.count { it } > 0) {
            invalidate()
            requestLayout()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        viewTreeObserver.removeOnGlobalLayoutListener(this)
    }
    // endregion Lifecycle

    // region Card Management
    @JvmField
    protected var activeCard: View? = null
    @JvmField
    protected var activeCardPosition = 0
    @JvmField
    protected var totalCards = 0

    @JvmField
    protected val cardPositionOffset = 2

    var activeCardListener: OnActiveCardListener? = null

    fun addCard(card: View, position: Int) {
        addView(card, position + cardPositionOffset)
    }

    protected fun dispatchActiveCardChanged() {
        // only dispatch change active card callback if we aren't animating
        if (activeAnimation == null) activeCardListener?.onActiveCardChanged(activeCard)
    }

    interface OnActiveCardListener {
        fun onActiveCardChanged(activeCard: View?)
    }
    // endregion Card Management

    // region Animation
    @JvmField
    protected var activeAnimation: Animator? = null

    // region Card Bounce Animation
    open var isBounceFirstCard = false
    @JvmField
    protected val bounceHeight = resources.getDimension(R.dimen.card_bounce_height)
    @JvmField
    protected val bounceInterpolator = BounceInterpolator(BOUNCE_ANIMATION_BOUNCES, BOUNCE_ANIMATION_BOUNCE_DECAY)
    // endregion Card Bounce Animation
    // endregion Animation

    // region View layout logic
    protected fun layoutFullyVisibleChild(
        child: View,
        parentLeft: Int,
        parentTop: Int,
        parentRight: Int,
        parentBottom: Int
    ) {
        val lp = child.layoutParams as LayoutParams
        val width = child.measuredWidth
        val height = child.measuredHeight
        val gravity = lp.gravity.takeUnless { it == FrameLayout.LayoutParams.UNSPECIFIED_GRAVITY } ?: Gravity.TOP
        val above = findViewById<View>(lp.above)

        val childTop = when {
            above != null -> calculateTopOffset(above) - height
            gravity and Gravity.VERTICAL_GRAVITY_MASK == Gravity.BOTTOM -> parentBottom - height - lp.bottomMargin
            else -> parentTop + lp.topMargin
        }
        val childLeft = parentLeft + lp.leftMargin

        child.layout(childLeft, childTop, childLeft + width, childTop + height)
    }

    protected fun calculateCardOffsets(child: View): Boolean {
        // only update card offsets if the child has been laid out
        if (child.isLaidOut) {
            val lp = child.layoutParams as LayoutParams

            // calculate the current offsets
            var cardPaddingOffset = 0
            var cardPeekOffset = 0
            var cardStackOffset = 0

            when (lp.childType) {
                LayoutParams.CHILD_TYPE_CARD -> {
                    if (child is ViewGroup) {
                        cardPaddingOffset = child.calculateTopOffsetOrNull(lp.cardPaddingViewTop) ?: 0
                        cardPeekOffset = child.calculateTopOffsetOrNull(lp.cardPeekViewTop) ?: 0
                        cardStackOffset = child.calculateTopOffsetOrNull(lp.cardStackViewTop) ?: 0
                    }
                }
            }

            // only update values if any changed
            if (lp.cardPaddingOffset != cardPaddingOffset ||
                lp.cardPeekOffset != cardPeekOffset ||
                lp.cardStackOffset != cardStackOffset
            ) {
                lp.cardPaddingOffset = cardPaddingOffset
                lp.cardPeekOffset = cardPeekOffset
                lp.cardStackOffset = cardStackOffset
                return true
            }
        }
        return false
    }

    // region LayoutParams
    override fun checkLayoutParams(p: ViewGroup.LayoutParams?) = p is LayoutParams
    override fun generateLayoutParams(attrs: AttributeSet?) = LayoutParams(context, attrs)
    override fun generateDefaultLayoutParams() = LayoutParams(MATCH_PARENT, MATCH_PARENT)
    override fun generateLayoutParams(p: ViewGroup.LayoutParams) = when (p) {
        is LayoutParams -> LayoutParams(p)
        is MarginLayoutParams -> LayoutParams(p)
        else -> LayoutParams(p)
    }

    class LayoutParams : FrameLayout.LayoutParams {
        companion object {
            const val CHILD_TYPE_UNKNOWN = 0
            const val CHILD_TYPE_HERO = 1
            const val CHILD_TYPE_CARD = 2
            const val CHILD_TYPE_CALL_TO_ACTION = 3
            const val CHILD_TYPE_CALL_TO_ACTION_TIP = 4
        }

        @JvmField
        var childType = CHILD_TYPE_UNKNOWN

        @IdRes
        @JvmField
        var cardPaddingViewTop = INVALID_ID_RES
        @IdRes
        @JvmField
        var cardPeekViewTop = INVALID_ID_RES
        @IdRes
        @JvmField
        var cardStackViewTop = INVALID_ID_RES

        @IdRes
        @JvmField
        var above = INVALID_ID_RES

        // card peek heights
        @JvmField
        var cardPaddingOffset = 0
        @JvmField
        var cardStackOffset = 0
        @JvmField
        var cardPeekOffset = 0
        @JvmField
        var siblingStackOffset = 0

        constructor(c: Context, attrs: AttributeSet?) : super(c, attrs) {
            c.withStyledAttributes(attrs, R.styleable.PageContentLayout_Layout) {
                childType = getInt(R.styleable.PageContentLayout_Layout_layout_childType, childType)

                // get the views that are used to calculate the various peek heights
                cardPaddingViewTop =
                    getResourceId(R.styleable.PageContentLayout_Layout_layout_card_padding_toTopOf, cardPaddingViewTop)
                cardPeekViewTop =
                    getResourceId(R.styleable.PageContentLayout_Layout_layout_card_peek_toTopOf, cardPeekViewTop)
                cardStackViewTop =
                    getResourceId(R.styleable.PageContentLayout_Layout_layout_card_stack_toTopOf, cardStackViewTop)

                above = getResourceId(R.styleable.PageContentLayout_Layout_android_layout_above, above)
            }
        }

        constructor(width: Int, height: Int) : super(width, height)
        constructor(p: ViewGroup.LayoutParams) : super(p)
        constructor(source: MarginLayoutParams) : super(source) {
            if (source is FrameLayout.LayoutParams) gravity = source.gravity
        }

        constructor(p: LayoutParams) : this(p as MarginLayoutParams) {
            childType = p.childType
        }
    }
    // endregion LayoutParams
    // endregion View layout logic

    @Parcelize
    protected class SavedState(
        val activeCardPosition: Int,
        val isBounceFirstCard: Boolean,
        val superState: Parcelable
    ) : Parcelable
}
