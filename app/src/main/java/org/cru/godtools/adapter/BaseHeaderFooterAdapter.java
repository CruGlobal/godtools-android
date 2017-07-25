package org.cru.godtools.adapter;

import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.h6ah4i.android.widget.advrecyclerview.headerfooter.AbstractHeaderFooterWrapperAdapter;

import org.keynote.godtools.android.R;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.Optional;

import static org.ccci.gto.android.common.base.Constants.INVALID_DRAWABLE_RES;
import static org.ccci.gto.android.common.base.Constants.INVALID_STRING_RES;

public abstract class BaseHeaderFooterAdapter
        extends AbstractHeaderFooterWrapperAdapter<BaseViewHolder, BaseViewHolder> {
    public interface EmptyCallbacks {
        void onEmptyActionClick();
    }

    private static final int TYPE_SHOW_NONE_FOOTER = 1;

    @DrawableRes
    int mEmptyIcon = INVALID_DRAWABLE_RES;
    @StringRes
    int mEmptyLabel = INVALID_STRING_RES;
    @StringRes
    int mEmptyAction = INVALID_STRING_RES;
    private boolean mShowEmptyFooter = false;

    @Nullable
    EmptyCallbacks mEmptyCallbacks;

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

    @Override
    public int getHeaderItemCount() {
        return 0;
    }

    @Override
    public BaseViewHolder onCreateHeaderItemViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        throw new UnsupportedOperationException("onCreateHeaderItemViewHolder not supported");
    }

    @Override
    public void onBindHeaderItemViewHolder(@NonNull final BaseViewHolder holder, final int localPosition) {
        holder.bind(localPosition);
    }

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
        @BindView(R.id.action)
        TextView mAction;

        EmptyStaticViewHolder(@NonNull final ViewGroup parent) {
            super(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_none, parent, false));
        }

        @Override
        void bind(final int position) {
            if (mIcon != null) {
                mIcon.setImageResource(mEmptyIcon);
            }
            if (mLabel != null) {
                mLabel.setText(mEmptyLabel);
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
