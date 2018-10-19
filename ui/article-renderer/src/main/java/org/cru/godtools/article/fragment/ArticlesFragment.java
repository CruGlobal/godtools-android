package org.cru.godtools.article.fragment;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
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

import com.annimon.stream.Optional;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.ListenableFuture;

import org.ccci.gto.android.common.support.v4.util.FragmentUtils;
import org.ccci.gto.android.common.util.MainThreadExecutor;
import org.cru.godtools.article.R;
import org.cru.godtools.article.R2;
import org.cru.godtools.article.adapter.ArticlesAdapter;
import org.cru.godtools.article.databinding.FragmentArticlesBinding;
import org.cru.godtools.articles.aem.db.ArticleRoomDatabase;
import org.cru.godtools.articles.aem.model.Article;
import org.cru.godtools.articles.aem.service.AEMDownloadManger;
import org.cru.godtools.base.tool.fragment.BaseToolFragment;
import org.cru.godtools.xml.model.Category;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import butterknife.BindView;
import timber.log.Timber;

import static org.cru.godtools.article.Constants.EXTRA_CATEGORY;

public class ArticlesFragment extends BaseToolFragment implements ArticlesAdapter.Callbacks,
        SwipeRefreshLayout.OnRefreshListener {

    public interface Callbacks {
        void onArticleSelected(@Nullable Article article);
    }

    @Nullable
    FragmentArticlesBinding mBinding;
    @Nullable
    @BindView(R2.id.articles)
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

    @Nullable
    private LiveData<List<Article>> mArticles;

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
        setupDataBinding(view);
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
        updateDataBindingManifest();
        updateArticlesViewManifest();
        updateArticlesLiveData();
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
        cleanupDataBinding();
        super.onDestroyView();
    }

    // endregion LifeCycle Events

    // region ViewModel methods

    private void setupViewModel() {
        mViewModel = ViewModelProviders.of(this).get(ArticleListViewModel.class);
        updateArticlesLiveData();
    }

    private void updateArticlesLiveData() {
        final LiveData<List<Article>> articles;
        if (mCategory != null) {
            // lookup AEM tags from the manifest category
            final Set<String> tags = Optional.ofNullable(mManifest)
                    .flatMap(m -> m.findCategory(mCategory))
                    .map(Category::getAemTags)
                    .orElseGet(ImmutableSet::of);
            articles = mViewModel.getArticlesForTags(mTool, mLocale, tags);
        } else {
            // no category, so show all articles for this tool
            articles = mViewModel.getArticles(mTool, mLocale);
        }

        if (articles != mArticles) {
            if (mArticles != null) {
                mArticles.removeObservers(this);
            }
            mArticles = articles;
            updateArticlesViewArticles();
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
        ListenableFuture<?> future = manger.enqueueSyncManifestAemImports(mManifest, true);
        future.addListener(() -> setRefreshing(false), new MainThreadExecutor());
    }

    //endregion refresh Layout

    // endregion ViewModel methods

    // region View Logic

    // region Data Binding

    private void setupDataBinding(@NonNull final View view) {
        mBinding = FragmentArticlesBinding.bind(view);
        updateDataBindingManifest();
    }

    private void updateDataBindingManifest() {
        if (mBinding != null) {
            mBinding.setManifest(mManifest);
        }
    }

    private void cleanupDataBinding() {
        mBinding = null;
    }

    // endregion Data Binding


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
            mArticlesView.setAdapter(mArticlesAdapter);

            updateArticlesViewManifest();
            updateArticlesViewArticles();
        }
    }

    private void updateArticlesViewManifest() {
        if (mArticlesAdapter != null) {
            mArticlesAdapter.setToolManifest(mManifest);
        }
    }

    private void updateArticlesViewArticles() {
        if (mArticlesAdapter != null && mArticles != null) {
            mArticles.observe(this, mArticlesAdapter);
        }
    }

    private void cleanupArticlesView() {
        if (mArticlesAdapter != null) {
            mArticlesAdapter.setCallbacks(null);
            if (mArticles != null) {
                mArticles.removeObserver(mArticlesAdapter);
            }
        }
        mArticlesAdapter = null;
    }

    // endregion ArticlesView

    // endregion View Logic

    public static class ArticleListViewModel extends AndroidViewModel {
        private final ArticleRoomDatabase mAemDb;

        @Nullable
        private String mTool;
        @Nullable
        private Locale mLocale;
        @Nullable
        private Set<String> mTags;
        @Nullable
        private LiveData<List<Article>> mArticles;

        public ArticleListViewModel(@NonNull final Application application) {
            super(application);
            mAemDb = ArticleRoomDatabase.getInstance(application);
        }

        LiveData<List<Article>> getArticles(@NonNull final String tool, @NonNull final Locale locale) {
            // re-initialize the articles LiveData if necessary
            if (isArticlesLiveDataStale(tool, locale, null)) {
                mTool = tool;
                mLocale = locale;
                mTags = null;
                mArticles = mAemDb.articleDao().getArticles(tool, locale);
            }

            return mArticles;
        }

        LiveData<List<Article>> getArticlesForTags(@NonNull final String tool, @NonNull final Locale locale,
                                                   @NonNull final Set<String> tags) {
            // re-initialize the articles LiveData if necessary
            if (isArticlesLiveDataStale(tool, locale, tags)) {
                mTool = tool;
                mLocale = locale;
                mTags = tags;
                mArticles = mAemDb.articleDao().getArticles(tool, locale, ImmutableList.copyOf(tags));
            }

            return mArticles;
        }

        private boolean isArticlesLiveDataStale(@NonNull final String tool, @NonNull final Locale locale,
                                                @Nullable final Set<String> tags) {
            return mArticles == null ||
                    !Objects.equal(mTool, tool) ||
                    !Objects.equal(mLocale, locale) ||
                    !Objects.equal(tags, mTags);
        }
    }
}
