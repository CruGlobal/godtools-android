package org.cru.godtools.tract.util

import android.content.Context
import android.util.TypedValue
import android.view.MenuItem
import androidx.annotation.RawRes
import com.airbnb.lottie.LottieCompositionFactory
import com.airbnb.lottie.LottieDrawable

fun MenuItem.loadAnimation(context: Context, @RawRes rawRes: Int) {
    LottieCompositionFactory.fromRawRes(context, rawRes).addListener { comp ->
        icon = LottieDrawable().apply {
            composition = comp
            repeatCount = LottieDrawable.INFINITE
            val size = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24f, context.resources.displayMetrics)
            scale = comp.scaleTo(size, size)
            playAnimation()
        }
    }
}
