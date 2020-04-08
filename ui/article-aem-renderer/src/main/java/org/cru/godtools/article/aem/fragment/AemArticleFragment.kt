package org.cru.godtools.article.aem.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.activityViewModels
import butterknife.BindView
import org.cru.godtools.article.aem.R
import org.cru.godtools.article.aem.R2
import org.cru.godtools.article.aem.ui.AemArticleViewModel
import org.cru.godtools.base.ui.fragment.BaseFragment

class AemArticleFragment : BaseFragment<ViewDataBinding>() {
    private val viewModel: AemArticleViewModel by activityViewModels()

    override val hasDataBinding get() = false

    // region Lifecycle
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        inflater.inflate(R.layout.fragment_aem_article, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupWebView()
    }

    override fun onDestroyView() {
        cleanupWebView()
        super.onDestroyView()
    }
    // endregion Lifecycle

    // region WebView content
    @JvmField
    @BindView(R2.id.frame)
    internal var webViewContainer: FrameLayout? = null

    private fun setupWebView() = webViewContainer?.addView(viewModel.getWebView(requireActivity()))

    private fun cleanupWebView() = webViewContainer?.removeView(viewModel.getWebView(requireActivity()))
    // endregion WebView content
}
