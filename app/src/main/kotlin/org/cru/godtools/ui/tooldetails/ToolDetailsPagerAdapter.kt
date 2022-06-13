package org.cru.godtools.ui.tooldetails

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import org.ccci.gto.android.common.androidx.recyclerview.adapter.SimpleDataBindingAdapter
import org.cru.godtools.R
import org.cru.godtools.databinding.ToolDetailsPageDescriptionBinding
import org.cru.godtools.databinding.ToolDetailsPageVariantsBinding

internal class ToolDetailsPagerAdapter(
    lifecycleOwner: LifecycleOwner,
    private val dataModel: ToolDetailsFragmentDataModel,
    private val variantsAdapter: RecyclerView.Adapter<*>,
    private val linkClickListener: LinkClickedListener?
) : SimpleDataBindingAdapter<ViewDataBinding>(lifecycleOwner) {
    enum class Page(@StringRes val tabLabel: Int) {
        DESCRIPTION(R.string.label_tools_about),
        VARIANTS(R.string.tool_details_section_variants_label)
    }

    init {
        setHasStableIds(true)
    }

    var pages: List<Page> = emptyList()
        set(value) {
            val distinct = value.distinct()
            if (field == distinct) return
            field = distinct
            notifyDataSetChanged()
        }

    fun getItem(position: Int) = pages[position]
    override fun getItemCount() = pages.size
    override fun getItemViewType(position: Int) = getItem(position).ordinal

    override fun onCreateViewDataBinding(parent: ViewGroup, viewType: Int) = when (Page.values()[viewType]) {
        Page.DESCRIPTION -> ToolDetailsPageDescriptionBinding
            .inflate(LayoutInflater.from(parent.context), parent, false).apply {
                tool = dataModel.tool
                translation = dataModel.primaryTranslation
                autoLinkListener = linkClickListener
                languages = dataModel.availableLanguages
            }
        Page.VARIANTS -> ToolDetailsPageVariantsBinding
            .inflate(LayoutInflater.from(parent.context), parent, false).apply {
                variants.adapter = variantsAdapter
            }
    }

    override fun onBindViewDataBinding(binding: ViewDataBinding, position: Int) = Unit
}
