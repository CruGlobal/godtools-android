package org.cru.godtools.tutorial

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.ccci.gto.android.common.androidx.fragment.app.BindingFragment
import org.ccci.gto.android.common.androidx.fragment.app.findListener
import org.cru.godtools.base.ui.theme.GodToolsTheme
import org.cru.godtools.tutorial.databinding.TutorialPageComposeBinding
import org.cru.godtools.tutorial.layout.TutorialPageLayout
import splitties.fragmentargs.arg

internal class TutorialPageFragment() : BindingFragment<TutorialPageComposeBinding>() {
    constructor(page: Page) : this() {
        this.page = page
    }

    private var page by arg<Page>()

    // region Lifecycle
    override fun onCreateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = TutorialPageComposeBinding.inflate(inflater, container, false)

    override fun onBindingCreated(binding: TutorialPageComposeBinding, savedInstanceState: Bundle?) {
        binding.compose.setContent {
            GodToolsTheme {
                TutorialPageLayout(
                    page,
                    nextPage = { findListener<TutorialCallbacks>()?.nextPage() },
                    onTutorialAction = { findListener<TutorialCallbacks>()?.onTutorialAction(it) },
                    modifier = Modifier.padding(top = 48.dp, bottom = 64.dp)
                )
            }
        }
    }
    // endregion Lifecycle
}
