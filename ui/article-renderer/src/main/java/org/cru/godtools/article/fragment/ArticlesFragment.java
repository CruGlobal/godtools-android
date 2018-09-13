package org.cru.godtools.article.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.cru.godtools.article.R;
import org.cru.godtools.article.R2;
import org.cru.godtools.article.adapter.ArticleAdapter;
import org.cru.godtools.articles.aem.model.Article;
import org.cru.godtools.articles.aem.view_model.ArticleViewModel;
import org.cru.godtools.base.ui.fragment.BaseFragment;

import butterknife.BindView;
import timber.log.Timber;

public class ArticlesFragment extends BaseFragment implements ArticleAdapter.Callback {
    private static final String MANIFEST_Key = "manifest-key";

    @Nullable
    @BindView(R2.id.articles_recycler_view)
    RecyclerView mArticlesView;

    ArticleAdapter mAdapter;

    public static ArticlesFragment newInstance(String manifestKey) {
        ArticlesFragment fragment = new ArticlesFragment();
        Bundle args = new Bundle();
        args.putString(MANIFEST_Key, manifestKey);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        return inflater.inflate(R.layout.fragment_articles, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setupArticleRecyclerView();
    }

    private void setupArticleRecyclerView() {
        if (mArticlesView != null) {
            mAdapter = new ArticleAdapter();
            mAdapter.setCallbacks(this);

            mArticlesView.setLayoutManager(new LinearLayoutManager(getActivity()));
            mArticlesView.setAdapter(mAdapter);

            ArticleViewModel viewModel = ArticleViewModel.getInstance(getActivity());

            String manifestKey = getArguments() != null ? getArguments().getString(MANIFEST_Key) : "";

            viewModel.getArticlesByManifest(manifestKey).observe(this, articles -> {
                // This will be triggered by any change to the database
                mAdapter.setArticles(articles);
            });
        }
    }

    @Override
    public void onArticleSelected(Article article) {
        Timber.d("You selected %s as your article", article.mTitle);
    }
}
