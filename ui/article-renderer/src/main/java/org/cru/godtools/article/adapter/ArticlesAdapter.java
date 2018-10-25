package org.cru.godtools.article.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.ccci.gto.android.common.recyclerview.adapter.SimpleDataBindingAdapter;
import org.cru.godtools.article.databinding.ListItemArticleBinding;
import org.cru.godtools.articles.aem.model.Article;
import org.cru.godtools.xml.model.Manifest;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;

public class ArticlesAdapter extends SimpleDataBindingAdapter<ListItemArticleBinding>
        implements Observer<List<Article>> {
    public interface Callbacks {
        /**
         * This method will return the select Article Object in the List.
         *
         * @param article the selected Article
         */
        void onArticleSelected(@Nullable Article article);
    }

    @Nullable
    private List<Article> mArticles;
    @Nullable
    private Callbacks mCallback;
    @Nullable
    private Manifest mManifest;

    // region LifeCycle Events

    @Override
    public void onChanged(@Nullable final List<Article> articles) {
        setArticles(articles);
    }

    @NonNull
    @Override
    protected ListItemArticleBinding onCreateViewDataBinding(@NonNull final ViewGroup parent, final int viewType) {
        return ListItemArticleBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
    }

    @Override
    protected void onBindViewDataBinding(@NonNull final ListItemArticleBinding binding, final int position) {
        assert mArticles != null : "Article must be defined to bind";
        binding.setCallbacks(mCallback);
        binding.setArticle(mArticles.get(position));
        binding.setManifest(mManifest);
    }

    @Override
    protected void onViewDataBindingRecycled(@NonNull final ListItemArticleBinding binding) {
        binding.setArticle(null);
        binding.setCallbacks(null);
        binding.setManifest(null);
    }

    // endregion LifeCycle Events

    @Override
    public int getItemCount() {
        return mArticles != null ? mArticles.size() : 0;
    }

    // region Setters

    /**
     * This method initialized the Callback interface used on the Item click event.
     *
     * @param callbacks this interface used for this click event
     */
    public void setCallbacks(@Nullable final Callbacks callbacks) {
        mCallback = callbacks;
        notifyItemRangeChanged(0, getItemCount());
    }

    /**
     * This method initializes the manifest used for data on each item in this adapter.
     *
     * @param manifest the Manifest Object to be used.
     */
    public void setToolManifest(@Nullable final Manifest manifest) {
        mManifest = manifest;
        notifyItemRangeChanged(0, getItemCount());
    }

    /**
     * This method is called to set the list of articles.  Used by Live Data to update list
     *
     * @param articles list of Articles
     */
    public void setArticles(@Nullable final List<Article> articles) {
        mArticles = articles;
        notifyDataSetChanged();
    }

    // endregion Setters
}
