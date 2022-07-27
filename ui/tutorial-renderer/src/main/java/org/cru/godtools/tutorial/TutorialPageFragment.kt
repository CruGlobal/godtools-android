package org.cru.godtools.tutorial

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.viewbinding.ViewBinding
import org.ccci.gto.android.common.androidx.fragment.app.BindingFragment
import org.ccci.gto.android.common.androidx.fragment.app.findListener
import org.cru.godtools.base.ui.theme.GodToolsTheme
import org.cru.godtools.tutorial.animation.animateViews
import org.cru.godtools.tutorial.databinding.TutorialOnboardingWelcomeBinding
import org.cru.godtools.tutorial.databinding.TutorialPageComposeBinding
import org.cru.godtools.tutorial.layout.TutorialPageLayout
import splitties.fragmentargs.arg
import splitties.fragmentargs.argOrNull

internal class TutorialPageFragment() : BindingFragment<ViewBinding>(), TutorialCallbacks {
    constructor(page: Page, formatArgs: Bundle?) : this() {
        this.page = page
        this.formatArgs = formatArgs
    }

    private var page by arg<Page>()
    private var formatArgs by argOrNull<Bundle>()

    // region Lifecycle
    override fun onCreateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = when (page.layout) {
        R.layout.tutorial_page_compose -> TutorialPageComposeBinding.inflate(inflater, container, false)
        else -> DataBindingUtil.inflate<ViewDataBinding>(inflater, page.layout, container, false)
    }

    override fun onBindingCreated(binding: ViewBinding, savedInstanceState: Bundle?) {
        when (binding) {
            is TutorialPageComposeBinding -> {
                binding.compose.setContent {
                    GodToolsTheme {
                        TutorialPageLayout(
                            page,
                            nextPage = { findListener<TutorialCallbacks>()?.nextPage() },
                            onTutorialAction = { findListener<TutorialCallbacks>()?.onTutorialAction(it) },
                        )
                    }
                }
            }
            is ViewDataBinding -> {
                binding.setVariable(BR.callbacks, this)
                binding.setVariable(BR.page, page)
                binding.setVariable(BR.formatArgs, formatArgs)
                binding.startAnimations()
            }
        }
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

    override fun onTutorialAction(id: Int) {
        findListener<TutorialCallbacks>()?.onTutorialAction(id)
    }
    // endregion TutorialCallbacks
}
