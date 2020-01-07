package org.cru.godtools.base.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import butterknife.ButterKnife
import butterknife.Unbinder

abstract class BaseFragment : Fragment() {
    private var butterKnife: Unbinder? = null

    // region Lifecycle
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        butterKnife = ButterKnife.bind(this, view)
    }

    override fun onDestroyView() {
        butterKnife?.unbind()
        butterKnife = null
        super.onDestroyView()
    }
    // endregion Lifecycle
}
