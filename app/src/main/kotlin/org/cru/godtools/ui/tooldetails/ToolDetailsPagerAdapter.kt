package org.cru.godtools.ui.tooldetails

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.recyclerview.widget.RecyclerView
import org.ccci.gto.android.common.androidx.recyclerview.adapter.SimpleDataBindingAdapter
import org.cru.godtools.R
import org.cru.godtools.base.ui.theme.GodToolsTheme
import org.cru.godtools.databinding.ToolDetailsPageDescriptionBinding
import org.cru.godtools.databinding.ToolDetailsPageVariantsBinding
import org.cru.godtools.ui.tools.ToolViewModels

internal class ToolDetailsPagerAdapter(
    lifecycleOwner: LifecycleOwner,
    private val dataModel: ToolDetailsFragmentDataModel,
    private val variantsAdapter: RecyclerView.Adapter<*>
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
        Page.DESCRIPTION ->
            ToolDetailsPageDescriptionBinding.inflate(LayoutInflater.from(parent.context), parent, false).apply {
                compose.setContent {
                    GodToolsTheme {
                        val toolCode by dataModel.toolCode.collectAsState()

                        toolCode?.let { code ->
                            ToolDetailsAbout(viewModel<ToolViewModels>()[code])
                        }
                    }
                }
            }
        Page.VARIANTS ->
            ToolDetailsPageVariantsBinding.inflate(LayoutInflater.from(parent.context), parent, false).apply {
                variants.adapter = variantsAdapter
            }
    }

    override fun onBindViewDataBinding(binding: ViewDataBinding, position: Int) = Unit
}
