package org.cru.godtools.ui.tooldetails

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import org.ccci.gto.android.common.recyclerview.adapter.SimpleDataBindingAdapter
import org.cru.godtools.databinding.ToolDetailsPageDescriptionBinding
import org.cru.godtools.databinding.ToolDetailsPageLanguagesBinding

internal class ToolDetailsPagerAdapter(
    lifecycleOwner: LifecycleOwner,
    private val dataModel: ToolDetailsFragmentDataModel,
    private val linkClickListener: LinkClickedListener?
) : SimpleDataBindingAdapter<ViewDataBinding>(lifecycleOwner) {
    override fun getItemCount() = 2
    override fun getItemViewType(position: Int) = position

    override fun onCreateViewDataBinding(parent: ViewGroup, viewType: Int) = when (viewType) {
        0 -> ToolDetailsPageDescriptionBinding.inflate(LayoutInflater.from(parent.context), parent, false).apply {
            tool = dataModel.tool
            translation = dataModel.primaryTranslation
            autoLinkListener = linkClickListener
        }
        1 -> ToolDetailsPageLanguagesBinding.inflate(LayoutInflater.from(parent.context), parent, false).apply {
            languages = dataModel.availableLanguages
        }
        else -> throw IllegalArgumentException("page $viewType is not a valid page")
    }

    override fun onBindViewDataBinding(binding: ViewDataBinding, position: Int) = Unit
}
