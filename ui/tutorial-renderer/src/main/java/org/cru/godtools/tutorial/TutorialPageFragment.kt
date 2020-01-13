package org.cru.godtools.tutorial

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import org.ccci.gto.android.common.util.findListener
import org.cru.godtools.tutorial.analytics.model.TutorialAnalyticsActionEvent
import org.cru.godtools.tutorial.analytics.model.TutorialAnalyticsScreenEvent
import org.cru.godtools.tutorial.animation.animateViews
import org.cru.godtools.tutorial.databinding.TutorialOnboardingWelcomeBinding
import org.cru.godtools.tutorial.util.TutorialCallbacks
import org.greenrobot.eventbus.EventBus
import splitties.fragmentargs.arg

internal class TutorialPageFragment() : Fragment(), TutorialCallbacks {
    constructor(page: Page) : this() {
        this.page = page
    }

    private var page by arg<Page>()

    private var binding: ViewDataBinding? = null
    private val eventBus by lazy { EventBus.getDefault() }

    // region Lifecycle
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        DataBindingUtil.inflate<ViewDataBinding>(inflater, page.layout, container, false).also {
            it.lifecycleOwner = this
            it.setVariable(BR.callbacks, this)
            it.setVariable(BR.lifecycleOwner2, this)
            it.setVariable(BR.isVisible, false)
            binding = it
        }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.setVariable(BR.callbacks, this)
        binding?.startAnimations()
    }

    override fun onResume() {
        super.onResume()
        eventBus.post(TutorialAnalyticsScreenEvent(page.screenName))
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
        eventBus.post(TutorialAnalyticsScreenEvent(page.screenName))
        findListener<TutorialCallbacks>()?.nextPage()
    }

    override fun launchTraining() {
        eventBus.post(TutorialAnalyticsActionEvent(TutorialAnalyticsActionEvent.TUTORIAL_MORE_ACTION))
        findListener<TutorialCallbacks>()?.launchTraining()
    }

    override fun finishTutorial() {
        eventBus.post(TutorialAnalyticsActionEvent(TutorialAnalyticsActionEvent.TUTORIAL_START_ACTION))
        findListener<TutorialCallbacks>()?.finishTutorial()
    }
    // endregion TutorialCallbacks
}
