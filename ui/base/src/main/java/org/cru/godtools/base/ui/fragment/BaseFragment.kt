package org.cru.godtools.base.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import butterknife.ButterKnife
import butterknife.Unbinder

abstract class BaseFragment @JvmOverloads constructor(@LayoutRes layoutId: Int? = null) : Fragment(layoutId ?: 0) {
    // region Lifecycle
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindButterKnife(view)
    }

    override fun onDestroyView() {
        unbindButterKnife()
        super.onDestroyView()
    }
    // endregion Lifecycle

    // region ButterKnife
    private var butterKnife: Unbinder? = null

    private fun bindButterKnife(view: View) {
        butterKnife = ButterKnife.bind(this, view)
    }

    private fun unbindButterKnife() {
        butterKnife?.unbind()
        butterKnife = null
    }
    // endregion ButterKnife
}
