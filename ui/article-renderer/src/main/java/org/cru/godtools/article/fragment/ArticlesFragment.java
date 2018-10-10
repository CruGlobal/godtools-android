package org.cru.godtools.article.fragment;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.ccci.gto.android.common.support.v4.util.FragmentUtils;
import org.cru.godtools.article.R;
import org.cru.godtools.article.R2;
import org.cru.godtools.article.adapter.ArticlesAdapter;
import org.cru.godtools.articles.aem.db.ArticleRoomDatabase;
import org.cru.godtools.articles.aem.model.Article;
import org.cru.godtools.base.tool.fragment.BaseToolFragment;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;

public class ArticlesFragment extends BaseToolFragment implements ArticlesAdapter.Callbacks {
    private static final String MANIFEST_KEY = "manifest-key";

    public interface Callbacks {
        void onArticleSelected(@Nullable Article article);
    }

    @Nullable
    @BindView(R2.id.articles_recycler_view)
    RecyclerView mArticlesView;
    @Nullable
    ArticlesAdapter mArticlesAdapter;

    String mManifestKey = "";

    // these properties should be treated as final and only set/modified in onCreate()
    @NonNull
    private /*final*/ ArticleListViewModel mViewModel;

    public static ArticlesFragment newInstance(@NonNull final String code, @NonNull final Locale locale,
                                               String manifestKey) {
        final ArticlesFragment fragment = new ArticlesFragment();
        final Bundle args = new Bundle();
        populateArgs(args, code, locale);
        args.putString(MANIFEST_KEY, manifestKey);
        fragment.setArguments(args);
        return fragment;
    }

    // region LifeCycle Events

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle args = getArguments();
        if (args != null) {
            mManifestKey = args.getString(MANIFEST_KEY, mManifestKey);
        }

        setupViewModel();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_articles, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupArticlesView();
    }

    /**
     * This is the Override Method from BaseToolFragment. This is fired anytime the Manifest Object is updated and will
     * update the adapter of it's changes.
     */
    @Override
    protected void onManifestUpdated() {
        super.onManifestUpdated();
        updateArticlesViewManifest();
    }

    /**
     * This is the callback method from ArticleAdapter that will handle the functionality of an article being selected
     * from the list.
     *
     * @param article the selected Article
     */
    @Override
    public void onArticleSelected(@Nullable final Article article) {
        final Callbacks callbacks = FragmentUtils.getListener(this, Callbacks.class);
        if (callbacks != null) {
            callbacks.onArticleSelected(article);
        }
    }

    @Override
    public void onDestroyView() {
        cleanupArticlesView();
        super.onDestroyView();
    }

    // endregion LifeCycle Events

    private void setupViewModel() {
        mViewModel = ViewModelProviders.of(this).get(ArticleListViewModel.class);

        if (!mViewModel.initialized) {
            final ArticleRoomDatabase aemDb = ArticleRoomDatabase.getInstance(requireContext());
            mViewModel.articles = aemDb.articleDao().getArticles(mTool, mLocale);
            mViewModel.initialized = true;
        }
    }

    // region ArticlesView

    /**
     * This method will initialize all of the required data for the RecyclerView
     */
    private void setupArticlesView() {
        if (mArticlesView != null) {
            mArticlesView.addItemDecoration(
                    new DividerItemDecoration(mArticlesView.getContext(), DividerItemDecoration.VERTICAL));

            mArticlesAdapter = new ArticlesAdapter();
            mArticlesAdapter.setCallbacks(this);
            mViewModel.articles.observe(this, mArticlesAdapter);
            mArticlesView.setAdapter(mArticlesAdapter);

            updateArticlesViewManifest();
        }
    }

    private void updateArticlesViewManifest() {
        if (mArticlesAdapter != null) {
            mArticlesAdapter.setToolManifest(mManifest);
        }
    }

    private void cleanupArticlesView() {
        if (mArticlesAdapter != null) {
            mArticlesAdapter.setCallbacks(null);
            mViewModel.articles.removeObserver(mArticlesAdapter);
        }
        mArticlesAdapter = null;
    }

    // endregion ArticlesView

    public static class ArticleListViewModel extends ViewModel {
        boolean initialized = false;
        LiveData<List<Article>> articles;
    }
}
