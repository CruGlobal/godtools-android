package org.cru.godtools.tutorial

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import org.ccci.gto.android.common.util.findListener
import org.cru.godtools.tutorial.animation.animateViews
import org.cru.godtools.tutorial.databinding.TutorialOnboardingWelcomeBinding
import org.cru.godtools.tutorial.util.TutorialCallbacks
import splitties.fragmentargs.arg

internal class TutorialPageFragment() : Fragment(), TutorialCallbacks {
    constructor(page: Page) : this() {
        this.page = page
    }

    private var page by arg<Page>()

    private var binding: ViewDataBinding? = null

    // region Lifecycle
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, page.layout, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.setVariable(BR.callbacks, this)
        binding?.startAnimations()
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
    // endregion Lifecycle

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

    override fun launchTraining() {
        findListener<TutorialCallbacks>()?.launchTraining()
    }

    override fun finishTutorial() {
        findListener<TutorialCallbacks>()?.finishTutorial()
    }
    // endregion TutorialCallbacks
}
