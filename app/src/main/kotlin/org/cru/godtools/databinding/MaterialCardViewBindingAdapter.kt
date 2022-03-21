package org.cru.godtools.databinding

import androidx.databinding.BindingAdapter
import com.google.android.material.card.MaterialCardView

@BindingAdapter("strokeWidth")
fun MaterialCardView.setStrokeWidth(width: Float) = setStrokeWidth(width.toInt())
