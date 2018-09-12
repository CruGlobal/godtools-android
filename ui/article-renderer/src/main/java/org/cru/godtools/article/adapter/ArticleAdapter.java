package org.cru.godtools.article.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.cru.godtools.article.R;
import org.cru.godtools.articles.aem.model.Article;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ArticleAdapter extends RecyclerView.Adapter<ArticleAdapter.ArticleViewHolder> {

    private final LayoutInflater mInflater;
    private List<Article> mArticles;
    private Callback mCallback;

    /**
     *  This constructor is used to create the layout inflater
     * @param context the context that the layout will use
     */
    public ArticleAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
        if (context instanceof Callback){
            mCallback = (Callback) context;
        } else {
            throw new IllegalArgumentException("Callback not integrated");
        }
    }

    @NonNull
    @Override
    public ArticleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.list_item_article, parent, false);
        return new ArticleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ArticleViewHolder holder, int position) {
        if (mArticles != null && mArticles.size() != 0){
            Article article = mArticles.get(position);
            holder.mTitleTextView.setText(article.mTitle);
            String updatedString = String.format("Updated: %s",
                    new SimpleDateFormat("MM/DD/YYYY", Locale.getDefault())
                            .format(new Date(article.mDateUpdated)));
            holder.mUpdatedTextView.setText(updatedString);

        } else {
            holder.mTitleTextView.setText("No Article in Category"); // Todo: Convert to String Res
        }
    }

    @Override
    public int getItemCount() {
        if (mArticles != null){
            return mArticles.size();
        } else {
            return 0;
        }
    }


    class ArticleViewHolder extends RecyclerView.ViewHolder{
        private TextView mTitleTextView;
        private TextView mUpdatedTextView;
        private Article mArticle;

        private ArticleViewHolder(View itemView) {
            super(itemView);
            mTitleTextView = itemView.findViewById(R.id.list_item_title_text);
            mUpdatedTextView = itemView.findViewById(R.id.list_item_updated_text);
            itemView.setOnClickListener(v -> mCallback.onArticleSelected(mArticle));
        }

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

    public interface Callback{
        void onArticleSelected(Article article);
    }
}
