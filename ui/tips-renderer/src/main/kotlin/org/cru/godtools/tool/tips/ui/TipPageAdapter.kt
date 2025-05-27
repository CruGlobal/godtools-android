package org.cru.godtools.tool.tips.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import org.ccci.gto.android.common.androidx.recyclerview.adapter.SimpleDataBindingAdapter
import org.cru.godtools.shared.renderer.state.State
import org.cru.godtools.shared.tool.parser.model.tips.Tip
import org.cru.godtools.tool.tips.databinding.ToolTipPageBinding
import org.cru.godtools.tool.tips.ui.controller.TipPageController
import org.cru.godtools.tool.tips.ui.controller.bindController

internal class TipPageAdapter @AssistedInject internal constructor(
    @Assisted override val lifecycleOwner: LifecycleOwner,
    @Assisted private val toolState: State,
    private val controllerFactory: TipPageController.Factory
) : SimpleDataBindingAdapter<ToolTipPageBinding>(lifecycleOwner),
    Observer<Tip?>,
    TipCallbacks {
    @AssistedFactory
    interface Factory {
        fun create(lifecycleOwner: LifecycleOwner, toolState: State): TipPageAdapter
    }

    var callbacks: TipCallbacks? = null

    var tip: Tip? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    private fun getItem(position: Int) = tip!!.pages[position]
    override fun getItemCount() = tip?.pages?.size ?: 0

    // region Lifecycle
    override fun onChanged(value: Tip?) {
        tip = value
    }

    override fun onCreateViewDataBinding(parent: ViewGroup, viewType: Int) =
        ToolTipPageBinding.inflate(LayoutInflater.from(parent.context), parent, false)

    override fun onViewDataBindingCreated(binding: ToolTipPageBinding, viewType: Int) {
        binding.bindController(controllerFactory, lifecycleOwner, toolState)
        binding.callbacks = this
    }

    override fun onBindViewDataBinding(binding: ToolTipPageBinding, position: Int) {
        binding.controller?.model = getItem(position)
    }
    // endregion Lifecycle

    // region TipPageController.Callbacks
    override fun goToNextPage() {
        callbacks?.goToNextPage()
    }

    override fun closeTip(completed: Boolean) {
        callbacks?.closeTip(completed)
    }
    // endregion TipPageController.Callbacks
}
