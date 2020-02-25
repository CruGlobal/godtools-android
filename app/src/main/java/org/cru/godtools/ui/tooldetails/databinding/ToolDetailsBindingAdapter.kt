package org.cru.godtools.ui.tooldetails.databinding

import android.annotation.SuppressLint
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.databinding.adapters.TextViewBindingAdapter
import org.cru.godtools.base.util.getDisplayName
import java.util.Locale

@BindingAdapter("languages")
@SuppressLint("RestrictedApi")
fun TextView.bindLanguages(languages: List<Locale>?) =
    TextViewBindingAdapter.setText(this, languages?.joinToString(", ") { it.getDisplayName(context) })
