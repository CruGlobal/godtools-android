package org.keynote.godtools.android.adapter;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.ccci.gto.android.common.db.util.CursorUtils;
import org.ccci.gto.android.common.recyclerview.adapter.CursorAdapter;
import org.keynote.godtools.android.R;
import org.keynote.godtools.android.db.Contract.ResourceTable;
import org.keynote.godtools.android.model.Resource;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.Optional;

public class ResourcesAdapter extends CursorAdapter<ResourcesAdapter.ResourceViewHolder> {
    public interface Callbacks {
        void onResourceInfo(long id);

        void onResourceSelect(long id);

        void onResourceAdd(long id);
    }

    @Nullable
    Callbacks mCallbacks;

    public void setCallbacks(@Nullable final Callbacks callbacks) {
        mCallbacks = callbacks;
    }

    @Override
    public ResourceViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        return new ResourceViewHolder(LayoutInflater.from(parent.getContext())
                                              .inflate(R.layout.list_item_resource_card, parent, false));
    }

    @Override
    protected void onBindViewHolder(@NonNull final ResourceViewHolder holder, @Nullable final Cursor cursor,
                                    final int position) {
        holder.bind(cursor);
    }

    class ResourceViewHolder extends BaseViewHolder {
        @Nullable
        @BindView(R.id.banner)
        ImageView mBanner;
        @Nullable
        @BindView(R.id.title)
        TextView mTitleView;
        @Nullable
        @BindView(R.id.download_progress)
        ProgressBar mDownloadProgress;
        @Nullable
        @BindView(R.id.action_add)
        View mActionAdd;

        long mId;
        @Nullable
        String mTitle;
        boolean mAdded = false;
        boolean mDownloading = false;
        boolean mDownloaded = true;

        ResourceViewHolder(@NonNull final View view) {
            super(view);
        }

        void bind(@Nullable final Cursor cursor) {
            // update data from Cursor
            if (cursor != null) {
                mId = CursorUtils.getLong(cursor, ResourceTable.COLUMN_ID, Resource.INVALID_ID);
                mTitle = CursorUtils.getString(cursor, ResourceTable.COLUMN_NAME, null);
                mAdded = CursorUtils.getBool(cursor, ResourceTable.COLUMN_ADDED, false);
            } else {
                mId = Resource.INVALID_ID;
                mTitle = null;
                mAdded = false;
                mDownloaded = false;
                mDownloading = false;
            }

            // update any bound views
            if (mBanner != null) {
                // TODO
            }
            if (mTitleView != null) {
                mTitleView.setText(mTitle);
            }
            if (mActionAdd != null) {
                mActionAdd.setVisibility(mAdded ? View.GONE : View.VISIBLE);
            }
            if (mDownloadProgress != null) {
                mDownloadProgress.setVisibility(mAdded && (mDownloading || !mDownloaded) ? View.VISIBLE : View.GONE);
            }
        }

        @Optional
        @OnClick(R.id.root)
        void select() {
            if (mCallbacks != null) {
                mCallbacks.onResourceSelect(mId);
            }
        }

        @Optional
        @OnClick(R.id.action_add)
        void add() {
            if (mCallbacks != null) {
                mCallbacks.onResourceAdd(mId);
            }
        }

        @Optional
        @OnClick(R.id.action_info)
        void info() {
            if (mCallbacks != null) {
                mCallbacks.onResourceInfo(mId);
            }
        }
    }
}
