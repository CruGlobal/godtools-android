package org.cru.godtools.tract.ui.tips

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import org.ccci.gto.android.common.recyclerview.adapter.SimpleDataBindingAdapter
import org.cru.godtools.tool.model.tips.Tip
import org.cru.godtools.tool.state.State
import org.cru.godtools.tract.databinding.TractTipPageBinding
import org.cru.godtools.tract.ui.controller.tips.TipPageController
import org.cru.godtools.tract.ui.controller.tips.bindController

class TipPageAdapter @AssistedInject internal constructor(
    @Assisted lifecycleOwner: LifecycleOwner,
    @Assisted private val toolState: State,
    private val controllerFactory: TipPageController.Factory
) : SimpleDataBindingAdapter<TractTipPageBinding>(lifecycleOwner), Observer<Tip?>, TipCallbacks {
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
    override fun onChanged(t: Tip?) {
        tip = t
    }

    override fun onCreateViewDataBinding(parent: ViewGroup, viewType: Int) =
        TractTipPageBinding.inflate(LayoutInflater.from(parent.context), parent, false).apply {
            bindController(controllerFactory, toolState)
            callbacks = this@TipPageAdapter
        }

    override fun onBindViewDataBinding(binding: TractTipPageBinding, position: Int) {
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
