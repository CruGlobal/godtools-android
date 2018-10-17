package org.cru.godtools.article.fragment;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import org.ccci.gto.android.common.support.v4.util.FragmentUtils;
import org.cru.godtools.article.R;
import org.cru.godtools.article.R2;
import org.cru.godtools.article.adapter.ArticlesAdapter;
import org.cru.godtools.articles.aem.db.ArticleDao;
import org.cru.godtools.articles.aem.db.ArticleRoomDatabase;
import org.cru.godtools.articles.aem.model.Article;
import org.cru.godtools.articles.aem.service.AEMDownloadManger;
import org.cru.godtools.base.tool.fragment.BaseToolFragment;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

import butterknife.BindView;
import timber.log.Timber;

import static org.cru.godtools.article.Constants.EXTRA_CATEGORY;

public class ArticlesFragment extends BaseToolFragment implements ArticlesAdapter.Callbacks,
        SwipeRefreshLayout.OnRefreshListener {

    public interface Callbacks {
        void onArticleSelected(@Nullable Article article);
    }

    @Nullable
    @BindView(R2.id.articles_recycler_view)
    RecyclerView mArticlesView;
    @Nullable
    @BindView(R2.id.article_swipe_container)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @Nullable
    ArticlesAdapter mArticlesAdapter;

    // these properties should be treated as final and only set/modified in onCreate()
    @Nullable
    private /*final*/ String mCategory;
    @NonNull
    private /*final*/ ArticleListViewModel mViewModel;

    public static ArticlesFragment newInstance(@NonNull final String code, @NonNull final Locale locale,
                                               @Nullable final String category) {
        final ArticlesFragment fragment = new ArticlesFragment();
        final Bundle args = new Bundle(3);
        populateArgs(args, code, locale);
        args.putString(EXTRA_CATEGORY, category);
        fragment.setArguments(args);
        return fragment;
    }

    // region LifeCycle Events

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle args = getArguments();
        if (args != null) {
            mCategory = args.getString(EXTRA_CATEGORY, mCategory);
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
        setUpSwipeRefresh();
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

        if (mViewModel.articles == null) {
            final ArticleDao articleDao = ArticleRoomDatabase.getInstance(requireContext()).articleDao();
            mViewModel.articles = mCategory != null ? articleDao.getArticles(mTool, mLocale, mCategory) :
                    articleDao.getArticles(mTool, mLocale);
        }
    }

    // region refresh Layout
    private void setUpSwipeRefresh() {
        if (mSwipeRefreshLayout == null) {
            return;
        }

        mSwipeRefreshLayout.setOnRefreshListener(this);
    }

    private void setRefreshing(boolean isRefreshing) {
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setRefreshing(isRefreshing);
        }
    }

    @Override
    public void onRefresh() {
        Timber.d("onRefresh() called");
        setUpListenableFuture();
    }

    private void setUpListenableFuture() {
        AEMDownloadManger manger = AEMDownloadManger.getInstance(requireContext());
        ListeningExecutorService service = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(4));
        ListenableFuture<Boolean> future = service.submit(() ->
                manger.enqueueSyncManifestAemImports(mManifest), true);
        Futures.addCallback(future, new FutureCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                setRefreshing(false);
            }

            @Override
            public void onFailure(Throwable t) {
                Timber.d(t);
                setRefreshing(false);
            }
        }, manger.getExecutor());
    }

    //endregion refresh Layout

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
        LiveData<List<Article>> articles;
    }
}
