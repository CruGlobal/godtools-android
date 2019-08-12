package org.cru.godtools.article.aem.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import org.cru.godtools.article.aem.R;
import org.cru.godtools.article.aem.R2;
import org.cru.godtools.base.ui.fragment.BaseFragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import butterknife.BindView;

import static org.cru.godtools.article.aem.Constants.EXTRA_ARTICLE;

public class AemArticleFragment extends BaseFragment {
    static final String TAG = "AemArticleFragment";

    @Nullable
    @BindView(R2.id.frame)
    FrameLayout mWebViewContainer;

    private AemArticleViewModel mViewModel;

    // these properties should be treated as final and only set/modified in onCreate()
    @Nullable
    private /*final*/ Uri mArticleUri;

    public static AemArticleFragment newInstance(@NonNull final Uri articleUri) {
        final AemArticleFragment fragment = new AemArticleFragment();
        final Bundle args = new Bundle(3);
        args.putParcelable(EXTRA_ARTICLE, articleUri);
        fragment.setArguments(args);
        return fragment;
    }

    // region Lifecycle Events

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle args = getArguments();
        if (args != null) {
            mArticleUri = args.getParcelable(EXTRA_ARTICLE);
        }

        validateStartState();

        setupViewModel();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_aem_article, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupWebView();
    }

    @Override
    public void onDestroyView() {
        cleanupWebView();
        super.onDestroyView();
    }

    // endregion Lifecycle Events

    private void validateStartState() {
        if (mArticleUri == null) {
            throw new IllegalStateException("No article specified");
        }
    }

    private void setupViewModel() {
        mViewModel = ViewModelProviders.of(this).get(AemArticleViewModel.class);
        mViewModel.getArticleUri().setValue(mArticleUri);
    }

    // region WebView content
    private void setupWebView() {
        if (mWebViewContainer != null) {
            mWebViewContainer.addView(mViewModel.getWebView(requireActivity()));
        }
    }

    private void cleanupWebView() {
        if (mWebViewContainer != null) {
            mWebViewContainer.removeView(mViewModel.getWebView(requireActivity()));
        }
    }
    // endregion WebView content
}
