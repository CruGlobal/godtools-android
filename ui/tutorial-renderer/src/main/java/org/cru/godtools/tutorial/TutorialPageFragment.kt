package org.cru.godtools.tutorial

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.viewbinding.ViewBinding
import org.ccci.gto.android.common.androidx.fragment.app.BindingFragment
import org.ccci.gto.android.common.androidx.fragment.app.findListener
import org.cru.godtools.base.ui.theme.GodToolsTheme
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
                            modifier = Modifier
                                .padding(
                                    top = dimensionResource(R.dimen.tutorial_page_inset_top),
                                    bottom = dimensionResource(R.dimen.tutorial_page_inset_bottom)
                                )
                        )
                    }
                }
            }
            is ViewDataBinding -> {
                binding.setVariable(BR.callbacks, this)
            }
        }
    }
    // endregion Lifecycle

    // region TutorialCallbacks
    override fun nextPage() {
        findListener<TutorialCallbacks>()?.nextPage()
    }

    override fun onTutorialAction(id: Int) {
        findListener<TutorialCallbacks>()?.onTutorialAction(id)
    }
    // endregion TutorialCallbacks
}
