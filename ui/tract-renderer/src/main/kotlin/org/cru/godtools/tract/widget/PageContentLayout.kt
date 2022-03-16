package org.cru.godtools.tract.widget

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.os.Parcelable
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewTreeObserver
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import androidx.annotation.AttrRes
import androidx.annotation.IdRes
import androidx.annotation.StyleRes
import androidx.annotation.UiThread
import androidx.core.content.withStyledAttributes
import androidx.core.view.NestedScrollingParent
import androidx.core.view.NestedScrollingParentHelper
import androidx.core.view.children
import androidx.core.view.forEach
import androidx.core.view.forEachIndexed
import com.karumi.weak.weak
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min
import kotlinx.parcelize.Parcelize
import org.ccci.gto.android.common.base.Constants.INVALID_ID_RES
import org.ccci.gto.android.common.util.view.calculateTopOffset
import org.ccci.gto.android.common.util.view.calculateTopOffsetOrNull
import org.cru.godtools.base.Settings
import org.cru.godtools.tract.R
import org.cru.godtools.tract.animation.BounceInterpolator
import org.cru.godtools.tract.widget.PageContentLayout.LayoutParams.Companion.CHILD_TYPE_CALL_TO_ACTION
import org.cru.godtools.tract.widget.PageContentLayout.LayoutParams.Companion.CHILD_TYPE_CALL_TO_ACTION_TIP
import org.cru.godtools.tract.widget.PageContentLayout.LayoutParams.Companion.CHILD_TYPE_CARD
import org.cru.godtools.tract.widget.PageContentLayout.LayoutParams.Companion.CHILD_TYPE_HERO
import org.cru.godtools.tract.widget.PageContentLayout.LayoutParams.Companion.CHILD_TYPE_UNKNOWN

private const val DEFAULT_GUTTER_SIZE = 16
private const val FLING_SCALE_FACTOR = 20

private const val BOUNCE_ANIMATION_DELAY_INITIAL = 2000L
private const val BOUNCE_ANIMATION_DELAY = 7000L
private const val BOUNCE_ANIMATION_BOUNCES = 4
private const val BOUNCE_ANIMATION_BOUNCE_DECAY = 0.5
private const val BOUNCE_ANIMATION_DURATION_FIRST_BOUNCE = 400L
private const val BOUNCE_ANIMATION_HANDLER_MSG = 1

@AndroidEntryPoint
class PageContentLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0,
    @StyleRes defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes),
    ViewTreeObserver.OnGlobalLayoutListener,
    NestedScrollingParent {
    // region Lifecycle
    override fun onRestoreInstanceState(state: Parcelable?) = when (state) {
        is SavedState -> {
            super.onRestoreInstanceState(state.superState)
            changeActiveCard(state.activeCardPosition, false)
            isBounceFirstCard = state.isBounceFirstCard
        }
        else -> super.onRestoreInstanceState(state)
    }

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

    override fun onViewAdded(child: View) {
        super.onViewAdded(child)
        if (child.childType == CHILD_TYPE_CARD) totalCards++
        updateActiveCardPosition(false)
        updateChildrenOffsetsAndAlpha()
    }

    override fun onViewRemoved(child: View) {
        super.onViewRemoved(child)
        if (child.childType == CHILD_TYPE_CARD) totalCards--
        if (child !== activeCard) {
            updateActiveCardPosition(false)
            updateChildrenOffsetsAndAlpha()
        } else {
            changeActiveCard(getChildAt(activeCardPosition + cardPositionOffset - 1), false)
        }
    }

    override fun onSaveInstanceState(): Parcelable =
        SavedState(activeCardPosition, isBounceFirstCard, super.onSaveInstanceState())

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        viewTreeObserver.removeOnGlobalLayoutListener(this)
    }
    // endregion Lifecycle

    // region Card Management
    var activeCard: View? = null
        private set
    var activeCardPosition = 0
        private set
    private var totalCards = 0

    private val cardPositionOffset = 2

    var activeCardListener: OnActiveCardListener? = null

    fun addCard(card: View, position: Int) {
        addView(card, position + cardPositionOffset)
    }

    fun changeActiveCard(cardPosition: Int, animate: Boolean = true) =
        changeActiveCard(getChildAt(cardPositionOffset + cardPosition), animate)

    @UiThread
    fun changeActiveCard(view: View?, animate: Boolean = true) {
        require(!(view != null && view.parent !== this)) { "can't change the active view to a view that isn't a child" }

        // update the active card
        val oldActiveCard = activeCard
        activeCard = view?.takeIf { it.childType == CHILD_TYPE_CARD }

        // update the position, also update offsets if the active card didn't change
        updateActiveCardPosition(updateOffsets = oldActiveCard === activeCard)

        // active card changed
        if (oldActiveCard !== activeCard) {
            if (animate) {
                // replace current animation
                buildCardChangeAnimation().apply {
                    val oldAnimation = activeAnimation
                    activeAnimation = this
                    oldAnimation?.cancel()
                    start()
                }
            } else {
                // stop any running animation
                activeAnimation?.apply {
                    activeAnimation = null
                    cancel()
                }
                updateChildrenOffsetsAndAlpha()
                dispatchActiveCardChanged()
            }
        }
    }

    private fun updateActiveCardPosition(updateOffsets: Boolean) {
        val oldPosition = activeCardPosition
        activeCardPosition = indexOfChild(activeCard) - cardPositionOffset
        if (activeCardPosition < 0) {
            activeCard = null
            activeCardPosition = -1
        }
        if (oldPosition != activeCardPosition && updateOffsets) {
            updateChildrenOffsetsAndAlpha()
            dispatchActiveCardChanged()
        }
    }

    private fun dispatchActiveCardChanged() {
        // only dispatch change active card callback if we aren't animating
        if (activeAnimation == null) activeCardListener?.onActiveCardChanged(activeCard)
    }

    interface OnActiveCardListener {
        fun onActiveCardChanged(activeCard: View?)
    }
    // endregion Card Management

    // region Card Navigation
    @Inject
    internal lateinit var settings: Settings

    private fun flingCard(velocityY: Float): Boolean {
        val minVelocity = ViewConfiguration.get(context).scaledMinimumFlingVelocity * FLING_SCALE_FACTOR
        if (velocityY >= minVelocity && activeCardPosition >= 0) {
            settings.setFeatureDiscovered(Settings.FEATURE_TRACT_CARD_SWIPED)
            changeActiveCard(activeCardPosition - 1, true)
            return true
        }
        if (velocityY <= 0 - minVelocity && cardPositionOffset + activeCardPosition < childCount - 1) {
            changeActiveCard(activeCardPosition + 1, true)
            settings.setFeatureDiscovered(Settings.FEATURE_TRACT_CARD_SWIPED)
            return true
        }
        return false
    }

    // region Touch Events
    private val gestureDetector = GestureDetector(
        context,
        object : SimpleOnGestureListener() {
            override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float) = when {
                // ignore flings when the initial event is in the gutter
                isEventInGutter(e1) -> false
                else -> flingCard(velocityY)
            }
        }
    )

    override fun onInterceptTouchEvent(ev: MotionEvent?) = gestureDetector.onTouchEvent(ev)
    override fun onTouchEvent(event: MotionEvent) = when {
        gestureDetector.onTouchEvent(event) -> true
        // we always consume the down event if it reaches us so that we can continue to process future events
        event.action == MotionEvent.ACTION_DOWN -> true
        else -> super.onTouchEvent(event)
    }

    private fun isEventInGutter(event: MotionEvent) = event.y > height - gutterSize
    // endregion Touch Events

    // region NestedScrollingParent
    private val nestedScrollingParentHelper = NestedScrollingParentHelper(this)

    /**
     * @return true so that we will get the onNestedFling calls from descendant NestedScrollingChild
     */
    override fun onStartNestedScroll(child: View, target: View, nestedScrollAxes: Int) = true
    override fun onNestedScrollAccepted(child: View, target: View, axes: Int) =
        nestedScrollingParentHelper.onNestedScrollAccepted(child, target, axes)
    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray) = Unit
    override fun onNestedScroll(target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int) {}
    override fun onStopNestedScroll(child: View) = nestedScrollingParentHelper.onStopNestedScroll(child)
    override fun onNestedPreFling(target: View, velocityX: Float, velocityY: Float) = false
    override fun onNestedFling(target: View, velocityX: Float, velocityY: Float, consumed: Boolean) =
        flingCard(velocityY * -1)
    override fun getNestedScrollAxes() = nestedScrollingParentHelper.nestedScrollAxes
    // endregion NestedScrollingParent
    // endregion Card Navigation

    // region Animations
    private var activeAnimation: Animator? = null

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
    private val cardChangeAnimationListener = object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator) {
            if (activeAnimation === animation) {
                activeAnimation = null
                updateChildrenOffsetsAndAlpha()
                dispatchActiveCardChanged()
            }
        }
    }

    private fun buildCardChangeAnimation(): Animator {
        // build individual animations
        val offset = mutableListOf<Animator>()
        val fadeIn = mutableListOf<Animator>()
        val show = mutableListOf<Animator>()
        forEachIndexed { i, child ->
            when (child.childType) {
                CHILD_TYPE_HERO, CHILD_TYPE_CARD -> {
                    // position offset animation only
                    val targetY = getChildTargetY(i).toFloat()
                    if (child.y != targetY) {
                        offset.add(ObjectAnimator.ofFloat(child, Y, targetY))
                    }
                }
                CHILD_TYPE_CALL_TO_ACTION -> {
                    // alpha animation only
                    val targetAlpha = getChildTargetAlpha(child)
                    if (child.alpha != targetAlpha) {
                        val animation = ObjectAnimator.ofFloat(child, ALPHA, targetAlpha)
                        if (targetAlpha > 0) {
                            fadeIn.add(animation)
                        } else {
                            // fading out the call to action can happen at the same time as offset animations
                            offset.add(animation)
                        }
                    }
                }
                CHILD_TYPE_CALL_TO_ACTION_TIP -> {
                    // alpha animation only
                    val targetAlpha = getChildTargetAlpha(child)
                    if (child.alpha != targetAlpha) {
                        val animation = ObjectAnimator.ofFloat(child, ALPHA, targetAlpha)
                        animation.duration = 0
                        if (targetAlpha > 0) {
                            show.add(animation)
                        } else {
                            // hiding the call to action tip can happen at the same time as other animations
                            offset.add(animation)
                        }
                    }
                }
            }
        }

        // build final animation
        return AnimatorSet().apply {
            // play each group together
            playTogether(fadeIn)
            playTogether(offset)
            playTogether(show)

            // chain groups in proper sequence
            if (offset.isNotEmpty() && fadeIn.isNotEmpty()) play(fadeIn.first()).after(offset.first())
            if (fadeIn.isNotEmpty() && show.isNotEmpty()) play(show.first()).after(fadeIn.first())

            // set a few overall animation parameters
            interpolator = DecelerateInterpolator()
            addListener(cardChangeAnimationListener)
        }
    }
    // endregion Card Change Animation
    // endregion Animations

    // region View layout logic
    private val defaultGutterSize = (DEFAULT_GUTTER_SIZE * resources.displayMetrics.density).toInt()
    private var gutterSize = 0

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
    private fun updateChildrenOffsetsAndAlpha() {
        // don't update positions if we are currently animating something
        if (activeAnimation != null) return

        forEachIndexed { i, child ->
            child.y = getChildTargetY(i).toFloat()
            when (child.childType) {
                CHILD_TYPE_CALL_TO_ACTION, CHILD_TYPE_CALL_TO_ACTION_TIP -> child.alpha = getChildTargetAlpha(child)
            }
        }
    }

    private val View.childType get() = (layoutParams as? LayoutParams)?.childType ?: CHILD_TYPE_UNKNOWN

    private fun getChildTargetY(position: Int): Int {
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

    private fun getChildTargetAlpha(child: View) = when (child.childType) {
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

        internal var childType = CHILD_TYPE_UNKNOWN

        @IdRes
        internal var cardPaddingViewTop = INVALID_ID_RES
        @IdRes
        internal var cardPeekViewTop = INVALID_ID_RES
        @IdRes
        internal var cardStackViewTop = INVALID_ID_RES

        @IdRes
        internal var above = INVALID_ID_RES

        // card peek heights
        internal var cardPaddingOffset = 0
        internal var cardStackOffset = 0
        internal var cardPeekOffset = 0
        internal var siblingStackOffset = 0

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
    private class SavedState(
        val activeCardPosition: Int,
        val isBounceFirstCard: Boolean,
        val superState: Parcelable?
    ) : Parcelable
}
