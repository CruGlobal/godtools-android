package org.cru.godtools.article.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.cru.godtools.article.R;
import org.cru.godtools.article.adapter.ArticleAdapter;
import org.cru.godtools.articles.aem.view_model.ArticleViewModel;

import java.util.Objects;

public class ArticlesFragment extends Fragment {
    private static final String MANIFEST_Key = "manifest-key";
    private ArticleAdapter mAdapter;

    public static ArticlesFragment newInstance(String manifestKey){
        ArticlesFragment fragment = new ArticlesFragment();
        Bundle args = new Bundle();
        args.putString(MANIFEST_Key, manifestKey);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        return inflater.inflate(R.layout.fragment_articles, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        RecyclerView recyclerView = Objects.requireNonNull(getActivity())
                .findViewById(R.id.articles_recycler_view);
        mAdapter = new ArticleAdapter(getActivity());
        recyclerView.setAdapter(mAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        ArticleViewModel viewModel = ArticleViewModel.getInstance(getActivity());

        String manifestKey = getArguments() != null ? getArguments().getString(MANIFEST_Key) : "";

        viewModel.getArticlesByManifest(manifestKey).observe(this, articles -> {
            // This will be triggered by any change to the database
            mAdapter.setArticles(articles);
        });
    }
}
