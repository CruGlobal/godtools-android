package org.keynote.godtools.android.adapter;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.ccci.gto.android.common.db.util.CursorUtils;
import org.ccci.gto.android.common.picasso.view.PicassoImageView;
import org.ccci.gto.android.common.recyclerview.adapter.CursorAdapter;
import org.keynote.godtools.android.R;
import org.keynote.godtools.android.db.Contract.ToolTable;
import org.keynote.godtools.android.model.Tool;
import org.keynote.godtools.android.util.ViewUtils;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Optional;

import static org.keynote.godtools.android.util.ViewUtils.bindShares;

public class ToolsAdapter extends CursorAdapter<ToolsAdapter.ToolViewHolder> {
    public static final String COL_TITLE = "title";
    public static final String COL_BANNER = "banner";
    public static final String COL_PRIMARY_LANGUAGE = "primary_language";
    public static final String COL_PARALLEL_LANGUAGE = "parallel_language";
    public static final String COL_DEFAULT_LANGUAGE = "default_language";
    public interface Callbacks {
        void onToolInfo(long id, @Nullable String code);

        void onToolSelect(long id, @Nullable String code, @NonNull Tool.Type type, Locale... languages);

        void onToolAdd(long id, @Nullable String code);
    }

    final boolean mHideAddAction;

    @Nullable
    Callbacks mCallbacks;

    public ToolsAdapter(final boolean hideAddAction) {
        mHideAddAction = hideAddAction;
    }

    public void setCallbacks(@Nullable final Callbacks callbacks) {
        mCallbacks = callbacks;
    }

    @Override
    public ToolViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        return new ToolViewHolder(LayoutInflater.from(parent.getContext())
                                          .inflate(R.layout.list_item_tool_card, parent, false));
    }

    @Override
    protected void onBindViewHolder(@NonNull final ToolViewHolder holder, @Nullable final Cursor cursor,
                                    final int position) {
        holder.bind(cursor);
    }

    class ToolViewHolder extends BaseViewHolder {
        @Nullable
        @BindView(R.id.banner)
        PicassoImageView mBanner;
        @Nullable
        @BindView(R.id.title)
        TextView mTitleView;
        @Nullable
        @BindView(R.id.shares)
        TextView mSharesView;
        @Nullable
        @BindView(R.id.language_parallel)
        TextView mParallelLanguageView;
        @Nullable
        @BindView(R.id.download_progress)
        ProgressBar mDownloadProgress;
        @Nullable
        @BindView(R.id.action_add)
        View mActionAdd;
        @Nullable
        @BindViews({R.id.action_add, R.id.divider_download})
        List<View> mAddViews;

        long mId;
        @Nullable
        String mCode;
        @NonNull
        Tool.Type mType = Tool.Type.DEFAULT;
        @Nullable
        String mTitle;
        @Nullable
        String mBannerFile;
        @Nullable
        Locale mPrimaryLanguage;
        @Nullable
        Locale mParallelLanguage;
        @Nullable
        Locale mDefaultLanguage;
        int mShares = 0;
        boolean mAdded = false;
        boolean mDownloading = false;
        boolean mDownloaded = true;

        ToolViewHolder(@NonNull final View view) {
            super(view);
            if (mAddViews != null) {
                ButterKnife.apply(mAddViews, (ButterKnife.Action<View>) (v, i) -> v
                        .setVisibility(mHideAddAction ? View.GONE : View.VISIBLE));
            }
        }

        void bind(@Nullable final Cursor cursor) {
            // update data from Cursor
            if (cursor != null) {
                mId = CursorUtils.getLong(cursor, ToolTable.COLUMN_ID, Tool.INVALID_ID);
                mCode = CursorUtils.getString(cursor, ToolTable.COLUMN_CODE, Tool.INVALID_CODE);
                mType = CursorUtils.getEnum(cursor, ToolTable.COLUMN_TYPE, Tool.Type.class, Tool.Type.DEFAULT);
                mTitle = CursorUtils.getString(cursor, COL_TITLE, null);
                mBannerFile = CursorUtils.getString(cursor, COL_BANNER, null);
                mPrimaryLanguage = CursorUtils.getLocale(cursor, COL_PRIMARY_LANGUAGE, null);
                mDefaultLanguage = CursorUtils.getLocale(cursor, COL_DEFAULT_LANGUAGE, null);
                mParallelLanguage = CursorUtils.getLocale(cursor, COL_PARALLEL_LANGUAGE, null);
                mAdded = CursorUtils.getBool(cursor, ToolTable.COLUMN_ADDED, false);
                mShares = CursorUtils.getInt(cursor, ToolTable.COLUMN_SHARES, 0) +
                        CursorUtils.getInt(cursor, ToolTable.COLUMN_PENDING_SHARES, 0);
            } else {
                mId = Tool.INVALID_ID;
                mCode = Tool.INVALID_CODE;
                mType = Tool.Type.DEFAULT;
                mTitle = null;
                mBannerFile = null;
                mPrimaryLanguage = mDefaultLanguage = mParallelLanguage = null;
                mShares = 0;
                mAdded = false;
                mDownloaded = false;
                mDownloading = false;
            }

            // update any bound views
            ViewUtils.bindLocalImage(mBanner, mBannerFile);
            if (mTitleView != null) {
                mTitleView.setText(mTitle);
            }
            bindShares(mSharesView, mShares);
            if (mParallelLanguageView != null) {
                if (mParallelLanguage != null) {
                    mParallelLanguageView.setVisibility(View.VISIBLE);
                    mParallelLanguageView.setText(mParallelLanguage.getDisplayName());
                } else {
                    mParallelLanguageView.setVisibility(View.GONE);
                    mParallelLanguageView.setText(null);
                }
            }
            if (mActionAdd != null) {
                mActionAdd.setEnabled(!mAdded);
            }
            if (mDownloadProgress != null) {
                mDownloadProgress.setVisibility(mAdded && (mDownloading || !mDownloaded) ? View.VISIBLE : View.GONE);
            }
        }

        @Optional
        @OnClick(R.id.root)
        void select() {
            if (mCallbacks != null) {
                if (mPrimaryLanguage != null) {
                    mCallbacks.onToolSelect(mId, mCode, mType, mPrimaryLanguage, mParallelLanguage);
                } else if (mDefaultLanguage != null) {
                    mCallbacks.onToolSelect(mId, mCode, mType, mDefaultLanguage, mParallelLanguage);
                } else if (mParallelLanguage != null) {
                    mCallbacks.onToolSelect(mId, mCode, mType, mParallelLanguage);
                } else if (mType == Tool.Type.ARTICLE && mId == 5) {
                    // everystudent content for now
                    mCallbacks.onToolSelect(mId, mCode, mType);
                } else {
                    // do nothing
                }
            }
        }

        @Optional
        @OnClick(R.id.action_add)
        void add() {
            if (mCallbacks != null) {
                mCallbacks.onToolAdd(mId, mCode);
            }
        }

        @Optional
        @OnClick(R.id.action_info)
        void info() {
            if (mCallbacks != null) {
                mCallbacks.onToolInfo(mId, mCode);
            }
        }
    }
}
