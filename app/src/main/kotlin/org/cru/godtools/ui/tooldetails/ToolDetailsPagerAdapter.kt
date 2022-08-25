package org.cru.godtools.ui.tooldetails

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.recyclerview.widget.RecyclerView
import org.ccci.gto.android.common.androidx.recyclerview.adapter.SimpleDataBindingAdapter
import org.cru.godtools.base.ui.theme.GodToolsTheme
import org.cru.godtools.databinding.ToolDetailsPageDescriptionBinding
import org.cru.godtools.databinding.ToolDetailsPageVariantsBinding
import org.cru.godtools.ui.tools.ToolViewModels

internal class ToolDetailsPagerAdapter(
    lifecycleOwner: LifecycleOwner,
    private val dataModel: ToolDetailsFragmentDataModel,
    private val variantsAdapter: RecyclerView.Adapter<*>
) : SimpleDataBindingAdapter<ViewDataBinding>(lifecycleOwner) {

    init {
        setHasStableIds(true)
    }

    var pages: List<ToolDetailsPage> = emptyList()
        set(value) {
            val distinct = value.distinct()
            if (field == distinct) return
            field = distinct
            notifyDataSetChanged()
        }

    fun getItem(position: Int) = pages[position]
    override fun getItemCount() = pages.size
    override fun getItemViewType(position: Int) = getItem(position).ordinal

    override fun onCreateViewDataBinding(parent: ViewGroup, viewType: Int) = when (ToolDetailsPage.values()[viewType]) {
        ToolDetailsPage.DESCRIPTION ->
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
        ToolDetailsPage.VARIANTS ->
            ToolDetailsPageVariantsBinding.inflate(LayoutInflater.from(parent.context), parent, false).apply {
                compose.setContent {
                    GodToolsTheme {
                        ToolDetailsVariants(dataModel, modifier = Modifier.padding(all = 16.dp), onVariantSelected = {})
                    }
                }
            }
    }

    override fun onBindViewDataBinding(binding: ViewDataBinding, position: Int) = Unit
}
