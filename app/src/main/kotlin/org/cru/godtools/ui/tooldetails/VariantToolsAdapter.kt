package org.cru.godtools.ui.tooldetails

import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import org.cru.godtools.BR
import org.cru.godtools.ui.tools.ToolsAdapter
import org.cru.godtools.ui.tools.ToolsAdapterViewModel

class VariantToolsAdapter(
    lifecycleOwner: LifecycleOwner,
    dataModel: ToolsAdapterViewModel,
    itemLayout: Int,
    private val selectedVariant: LiveData<String?>
) : ToolsAdapter(lifecycleOwner, dataModel, itemLayout) {
    override fun onCreateViewDataBinding(parent: ViewGroup, viewType: Int) =
        super.onCreateViewDataBinding(parent, viewType)
            .also { it.setVariable(BR.selectedVariant, selectedVariant) }
}
