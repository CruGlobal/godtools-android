package org.cru.godtools.base.tool.databinding.adapters

import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.BindingAdapter
import org.ccci.gto.android.common.util.dpToPixelSize
import org.cru.godtools.base.tool.model.compareTo
import org.cru.godtools.shared.tool.parser.model.Dimension
import org.cru.godtools.shared.tool.parser.model.Dimension.Percent
import org.cru.godtools.shared.tool.parser.model.Dimension.Pixels
import org.cru.godtools.tool.BuildConfig
import timber.log.Timber

@BindingAdapter("android:layout_width")
internal fun View.setLayoutWidth(w: Dimension?) {
    val lp = layoutParams
    when (w) {
        null -> Unit
        is Pixels -> {
            val size = dpToPixelSize(w.value, resources)
            if (size != lp.width) {
                lp.width = size
                layoutParams = lp
            }
        }
        is Percent -> when {
            lp is ConstraintLayout.LayoutParams -> setConstraintLayoutWidthPercentage(w.value)
            w.compareTo(1f) >= 0 -> {
                lp.width = ViewGroup.LayoutParams.MATCH_PARENT
                layoutParams = lp
            }
            else -> {
                val msg = "Unsupported LayoutParams for Percent Dimension: $lp"
                val e = UnsupportedOperationException(msg)
                if (BuildConfig.DEBUG) throw e
                Timber.tag("ViewGroupAdapters").e(e, msg)
            }
        }
    }
}
