package org.cru.godtools.ui.tooldetails.databinding

import android.annotation.SuppressLint
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.databinding.adapters.TextViewBindingAdapter
import java.text.Collator
import java.util.Locale
import org.ccci.gto.android.common.compat.util.LocaleCompat
import org.ccci.gto.android.common.compat.util.LocaleCompat.Category
import org.cru.godtools.base.util.getDisplayName

@BindingAdapter("languages")
@SuppressLint("RestrictedApi")
fun TextView.bindLanguages(languages: List<Locale>?) = TextViewBindingAdapter.setText(
    this,
    languages?.map { it.getDisplayName(context) }
        ?.sortedWith(
            Collator.getInstance(LocaleCompat.getDefault(Category.DISPLAY)).apply { strength = Collator.PRIMARY }
        )
        ?.joinToString(", ")
)
