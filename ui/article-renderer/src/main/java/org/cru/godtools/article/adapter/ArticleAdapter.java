package org.cru.godtools.article.adapter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import org.cru.godtools.article.databinding.ListItemArticleBinding;
import org.ccci.gto.android.common.recyclerview.adapter.SimpleDataBindingAdapter;
import org.cru.godtools.articles.aem.model.Article;
import org.cru.godtools.xml.model.Manifest;

import java.util.List;

public class ArticleAdapter extends SimpleDataBindingAdapter<ListItemArticleBinding> {
    @Nullable
    private List<Article> mArticles;
    @Nullable
    private Callback mCallback;
    @Nullable
    private Manifest mManifest;

    //region Setters

    /**
     * This method initialized the Callback interface used on the Item click event.
     *
     * @param callbacks this interface used for this click event
     */
    public void setCallbacks(Callback callbacks) {
        this.mCallback = callbacks;
        notifyItemRangeChanged(0, getItemCount());
    }

    /**
     *  This method initializes the manifest used for data on each item in this adapter.
     *
     * @param manifest the Manifest Object to be used.
     */
    public void setToolManifest(Manifest manifest) {
        this.mManifest = manifest;
        notifyItemRangeChanged(0, getItemCount());
    }

    /**
     * This method is called to set the list of articles.  Used by Live Data to update list
     *
     * @param articles list of Articles
     */
    public void setArticles(List<Article> articles) {
        mArticles = articles;
        notifyDataSetChanged();
    }
    //endregion

    //region LifeCycle Methods

    @NonNull
    @Override
    protected ListItemArticleBinding onCreateViewDataBinding(@NonNull ViewGroup parent, int viewType) {
        return ListItemArticleBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
    }

    @Override
    protected void onBindViewDataBinding(ListItemArticleBinding binding, int position) {
        assert mArticles != null : "Article must be defined to bind";
        binding.setCallback(mCallback);
        binding.setArticle(mArticles.get(position));
        binding.setManifest(mManifest);
    }

    @Override
    protected void onViewDataBindingRecycled(@NonNull ListItemArticleBinding binding) {
        super.onViewDataBindingRecycled(binding);
        binding.setArticle(null);
        binding.setCallback(null);
    }

    //endregion

    @Override
    public int getItemCount() {
        return mArticles != null ? mArticles.size() : 0;
    }

    public interface Callback {
        /**
         * This method will return the select Article Object in the List.
         *
         * @param article the selected Article
         */
        void onArticleSelected(Article article);
    }


}
