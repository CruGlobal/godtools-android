package org.cru.godtools.article.fragment;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.cru.godtools.article.R;
import org.cru.godtools.article.R2;
import org.cru.godtools.article.adapter.ArticleAdapter;
import org.cru.godtools.articles.aem.db.ManifestAssociationRepository;
import org.cru.godtools.articles.aem.model.Article;
import org.cru.godtools.base.tool.fragment.BaseToolFragment;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

import butterknife.BindView;
import timber.log.Timber;

public class ArticlesFragment extends BaseToolFragment implements ArticleAdapter.Callback {
    public static final String TAG = "ArticlesFragment";
    private static final String MANIFEST_KEY = "manifest-key";

    @Nullable
    @BindView(R2.id.articles_recycler_view)
    RecyclerView mArticlesView;
    @Nullable
    ArticleAdapter mArticlesAdapter;

    String mManifestKey = "";

    @NonNull
    private /*final*/ ArticleListViewModel mViewModel;

    public static ArticlesFragment newInstance(@NonNull final String code, @NonNull final Locale locale,
                                               String manifestKey) {
        ArticlesFragment fragment = new ArticlesFragment();
        Bundle args = new Bundle();
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

        mViewModel = ViewModelProviders.of(this).get(ArticleListViewModel.class);
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

    @Override
    public void onDestroyView() {
        cleanupArticlesView();
        super.onDestroyView();
    }

    // endregion LifeCycle Events

    // region ArticlesView

    /**
     * This method will initialize all of the required data for the RecyclerView
     */
    private void setupArticlesView() {
        if (mArticlesView != null) {
            mArticlesAdapter = new ArticleAdapter();
            mArticlesAdapter.setCallbacks(this);
            mArticlesAdapter.setToolManifest(mManifest);
            DividerItemDecoration itemDecoration = new DividerItemDecoration(getContext(),
                    DividerItemDecoration.VERTICAL);
            itemDecoration.setDrawable(Objects.requireNonNull(getActivity()).getResources()
                    .getDrawable(R.drawable.divider));

            mArticlesView.addItemDecoration(itemDecoration);
            mArticlesView.setAdapter(mArticlesAdapter);

            mViewModel.getArticles(mManifestKey).observe(this, articles -> {
                if (mArticlesAdapter != null) {
                    mArticlesAdapter.setArticles(articles);
                }
            });
        }
    }

    private void cleanupArticlesView() {
        if (mArticlesAdapter != null) {
            mArticlesAdapter.setCallbacks(null);
        }
        mArticlesAdapter = null;
    }

    // endregion ArticlesView

    /**
     * This is the Override method from ArticleAdapter that will handle the functionality of an article
     * being selected from the list.
     *
     * @param article the selected Article
     */
    @Override
    public void onArticleSelected(Article article) {
        Timber.tag(TAG).d("You selected \"%s\" as your article", article.mTitle);
    }

    /**
     * This is the Override Method from BaseToolFragment.  This is fired anytime the Manifest Object is
     * updated and will update the adapter of it's changes.
     */
    @Override
    protected void onManifestUpdated() {
        super.onManifestUpdated();
        mArticlesAdapter.setToolManifest(mManifest);
    }

    static class ArticleListViewModel extends AndroidViewModel {
        @Nullable
        private LiveData<List<Article>> mArticles;

        public ArticleListViewModel(@NonNull final Application application) {
            super(application);
        }

        @NonNull
        @MainThread
        LiveData<List<Article>> getArticles(@Nullable final String manifestKey) {
            if (mArticles == null) {
                mArticles = new ManifestAssociationRepository(getApplication()).getArticlesByManifestID(manifestKey);
            }

            return mArticles;
        }
    }
}
