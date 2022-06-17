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
import org.cru.godtools.tutorial.databinding.TutorialTipsLearnBinding
import org.cru.godtools.tutorial.databinding.TutorialTipsLightBinding
import org.cru.godtools.tutorial.databinding.TutorialTipsStartBinding
import org.cru.godtools.tutorial.layout.TipsTutorialLayout
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
            binding = it
        }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.setupCompose()
        binding?.startAnimations()
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
    // endregion Lifecycle

    private fun ViewDataBinding.setupCompose() {
        when (this) {
            is TutorialTipsLearnBinding -> compose.setContent {
                TipsTutorialLayout(
                    nextPage = { findListener<TutorialCallbacks>()?.nextPage() },
                    onTutorialAction = {
                        view?.let { it1 ->
                            findListener<TutorialCallbacks>()?.onTutorialAction(it1)
                        }
                    },
                    R.raw.anim_tutorial_tips_people,
                    R.string.tutorial_tips_learn_headline, R.string.tutorial_tips_learn_text
                )
            }
            is TutorialTipsLightBinding -> compose.setContent {
                TipsTutorialLayout(
                    nextPage = { findListener<TutorialCallbacks>()?.nextPage() },
                    onTutorialAction = {
                        view?.let { it1 ->
                            findListener<TutorialCallbacks>()?.onTutorialAction(it1)
                        }
                    },
                    R.raw.anim_tutorial_tips_tool,
                    R.string.tutorial_tips_light_headline, R.string.tutorial_tips_light_text1,
                    R.string.tutorial_tips_light_text2
                )
            }
            is TutorialTipsStartBinding -> compose.setContent {
                TipsTutorialLayout(
                    nextPage = { findListener<TutorialCallbacks>()?.nextPage() },
                    onTutorialAction = {
                        view?.let { it1 ->
                            findListener<TutorialCallbacks>()?.onTutorialAction(it1)
                        }
                    },
                    R.raw.anim_tutorial_tips_light,
                    R.string.tutorial_tips_start_headline, R.string.tutorial_tips_start_text
                )
            }
        }
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
