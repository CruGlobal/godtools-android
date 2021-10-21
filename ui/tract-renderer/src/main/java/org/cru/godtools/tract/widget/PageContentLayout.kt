package org.cru.godtools.tract.widget

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.Message
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
import androidx.annotation.UiThread
import androidx.core.content.withStyledAttributes
import androidx.core.view.children
import androidx.core.view.forEach
import androidx.core.view.forEachIndexed
import com.karumi.weak.weak
import kotlin.math.max
import kotlin.math.min
import kotlinx.parcelize.Parcelize
import org.ccci.gto.android.common.base.Constants.INVALID_ID_RES
import org.ccci.gto.android.common.util.view.calculateTopOffset
import org.ccci.gto.android.common.util.view.calculateTopOffsetOrNull
import org.cru.godtools.tract.R
import org.cru.godtools.tract.animation.BounceInterpolator
import org.cru.godtools.tract.widget.PageContentLayout.LayoutParams.Companion.CHILD_TYPE_CALL_TO_ACTION
import org.cru.godtools.tract.widget.PageContentLayout.LayoutParams.Companion.CHILD_TYPE_CALL_TO_ACTION_TIP
import org.cru.godtools.tract.widget.PageContentLayout.LayoutParams.Companion.CHILD_TYPE_CARD
import org.cru.godtools.tract.widget.PageContentLayout.LayoutParams.Companion.CHILD_TYPE_HERO
import org.cru.godtools.tract.widget.PageContentLayout.LayoutParams.Companion.CHILD_TYPE_UNKNOWN

private const val DEFAULT_GUTTER_SIZE = 16

private const val BOUNCE_ANIMATION_DELAY_INITIAL = 2000L
private const val BOUNCE_ANIMATION_DELAY = 7000L
private const val BOUNCE_ANIMATION_BOUNCES = 4
private const val BOUNCE_ANIMATION_BOUNCE_DECAY = 0.5
private const val BOUNCE_ANIMATION_DURATION_FIRST_BOUNCE = 400L
private const val BOUNCE_ANIMATION_HANDLER_MSG = 1

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

    // region Animations
    @JvmField
    protected var activeAnimation: Animator? = null

    // region Card Bounce Animation
    private val bounceHeight = resources.getDimension(R.dimen.card_bounce_height)
    private val bounceInterpolator = BounceInterpolator(BOUNCE_ANIMATION_BOUNCES, BOUNCE_ANIMATION_BOUNCE_DECAY)
    private val bounceAnimationListener = object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator) {
            if (activeAnimation === animation) {
                activeAnimation = null
                updateChildrenOffsetsAndAlpha()
            }
        }
    }
    private val cardBounceHandler = CardBounceHandler(this)

    var isBounceFirstCard = false
        set(animate) {
            field = animate
            if (animate) {
                cardBounceHandler.enqueueBounce(BOUNCE_ANIMATION_DELAY_INITIAL)
            } else {
                cardBounceHandler.cancelBounce()
            }
        }

    @UiThread
    private fun triggerCardBounceAnimation() {
        if (activeCard != null) return
        if (activeAnimation != null) return

        // animate the first card
        children.firstOrNull { it.childType == CHILD_TYPE_CARD }?.let {
            with(buildCardBounceAnimation(it)) {
                activeAnimation = this
                start()
            }
        }
    }

    @UiThread
    private fun buildCardBounceAnimation(view: View) = ObjectAnimator.ofFloat(view, Y, view.y - bounceHeight).apply {
        interpolator = bounceInterpolator
        duration = bounceInterpolator.getTotalDuration(BOUNCE_ANIMATION_DURATION_FIRST_BOUNCE)
        addListener(bounceAnimationListener)
    }

    private class CardBounceHandler(layout: PageContentLayout) : Handler(Looper.getMainLooper()) {
        private val layout by weak(layout)

        fun enqueueBounce(delay: Long) {
            if (!hasMessages(BOUNCE_ANIMATION_HANDLER_MSG)) sendEmptyMessageDelayed(BOUNCE_ANIMATION_HANDLER_MSG, delay)
        }

        fun cancelBounce() {
            removeMessages(BOUNCE_ANIMATION_HANDLER_MSG)
        }

        @UiThread
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                BOUNCE_ANIMATION_HANDLER_MSG -> layout?.let {
                    if (it.isBounceFirstCard) {
                        it.triggerCardBounceAnimation()
                        enqueueBounce(BOUNCE_ANIMATION_DELAY)
                    }
                }
            }
        }
    }
    // endregion Card Bounce Animation

    // region Card Change Animation
    @JvmField
    protected val cardChangeAnimationListener = object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator) {
            if (activeAnimation === animation) {
                activeAnimation = null
                updateChildrenOffsetsAndAlpha()
                dispatchActiveCardChanged()
            }
        }
    }
    // endregion Card Change Animation
    // endregion Animations

    // region View layout logic
    private val defaultGutterSize = (DEFAULT_GUTTER_SIZE * resources.displayMetrics.density).toInt()
    @JvmField
    protected var gutterSize = 0

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // measure the call to action view first
        val callToActionHeight = measureCallToActionHeight(widthMeasureSpec, heightMeasureSpec)

        // track the next card, this is next and not previous because we are walking children backwards
        var nextCardLp: LayoutParams? = null
        var cardStackHeight = 0
        var maxHeight = 0
        var maxWidth = 0
        var childState = 0

        // measure all children (we iterate backwards to calculate card stack height)
        // XXX: we currently end up re-measuring the call to action view
        for (i in childCount - 1 downTo 0) {
            val child = getChildAt(i)
            if (child.visibility == GONE) continue
            val lp = child.layoutParams as LayoutParams

            // determine how much height is used by subsequent views
            val heightUsed = when (lp.childType) {
                CHILD_TYPE_CARD -> max(nextCardLp?.cardPeekOffset ?: 0, callToActionHeight)
                CHILD_TYPE_HERO -> when (nextCardLp) {
                    null -> callToActionHeight
                    else -> cardStackHeight + nextCardLp.cardPaddingOffset
                }
                else -> 0
            }

            measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, heightUsed)
            maxWidth = max(child.measuredWidth + lp.leftMargin + lp.rightMargin, maxWidth)
            maxHeight = max(child.measuredHeight + lp.topMargin + lp.bottomMargin + heightUsed, maxHeight)
            childState = combineMeasuredStates(childState, child.measuredState)

            // handle some specific sizing based on view type
            when (lp.childType) {
                CHILD_TYPE_CARD -> {
                    calculateCardOffsets(child)
                    lp.siblingStackOffset = cardStackHeight
                    cardStackHeight += lp.cardStackOffset - lp.cardPaddingOffset
                    nextCardLp = lp
                }
            }
        }

        // Include padding in maxWidth & maxHeight
        maxWidth += paddingLeft + paddingRight
        maxHeight += paddingTop + paddingBottom

        // Check against our minimum height and width
        maxHeight = maxHeight.coerceAtLeast(suggestedMinimumHeight)
        maxWidth = maxWidth.coerceAtLeast(suggestedMinimumWidth)
        setMeasuredDimension(
            resolveSizeAndState(maxWidth, widthMeasureSpec, childState),
            resolveSizeAndState(maxHeight, heightMeasureSpec, childState shl MEASURED_HEIGHT_STATE_SHIFT)
        )

        gutterSize = min(defaultGutterSize, measuredHeight / 10)
    }

    private fun measureCallToActionHeight(widthMeasureSpec: Int, heightMeasureSpec: Int): Int {
        forEach {
            if (it.visibility != GONE && it.childType == CHILD_TYPE_CALL_TO_ACTION) {
                measureChildWithMargins(it, widthMeasureSpec, 0, heightMeasureSpec, 0)
                return it.measuredHeight
            }
        }
        return 0
    }

    private fun calculateCardOffsets(child: View): Boolean {
        // only update card offsets if the child has been laid out
        if (child.isLaidOut) {
            val lp = child.layoutParams as LayoutParams

            // calculate the current offsets
            var cardPaddingOffset = 0
            var cardPeekOffset = 0
            var cardStackOffset = 0

            when (lp.childType) {
                CHILD_TYPE_CARD -> {
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

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        val parentLeft = paddingLeft
        val parentRight = right - left - paddingRight
        val parentTop = paddingTop
        val parentBottom = bottom - top - paddingBottom

        forEach {
            if (it.visibility != GONE) layoutFullyVisibleChild(it, parentLeft, parentTop, parentRight, parentBottom)
        }

        updateChildrenOffsetsAndAlpha()
    }

    private fun layoutFullyVisibleChild(
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

    @UiThread
    protected fun updateChildrenOffsetsAndAlpha() {
        // don't update positions if we are currently animating something
        if (activeAnimation != null) return

        forEachIndexed { i, child ->
            child.y = getChildTargetY(i).toFloat()
            when (child.childType) {
                CHILD_TYPE_CALL_TO_ACTION, CHILD_TYPE_CALL_TO_ACTION_TIP -> child.alpha = getChildTargetAlpha(child)
            }
        }
    }

    protected val View.childType get() = (layoutParams as? LayoutParams)?.childType ?: CHILD_TYPE_UNKNOWN

    protected fun getChildTargetY(position: Int): Int {
        val child = getChildAt(position) ?: return paddingTop
        val lp = child.layoutParams as LayoutParams
        val parentBottom = measuredHeight - paddingBottom

        return when (lp.childType) {
            CHILD_TYPE_HERO -> when {
                // we are currently displaying the hero
                activeCardPosition < 0 -> child.top
                else -> 0 - parentBottom
            }
            CHILD_TYPE_CARD -> {
                // no cards currently active, so stack the cards
                if (activeCardPosition < 0) {
                    return parentBottom - lp.cardStackOffset - lp.siblingStackOffset
                }

                // this is a previous card
                val activePosition = cardPositionOffset + activeCardPosition
                when {
                    position < activePosition -> 0 - parentBottom
                    position == activePosition -> child.top
                    position - 1 == activePosition -> parentBottom - lp.cardPeekOffset
                    else -> measuredHeight - paddingTop
                }
            }
            CHILD_TYPE_CALL_TO_ACTION, CHILD_TYPE_CALL_TO_ACTION_TIP -> child.top
            else -> child.top
        }
    }

    protected fun getChildTargetAlpha(child: View) = when (child.childType) {
        CHILD_TYPE_CALL_TO_ACTION, CHILD_TYPE_CALL_TO_ACTION_TIP -> when {
            activeCardPosition + 1 >= totalCards -> 1f
            else -> 0f
        }
        else -> 1f
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
