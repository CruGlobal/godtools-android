package org.cru.godtools.base.ui.languages

import android.content.Context
import android.widget.Filter
import java.util.Locale
import org.ccci.gto.android.common.androidx.databinding.widget.DataBindingArrayAdapter
import org.ccci.gto.android.common.util.Ids
import org.cru.godtools.base.ui.R
import org.cru.godtools.base.ui.databinding.LanguagesDropdownItemBinding
import org.cru.godtools.model.Language

class LanguagesDropdownAdapter(context: Context) :
    DataBindingArrayAdapter<LanguagesDropdownItemBinding, Language>(context, R.layout.languages_dropdown_item) {
    override fun getItemId(position: Int) = getItem(position)?.code?.let { Ids.generate(it) } ?: -1

    override fun onBindingCreated(binding: LanguagesDropdownItemBinding) = Unit
    override fun onBind(binding: LanguagesDropdownItemBinding, position: Int) {
        binding.language = getItem(position)
    }

    override fun getFilter() = object : Filter() {
        override fun performFiltering(constraint: CharSequence?) = FilterResults()
        override fun publishResults(constraint: CharSequence?, results: FilterResults?) = Unit
        override fun convertResultToString(resultValue: Any?) = when (resultValue) {
            is Language -> resultValue.getDisplayName(context)
            else -> ""
        }
    }
}
