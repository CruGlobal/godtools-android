package org.cru.godtools.base.tool.databinding

import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.BindingAdapter
import org.ccci.gto.android.common.util.dpToPixelSize
import org.cru.godtools.base.tool.BuildConfig
import org.cru.godtools.tool.model.Dimension
import org.cru.godtools.tool.model.Dimension.Percent
import org.cru.godtools.tool.model.Dimension.Pixels
import timber.log.Timber

@BindingAdapter("android:layout_width")
internal fun View.setLayoutWidth(w: Dimension) {
    when (w) {
        is Pixels -> {
            val size = dpToPixelSize(w.value, resources)
            val lp = layoutParams
            if (size != lp.width) {
                lp.width = size
                layoutParams = lp
            }
        }
        is Percent -> when (val lp = layoutParams) {
            is ConstraintLayout.LayoutParams -> setConstraintLayoutWidthPercentage(w.value)
            else -> {
                val msg = "Unsupported LayoutParams for Percent Dimension: $lp"
                val e = UnsupportedOperationException(msg)
                if (BuildConfig.DEBUG) throw e
                Timber.tag("ViewGroupAdapters").e(e, msg)
            }
        }
    }
}
