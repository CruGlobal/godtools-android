package org.cru.godtools.ui.tooldetails

import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import org.cru.godtools.BR
import org.cru.godtools.ui.tools.ToolViewModels
import org.cru.godtools.ui.tools.ToolsAdapter

class VariantToolsAdapter(
    lifecycleOwner: LifecycleOwner,
    toolViewModels: ToolViewModels,
    itemLayout: Int,
    private val selectedVariant: LiveData<String?>
) : ToolsAdapter(lifecycleOwner, toolViewModels, itemLayout) {
    override fun onCreateViewDataBinding(parent: ViewGroup, viewType: Int) =
        super.onCreateViewDataBinding(parent, viewType)
            .also { it.setVariable(BR.selectedVariant, selectedVariant) }
}
