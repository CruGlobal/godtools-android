package org.cru.godtools.tract.util

import com.airbnb.lottie.LottieComposition

fun LottieComposition.scaleTo(width: Float, height: Float) = minOf(width / bounds.width(), height / bounds.height())
