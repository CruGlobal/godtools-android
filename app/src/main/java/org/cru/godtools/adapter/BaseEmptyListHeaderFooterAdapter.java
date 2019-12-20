package org.cru.godtools.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.h6ah4i.android.widget.advrecyclerview.headerfooter.AbstractHeaderFooterWrapperAdapter;

import org.cru.godtools.R;

import androidx.annotation.DrawableRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.OnClick;
import butterknife.Optional;

import static org.ccci.gto.android.common.base.Constants.INVALID_DRAWABLE_RES;
import static org.ccci.gto.android.common.base.Constants.INVALID_STRING_RES;

public abstract class BaseEmptyListHeaderFooterAdapter
        extends AbstractHeaderFooterWrapperAdapter<RecyclerView.ViewHolder, BaseViewHolder> {
    public abstract static class Builder<T extends Builder> {
        @LayoutRes
        int mLayout = R.layout.list_item_none;
        @DrawableRes
        int mEmptyIcon = INVALID_DRAWABLE_RES;
        @StringRes
        int mEmptyLabel = INVALID_STRING_RES;
        @StringRes
        int mEmptyText = INVALID_STRING_RES;
        @StringRes
        int mEmptyAction = INVALID_STRING_RES;

        @SuppressWarnings("unchecked")
        protected T self() {
            return (T) this;
        }

        public T layout(@LayoutRes final int layout) {
            mLayout = layout;
            return self();
        }

        public T emptyIcon(@DrawableRes final int icon) {
            mEmptyIcon = icon;
            return self();
        }

        public T emptyLabel(@StringRes final int label) {
            mEmptyLabel = label;
            return self();
        }

        public T emptyText(@StringRes final int text) {
            mEmptyText = text;
            return self();
        }

        public T emptyAction(@StringRes final int action) {
            mEmptyAction = action;
            return self();
        }
    }

    public interface EmptyCallbacks {
        void onEmptyActionClick();
    }

    private static final int TYPE_SHOW_NONE_FOOTER = 1;

    @LayoutRes
    final int mLayout;
    @DrawableRes
    final int mEmptyIcon;
    @StringRes
    final int mEmptyLabel;
    @StringRes
    final int mEmptyText;
    @StringRes
    final int mEmptyAction;
    private boolean mShowEmptyFooter = false;

    @Nullable
    EmptyCallbacks mEmptyCallbacks;

    protected BaseEmptyListHeaderFooterAdapter(@NonNull final Builder builder) {
        mLayout = builder.mLayout;
        mEmptyIcon = builder.mEmptyIcon;
        mEmptyLabel = builder.mEmptyLabel;
        mEmptyText = builder.mEmptyText;
        mEmptyAction = builder.mEmptyAction;
    }

    public void setEmptyCallbacks(@Nullable EmptyCallbacks callbacks) {
        mEmptyCallbacks = callbacks;
    }

    public final void setShowEmptyFooter(final boolean state) {
        final boolean different = mShowEmptyFooter != state;
        mShowEmptyFooter = state;
        if (different) {
            getFooterAdapter().notifyDataSetChanged();
        }
    }

    // region Header

    @Override
    public int getHeaderItemCount() {
        return 0;
    }

    @Override
    public RecyclerView.ViewHolder onCreateHeaderItemViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        throw new UnsupportedOperationException("onCreateHeaderItemViewHolder not supported");
    }

    @Override
    public void onBindHeaderItemViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int localPosition) { }

    // endregion Header

    @Override
    public int getFooterItemCount() {
        return mShowEmptyFooter ? 1 : 0;
    }

    @Override
    public long getFooterItemId(final int localPosition) {
        if (mShowEmptyFooter && localPosition == 0) {
            return 1;
        }
        return RecyclerView.NO_ID;
    }

    @Override
    public int getFooterItemViewType(final int localPosition) {
        if (mShowEmptyFooter && localPosition == 0) {
            return TYPE_SHOW_NONE_FOOTER;
        }
        return super.getFooterItemViewType(localPosition);
    }

    @Override
    public BaseViewHolder onCreateFooterItemViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        switch (viewType) {
            case TYPE_SHOW_NONE_FOOTER:
                return new EmptyStaticViewHolder(parent);
        }

        return null;
    }

    @Override
    public void onBindFooterItemViewHolder(@NonNull final BaseViewHolder holder, final int localPosition) {
        holder.bind(localPosition);
    }

    final class EmptyStaticViewHolder extends BaseViewHolder {
        @Nullable
        @BindView(R.id.icon)
        ImageView mIcon;
        @Nullable
        @BindView(R.id.label)
        TextView mLabel;
        @Nullable
        @BindView(R.id.text)
        TextView mText;
        @Nullable
        @BindView(R.id.action)
        TextView mAction;

        EmptyStaticViewHolder(@NonNull final ViewGroup parent) {
            super(LayoutInflater.from(parent.getContext()).inflate(mLayout, parent, false));
        }

        @Override
        protected void bind(final int position) {
            if (mIcon != null) {
                if (mEmptyIcon != INVALID_DRAWABLE_RES) {
                    mIcon.setVisibility(View.VISIBLE);
                    mIcon.setImageResource(mEmptyIcon);
                } else {
                    mIcon.setVisibility(View.GONE);
                }
            }
            if (mLabel != null) {
                if (mEmptyLabel != INVALID_STRING_RES) {
                    mLabel.setVisibility(View.VISIBLE);
                    mLabel.setText(mEmptyLabel);
                } else {
                    mLabel.setVisibility(View.GONE);
                }
            }
            if (mText != null) {
                if (mEmptyText != INVALID_STRING_RES) {
                    mText.setVisibility(View.VISIBLE);
                    mText.setText(mEmptyText);
                } else {
                    mText.setVisibility(View.GONE);
                }
            }
            if (mAction != null) {
                if (mEmptyAction != INVALID_STRING_RES) {
                    mAction.setVisibility(View.VISIBLE);
                    mAction.setText(mEmptyAction);
                } else {
                    mAction.setVisibility(View.GONE);
                }
            }
        }

        @Optional
        @OnClick(R.id.action)
        void onClick() {
            if (mEmptyCallbacks != null) {
                mEmptyCallbacks.onEmptyActionClick();
            }
        }
    }
}
