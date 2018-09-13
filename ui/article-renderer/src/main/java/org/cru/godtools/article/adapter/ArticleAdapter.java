package org.cru.godtools.article.adapter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import org.cru.godtools.article.databinding.ListItemArticleBinding;
import org.ccci.gto.android.common.recyclerview.adapter.SimpleDataBindingAdapter;
import org.cru.godtools.articles.aem.model.Article;

import java.util.List;

public class ArticleAdapter extends SimpleDataBindingAdapter<ListItemArticleBinding> {
    @Nullable
    private List<Article> mArticles;
    @Nullable
    private Callback mCallback;

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
    }

    public void setCallbacks(Callback callbacks) {
        this.mCallback = callbacks;
        notifyItemRangeChanged(0, getItemCount());
    }

    @Override
    protected void onViewDataBindingRecycled(@NonNull ListItemArticleBinding binding) {
        super.onViewDataBindingRecycled(binding);
        binding.setArticle(null);
        binding.setCallback(null);
    }

    @Override
    public int getItemCount() {
        return mArticles != null? mArticles.size() : 0;
    }


    public interface Callback{
        void onArticleSelected(Article article);
    }

    /**
     * This method is called to set the list of articles.  Used by Live Data to update list
     *
     * @param articles list of Articles
     */
    public void setArticles(List<Article> articles){
        mArticles = articles;
        notifyDataSetChanged();
    }
}
