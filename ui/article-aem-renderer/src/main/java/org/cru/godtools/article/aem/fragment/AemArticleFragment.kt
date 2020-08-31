package org.cru.godtools.article.aem.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import dagger.hilt.android.AndroidEntryPoint
import org.cru.godtools.article.aem.R
import org.cru.godtools.article.aem.databinding.AemArticleFragmentBinding
import org.cru.godtools.article.aem.ui.AemArticleViewModel
import org.cru.godtools.base.ui.fragment.BaseFragment

@AndroidEntryPoint
class AemArticleFragment : BaseFragment<AemArticleFragmentBinding>(R.layout.aem_article_fragment) {
    private val viewModel: AemArticleViewModel by activityViewModels()

    override val View.viewBinding: AemArticleFragmentBinding get() = AemArticleFragmentBinding.bind(this)

    // region Lifecycle
    override fun onBindingCreated(binding: AemArticleFragmentBinding, savedInstanceState: Bundle?) {
        super.onBindingCreated(binding, savedInstanceState)
        binding.setupWebView()
    }

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
