package org.cru.godtools.databinding

import android.view.View
import androidx.databinding.BindingAdapter

private const val VISIBLE_IF = "visibleIf"
private const val INVISIBLE_IF = "invisibleIf"
private const val GONE_IF = "goneIf"

@BindingAdapter(VISIBLE_IF)
fun View.visibleIf(visible: Boolean) = visibility(visible, invisible = false, gone = !visible)

@BindingAdapter(VISIBLE_IF, INVISIBLE_IF, GONE_IF)
fun View.visibility(visible: Boolean, invisible: Boolean, gone: Boolean) {
    visibility = when {
        gone -> View.GONE
        invisible -> View.INVISIBLE
        visible -> View.VISIBLE
        else -> View.GONE
    }
}
