package org.cru.godtools.ui.languages.paralleldialog

import android.content.Context
import android.widget.Filter
import org.ccci.gto.android.common.androidx.databinding.widget.DataBindingArrayAdapter
import org.ccci.gto.android.common.support.v4.util.IdUtils
import org.cru.godtools.R
import org.cru.godtools.base.util.deviceLocale
import org.cru.godtools.databinding.LanguagesParallelDialogItemBinding as ItemBinding
import org.cru.godtools.model.Language

class LanguagesAdapter(context: Context) :
    DataBindingArrayAdapter<ItemBinding, Language>(context, R.layout.languages_parallel_dialog_item) {
    override fun getItemId(position: Int) = getItem(position)?.code?.let { IdUtils.convertId(it) } ?: -1

    override fun onBindingCreated(binding: ItemBinding) = Unit
    override fun onBind(binding: ItemBinding, position: Int) {
        binding.language = getItem(position)
    }

    override fun getFilter() = object : Filter() {
        override fun performFiltering(constraint: CharSequence?) = FilterResults()
        override fun publishResults(constraint: CharSequence?, results: FilterResults?) = Unit
        override fun convertResultToString(resultValue: Any?) =
            (resultValue as? Language)?.getDisplayName(context, context.deviceLocale).orEmpty()
    }
}
