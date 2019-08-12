package org.cru.godtools.article.aem.fragment

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.lifecycle.ViewModelProviders
import butterknife.BindView
import org.cru.godtools.article.aem.R
import org.cru.godtools.article.aem.R2
import org.cru.godtools.base.ui.fragment.BaseFragment
import splitties.fragmentargs.arg

class AemArticleFragment() : BaseFragment() {
    constructor(articleUri: Uri) : this() {
        this.articleUri = articleUri
    }

    private var articleUri: Uri by arg()

    // region Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupViewModel()
    }

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

    // region ViewModel
    private val viewModel by lazy { ViewModelProviders.of(this).get(AemArticleViewModel::class.java) }

    private fun setupViewModel() {
        viewModel.articleUri.value = articleUri
    }
    // endregion ViewModel

    // region WebView content
    @BindView(R2.id.frame)
    internal var webViewContainer: FrameLayout? = null

    private fun setupWebView() = webViewContainer?.addView(viewModel.getWebView(requireActivity()))

    private fun cleanupWebView() = webViewContainer?.removeView(viewModel.getWebView(requireActivity()))
    // endregion WebView content
}
