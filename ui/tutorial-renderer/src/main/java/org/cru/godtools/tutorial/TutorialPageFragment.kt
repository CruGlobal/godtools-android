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
import org.cru.godtools.tutorial.databinding.TutorialLiveShareDescriptionBinding
import org.cru.godtools.tutorial.databinding.TutorialLiveShareMirroredBinding
import org.cru.godtools.tutorial.databinding.TutorialLiveShareStartBinding
import org.cru.godtools.tutorial.databinding.TutorialPageComposeBinding
import org.cru.godtools.tutorial.databinding.TutorialTipsLearnBinding
import org.cru.godtools.tutorial.databinding.TutorialTipsLightBinding
import org.cru.godtools.tutorial.databinding.TutorialTipsStartBinding
import org.cru.godtools.tutorial.layout.TipsTutorialLayout
import org.cru.godtools.tutorial.layout.TutorialLiveShareLayout
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
                binding.setupCompose()
            }
        }
    }
    // endregion Lifecycle

    private fun ViewDataBinding.setupCompose() {
        when (this) {
            is TutorialTipsLearnBinding -> compose.setContent {
                TipsTutorialLayout(
                    nextPage = { findListener<TutorialCallbacks>()?.nextPage() },
                    onTutorialAction = { findListener<TutorialCallbacks>()?.onTutorialAction(it) },
                    page,
                    R.raw.anim_tutorial_tips_people,
                    R.string.tutorial_tips_learn_headline, R.string.tutorial_tips_learn_text,

                )
            }
            is TutorialTipsLightBinding -> compose.setContent {
                TipsTutorialLayout(
                    nextPage = { findListener<TutorialCallbacks>()?.nextPage() },
                    onTutorialAction = { findListener<TutorialCallbacks>()?.onTutorialAction(it) },
                    page,
                    R.raw.anim_tutorial_tips_tool,
                    R.string.tutorial_tips_light_headline, R.string.tutorial_tips_light_text1,
                    R.string.tutorial_tips_light_text2
                )
            }
            is TutorialTipsStartBinding -> compose.setContent {
                TipsTutorialLayout(
                    nextPage = { findListener<TutorialCallbacks>()?.nextPage() },
                    onTutorialAction = { findListener<TutorialCallbacks>()?.onTutorialAction(it) },
                    page,
                    R.raw.anim_tutorial_tips_light,
                    R.string.tutorial_tips_start_headline, R.string.tutorial_tips_start_text
                )
            }
            is TutorialLiveShareDescriptionBinding -> compose.setContent {
                TutorialLiveShareLayout(
                    nextPage = { findListener<TutorialCallbacks>()?.nextPage() },
                    onTutorialAction = { findListener<TutorialCallbacks>()?.onTutorialAction(it) },
                    page,
                    null,
                    R.string.tutorial_live_share_description_headline, R.string.tutorial_live_share_description_text,
                    R.drawable.img_tutorial_live_share_people

                )
            }
            is TutorialLiveShareMirroredBinding -> compose.setContent {
                TutorialLiveShareLayout(
                    nextPage = { findListener<TutorialCallbacks>()?.nextPage() },
                    onTutorialAction = { findListener<TutorialCallbacks>()?.onTutorialAction(it) },
                    page,
                    R.raw.anim_tutorial_live_share_devices,
                    R.string.tutorial_live_share_mirrored_headline, R.string.tutorial_live_share_mirrored_text

                )
            }
            is TutorialLiveShareStartBinding -> compose.setContent {
                TutorialLiveShareLayout(
                    nextPage = { findListener<TutorialCallbacks>()?.nextPage() },
                    onTutorialAction = { findListener<TutorialCallbacks>()?.onTutorialAction(it) },
                    page,
                    R.raw.anim_tutorial_live_share_messages,
                    R.string.tutorial_live_share_start_headline, R.string.tutorial_live_share_start_text

                )
            }
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
