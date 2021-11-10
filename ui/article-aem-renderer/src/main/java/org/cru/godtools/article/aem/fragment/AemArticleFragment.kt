package org.cru.godtools.article.aem.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import dagger.hilt.android.AndroidEntryPoint
import org.cru.godtools.article.aem.databinding.AemArticleFragmentBinding
import org.cru.godtools.article.aem.ui.AemArticleViewModel
import org.cru.godtools.base.ui.fragment.BaseFragment

@AndroidEntryPoint
class AemArticleFragment : BaseFragment<AemArticleFragmentBinding>() {
    private val viewModel: AemArticleViewModel by activityViewModels()

    // region Lifecycle
    override fun onCreateBinding(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        AemArticleFragmentBinding.inflate(inflater, container, false).apply { setupWebView() }

    override fun onDestroyBinding(binding: AemArticleFragmentBinding) {
        binding.cleanupWebView()
        super.onDestroyBinding(binding)
    }
    // endregion Lifecycle

    // region WebView content
    private fun AemArticleFragmentBinding.setupWebView() = frame.addView(viewModel.getWebView(requireActivity()))
    private fun AemArticleFragmentBinding.cleanupWebView() = frame.removeView(viewModel.getWebView(requireActivity()))
    // endregion WebView content
}
