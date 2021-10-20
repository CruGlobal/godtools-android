package org.cru.godtools.tract.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.annotation.AttrRes
import androidx.annotation.IdRes
import androidx.annotation.StyleRes
import androidx.core.content.withStyledAttributes
import org.ccci.gto.android.common.base.Constants.INVALID_ID_RES
import org.cru.godtools.tract.R

open class PageContentLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0,
    @StyleRes defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {
    interface OnActiveCardListener {
        fun onActiveCardChanged(activeCard: View?)
    }

    // region View layout logic
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
}
