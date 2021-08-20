package org.cru.godtools.tutorial

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import org.ccci.gto.android.common.androidx.fragment.app.findListener
import org.cru.godtools.tutorial.animation.animateViews
import org.cru.godtools.tutorial.databinding.TutorialOnboardingWelcomeBinding
import splitties.fragmentargs.arg
import splitties.fragmentargs.argOrNull

internal class TutorialPageFragment() : Fragment(), TutorialCallbacks {
    constructor(page: Page, formatArgs: Bundle?) : this() {
        this.page = page
        this.formatArgs = formatArgs
    }

    private var page by arg<Page>()
    private var formatArgs by argOrNull<Bundle>()

    private var binding: ViewDataBinding? = null

    // region Lifecycle
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        DataBindingUtil.inflate<ViewDataBinding>(inflater, page.layout, container, false).also {
            it.lifecycleOwner = viewLifecycleOwner
            it.setVariable(BR.callbacks, this)
            it.setVariable(BR.page, page)
            it.setVariable(BR.formatArgs, formatArgs)
            it.setVariable(BR.lifecycleOwner2, viewLifecycleOwner)
            it.setVariable(BR.isVisible, false)
            binding = it
        }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.startAnimations()
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
    // endregion Lifecycle

    // HACK: we leverage menu visibility to infer when the fragment is visible or not
    override fun setMenuVisibility(menuVisible: Boolean) {
        super.setMenuVisibility(menuVisible)
        binding?.setVariable(BR.isVisible, menuVisible)
    }

    private fun ViewDataBinding.startAnimations() {
        when (this) {
            is TutorialOnboardingWelcomeBinding -> animateViews()
            else -> Unit
        }
    }

    // region TutorialCallbacks
    override fun nextPage() {
        findListener<TutorialCallbacks>()?.nextPage()
    }

    override fun onTutorialAction(view: View) {
        findListener<TutorialCallbacks>()?.onTutorialAction(view)
    }
    // endregion TutorialCallbacks
}
