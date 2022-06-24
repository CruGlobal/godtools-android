package org.cru.godtools.ui.dashboard.lessons

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.CallSuper
import dagger.hilt.android.AndroidEntryPoint
import org.ccci.gto.android.common.androidx.fragment.app.findListener
import org.cru.godtools.R
import org.cru.godtools.base.ui.fragment.BaseFragment
import org.cru.godtools.databinding.ComposeLayoutBinding
import org.cru.godtools.ui.tools.ToolsAdapterCallbacks

@AndroidEntryPoint
class LessonsFragment : BaseFragment<ComposeLayoutBinding>(R.layout.compose_layout) {
    override fun onCreateBinding(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        ComposeLayoutBinding.inflate(inflater, container, false)

    @CallSuper
    override fun onBindingCreated(binding: ComposeLayoutBinding, savedInstanceState: Bundle?) {
        super.onBindingCreated(binding, savedInstanceState)
        binding.compose.setContent {
            LessonsLayout(
                onOpenLesson = { tool, tr1 -> findListener<ToolsAdapterCallbacks>()?.openTool(tool, tr1) },
            )
        }
    }
}
