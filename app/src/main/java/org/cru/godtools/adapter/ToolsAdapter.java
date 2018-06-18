package org.cru.godtools.adapter;

import android.database.Cursor;
import android.graphics.Typeface;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.text.TextUtilsCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemViewHolder;
import com.h6ah4i.android.widget.advrecyclerview.draggable.ItemDraggableRange;
import com.h6ah4i.android.widget.advrecyclerview.draggable.annotation.DraggableItemStateFlags;

import org.ccci.gto.android.common.compat.view.TextViewCompat;
import org.ccci.gto.android.common.db.util.CursorUtils;
import org.ccci.gto.android.common.picasso.view.PicassoImageView;
import org.ccci.gto.android.common.recyclerview.adapter.CursorAdapter;
import org.cru.godtools.R;
import org.cru.godtools.base.ui.util.LocaleTypefaceUtils;
import org.cru.godtools.download.manager.DownloadProgress;
import org.cru.godtools.download.manager.GodToolsDownloadManager;
import org.cru.godtools.model.Tool;
import org.keynote.godtools.android.db.Contract.ToolTable;
import org.keynote.godtools.android.util.ViewUtils;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Optional;

import static android.view.HapticFeedbackConstants.LONG_PRESS;
import static org.cru.godtools.download.manager.util.ViewUtils.bindDownloadProgress;
import static org.keynote.godtools.android.util.ViewUtils.bindShares;

public class ToolsAdapter extends CursorAdapter<ToolsAdapter.ToolViewHolder>
        implements DraggableItemAdapter<ToolsAdapter.ToolViewHolder> {
    public static final String COL_TITLE = "title";
    public static final String COL_TITLE_LANGUAGE = "title_lang";
    public static final String COL_TAGLINE = "tagline";
    public static final String COL_TAGLINE_LANGUAGE = "tagline_lang";
    public static final String COL_BANNER = "banner";
    public static final String COL_PRIMARY_LANGUAGE = "primary_language";
    public static final String COL_PARALLEL_LANGUAGE = "parallel_language";
    public static final String COL_DEFAULT_LANGUAGE = "default_language";
    public interface Callbacks {
        void onToolInfo(@Nullable String code);

        void onToolSelect(@Nullable String code, @NonNull Tool.Type type, Locale... languages);

        void onToolAdd(@Nullable String code);

        void onToolsReordered(long... ids);
    }

    @Nullable
    private RecyclerView mRecyclerView;

    @NonNull
    private int[] mTmpPositions = new int[0];
    final boolean mHideAddAction;

    @Nullable
    Callbacks mCallbacks;

    public ToolsAdapter(final boolean hideAddAction) {
        mHideAddAction = hideAddAction;
    }

    public void setCallbacks(@Nullable final Callbacks callbacks) {
        mCallbacks = callbacks;
    }

    /* BEGIN lifecycle */

    @Override
    public void onAttachedToRecyclerView(@NonNull final RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerView = recyclerView;
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

    @Override
    public void onViewAttachedToWindow(final ToolViewHolder holder) {
        holder.startDownloadProgressListener();
    }

    @Override
    public boolean onCheckCanStartDrag(final ToolViewHolder holder, final int position, final int x, final int y) {
        return true;
    }

    @Override
    public void onItemDragStarted(final int position) {
        // perform haptic feedback
        if (mRecyclerView != null) {
            mRecyclerView.performHapticFeedback(LONG_PRESS);
        }
    }

    @Override
    public void onItemDragFinished(final int fromPosition, final int toPosition, final boolean result) {}

    @Override
    public ItemDraggableRange onGetItemDraggableRange(final ToolViewHolder holder, final int position) {
        return null;
    }

    @Override
    public void onMoveItem(final int fromPosition, final int toPosition) {
        updateTmpPositions(fromPosition, toPosition);
        triggerToolOrderUpdate();
    }

    @Override
    public boolean onCheckCanDrop(final int draggingPosition, final int dropPosition) {
        return true;
    }

    @Override
    public void onViewDetachedFromWindow(final ToolViewHolder holder) {
        holder.stopDownloadProgressListener();
    }

    @Override
    public void onViewRecycled(final ToolViewHolder holder) {
        holder.bind(null);
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull final RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        if (mRecyclerView == recyclerView) {
            mRecyclerView = null;
        }
    }

    /* END lifecycle */

    @Nullable
    @Override
    public Cursor swapCursor(@Nullable final Cursor cursor) {
        mTmpPositions = new int[cursor != null ? cursor.getCount() : 0];
        for (int i = 0; i < mTmpPositions.length; i++) {
            mTmpPositions[i] = i;
        }

        return super.swapCursor(cursor);
    }

    @Override
    protected Cursor scrollCursor(@Nullable final Cursor cursor, final int position) {
        return super.scrollCursor(cursor, mTmpPositions[position]);
    }

    private void updateTmpPositions(final int fromPosition, final int toPosition) {
        // short-circuit if the position isn't actually changing
        if (fromPosition == toPosition) {
            return;
        }

        // short-circuit if there are invalid positions
        if (fromPosition < 0 || fromPosition >= mTmpPositions.length ||
                toPosition < 0 || toPosition >= mTmpPositions.length) {
            return;
        }

        final int tmp = mTmpPositions[fromPosition];
        if (fromPosition < toPosition) {
            // 0123F56T8 -> 0123_56T8 -> 012356T_8 -> 012356TF8
            System.arraycopy(mTmpPositions, fromPosition + 1, mTmpPositions, fromPosition, toPosition - fromPosition);
        } else {
            // 0123T56F8 -> 0123T56_8 -> 0123_T568 -> 0123FT568
            System.arraycopy(mTmpPositions, toPosition, mTmpPositions, toPosition + 1, fromPosition - toPosition);
        }
        mTmpPositions[toPosition] = tmp;
    }

    @MainThread
    private void triggerToolOrderUpdate() {
        if (mCallbacks != null) {
            final int count = getItemCount();
            final long[] ids = new long[count];
            for (int i = 0; i < count; i++) {
                ids[i] = getItemId(i);
            }

            mCallbacks.onToolsReordered(ids);
        }
    }

    class ToolViewHolder extends BaseViewHolder
            implements GodToolsDownloadManager.OnDownloadProgressUpdateListener, DraggableItemViewHolder {
        @Nullable
        @BindView(R.id.banner)
        PicassoImageView mBanner;
        @Nullable
        @BindView(R.id.title)
        TextView mTitleView;
        int mTitleTextStyle = Typeface.NORMAL;
        @Nullable
        @BindView(R.id.tagline)
        TextView mTaglineView;
        @Nullable
        @BindView(R.id.shares)
        TextView mSharesView;
        @Nullable
        @BindView(R.id.language_parallel)
        TextView mParallelLanguageView;
        @Nullable
        @BindView(R.id.download_progress)
        ProgressBar mDownloadProgressBar;
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
        Locale mTitleLanguage;
        @Nullable
        String mTagline;
        @Nullable
        Locale mTaglineLanguage;
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
        @Nullable
        private DownloadProgress mDownloadProgress;

        @DraggableItemStateFlags
        private int mDragStateFlags;

        ToolViewHolder(@NonNull final View view) {
            super(view);
            if (mAddViews != null) {
                ButterKnife.apply(mAddViews, (v, i) -> v.setVisibility(mHideAddAction ? View.GONE : View.VISIBLE));
            }
            if (mTitleView != null) {
                mTitleTextStyle = TextViewCompat.getTypefaceStyle(mTitleView);
            }
        }

        void bind(@Nullable final Cursor cursor) {
            // update data from Cursor
            if (cursor != null) {
                mId = CursorUtils.getLong(cursor, ToolTable.COLUMN_ID, Tool.INVALID_ID);
                mCode = CursorUtils.getString(cursor, ToolTable.COLUMN_CODE, Tool.INVALID_CODE);
                mType = CursorUtils.getEnum(cursor, ToolTable.COLUMN_TYPE, Tool.Type.class, Tool.Type.DEFAULT);
                mTitle = CursorUtils.getString(cursor, COL_TITLE, null);
                mTitleLanguage = CursorUtils.getLocale(cursor, COL_TITLE_LANGUAGE, null);
                mTagline = CursorUtils.getString(cursor, COL_TAGLINE, null);
                mTaglineLanguage = CursorUtils.getLocale(cursor, COL_TAGLINE_LANGUAGE, null);
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
                mTitleLanguage = null;
                mTagline = null;
                mTaglineLanguage = null;
                mBannerFile = null;
                mPrimaryLanguage = null;
                mDefaultLanguage = null;
                mParallelLanguage = null;
                mShares = 0;
                mAdded = false;
            }

            // update any bound views
            ViewCompat.setLayoutDirection(itemView, TextUtilsCompat.getLayoutDirectionFromLocale(mPrimaryLanguage));
            ViewUtils.bindLocalImage(mBanner, mBannerFile);
            if (mTitleView != null) {
                mTitleView.setText(mTitle);
                mTitleView.setTypeface(LocaleTypefaceUtils.getTypeface(mTitleView.getContext(), mTitleLanguage),
                                       mTitleTextStyle);
            }
            if (mTaglineView != null) {
                mTaglineView.setText(mTagline);
                mTaglineView.setTypeface(LocaleTypefaceUtils.getTypeface(mTaglineView.getContext(), mTaglineLanguage));
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
        }

        @Override
        public void setDragStateFlags(@DraggableItemStateFlags final int flags) {
            mDragStateFlags = flags;
        }

        @Override
        @DraggableItemStateFlags
        public int getDragStateFlags() {
            return mDragStateFlags;
        }

        void startDownloadProgressListener() {
            // start listening for new state
            if (mCode != null && mPrimaryLanguage != null) {
                final GodToolsDownloadManager downloadManager =
                        GodToolsDownloadManager.getInstance(itemView.getContext());
                downloadManager.addOnDownloadProgressUpdateListener(mCode, mPrimaryLanguage, this);
                onDownloadProgressUpdated(downloadManager.getDownloadProgress(mCode, mPrimaryLanguage));
            }
        }

        void stopDownloadProgressListener() {
            GodToolsDownloadManager.getInstance(itemView.getContext()).removeOnDownloadProgressUpdateListener(this);
            mDownloadProgress = null;
        }

        @Override
        public void onDownloadProgressUpdated(@Nullable final DownloadProgress progress) {
            mDownloadProgress = progress;
            bindDownloadProgress(mDownloadProgressBar, mDownloadProgress);
        }

        @Optional
        @OnClick(R.id.root)
        void select() {
            if (mCallbacks != null) {
                if (mPrimaryLanguage != null) {
                    mCallbacks.onToolSelect(mCode, mType, mPrimaryLanguage, mParallelLanguage);
                } else if (mDefaultLanguage != null) {
                    mCallbacks.onToolSelect(mCode, mType, mDefaultLanguage, mParallelLanguage);
                } else if (mParallelLanguage != null) {
                    mCallbacks.onToolSelect(mCode, mType, mParallelLanguage);
                } else if (mType == Tool.Type.ARTICLE && "es".equals(mCode)) {
                    // everystudent content for now
                    mCallbacks.onToolSelect(mCode, mType);
                } else {
                    // do nothing
                }
            }
        }

        @Optional
        @OnClick(R.id.action_add)
        void add() {
            if (mCallbacks != null) {
                mCallbacks.onToolAdd(mCode);
            }
        }

        @Optional
        @OnClick(R.id.action_info)
        void info() {
            if (mCallbacks != null) {
                mCallbacks.onToolInfo(mCode);
            }
        }
    }
}
