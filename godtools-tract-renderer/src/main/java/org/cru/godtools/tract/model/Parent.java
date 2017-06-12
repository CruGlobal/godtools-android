package org.cru.godtools.tract.model;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.common.collect.ImmutableList;

import org.cru.godtools.tract.R2;
import org.cru.godtools.tract.model.Base.BaseViewHolder;

import java.util.List;

import butterknife.BindView;

interface Parent {
    @NonNull
    List<Content> getContent();

    @UiThread
    abstract class ParentViewHolder<T extends Base & Parent> extends BaseViewHolder<T> {
        @Nullable
        @BindView(R2.id.content)
        LinearLayout mContent;

        ParentViewHolder(@NonNull final Class<T> modelType, @NonNull final ViewGroup parent, final int layout,
                         @Nullable final ParentViewHolder<?> parentViewHolder) {
            super(modelType, parent, layout, parentViewHolder);
        }

        @Override
        @CallSuper
        void onBind() {
            super.onBind();
            bindContent();
        }

        private void bindContent() {
            if (mContent != null) {
                final List<Content> content = mModel != null ? mModel.getContent() : ImmutableList.of();
                mContent.removeAllViews();

                for (final Content item : content) {
                    final BaseViewHolder holder = item.createViewHolder(mContent, this);
                    //noinspection unchecked
                    holder.bind(item);
                    mContent.addView(holder.mRoot);
                }
            }
        }
    }
}
