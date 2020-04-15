package org.cru.godtools.adapter;

import android.database.Cursor;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemState;
import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemViewHolder;
import com.h6ah4i.android.widget.advrecyclerview.draggable.ItemDraggableRange;
import com.h6ah4i.android.widget.advrecyclerview.draggable.annotation.DraggableItemStateFlags;

import org.ccci.gto.android.common.compat.view.TextViewCompat;
import org.ccci.gto.android.common.db.util.CursorUtils;
import org.cru.godtools.R;
import org.cru.godtools.base.ui.util.LocaleTypefaceUtils;
import org.cru.godtools.databinding.ToolsListItemToolBinding;
import org.cru.godtools.model.Tool;
import org.cru.godtools.ui.tools.ToolsAdapterToolViewModel;
import org.keynote.godtools.android.db.Contract.ToolTable;

import java.util.Locale;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ObservableField;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.OnClick;
import butterknife.Optional;

import static android.view.HapticFeedbackConstants.LONG_PRESS;

public class ToolsAdapter extends CursorDataBindingAdapter<ToolsListItemToolBinding, ToolsAdapter.ToolViewHolder>
        implements DraggableItemAdapter<ToolsAdapter.ToolViewHolder> {
    public static final String COL_TITLE = "title";
    public static final String COL_TITLE_LANGUAGE = "title_lang";
    public static final String COL_TAGLINE = "tagline";
    public static final String COL_TAGLINE_LANGUAGE = "tagline_lang";
    public static final String COL_PRIMARY_LANGUAGE = "primary_language";
    public static final String COL_PARALLEL_LANGUAGE = "parallel_language";
    public static final String COL_DEFAULT_LANGUAGE = "default_language";

    private static final String VIEW_MODEL_KEY_PREFIX = ToolsAdapter.class.getCanonicalName() + ":" +
            ToolsAdapterToolViewModel.class.getCanonicalName() + ":";

    public interface Callbacks {
        void onToolInfo(@Nullable String code);

        void onToolSelect(@Nullable String code, @NonNull Tool.Type type, Locale... languages);

        void onToolAdd(@Nullable String code);

        void onToolsReordered(long... ids);
    }

    @NonNull
    private final ViewModelProvider mViewModelProvider;

    @Nullable
    private RecyclerView mRecyclerView;

    @NonNull
    private int[] mTmpPositions = new int[0];

    @NonNull
    final ObservableField<Callbacks> mCallbacks = new ObservableField<>();

    public ToolsAdapter(@NonNull final LifecycleOwner lifecycleOwner, @NonNull final ViewModelProvider provider) {
        super(lifecycleOwner);
        mViewModelProvider = provider;
    }

    public void setCallbacks(@Nullable final Callbacks callbacks) {
        mCallbacks.set(callbacks);
    }

    // region Lifecycle
    @Override
    public void onAttachedToRecyclerView(@NonNull final RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerView = recyclerView;
    }

    @NonNull
    @Override
    protected ToolsListItemToolBinding onCreateViewDataBinding(@NonNull final ViewGroup parent, final int viewType) {
        final ToolsListItemToolBinding binding =
                ToolsListItemToolBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        binding.setCallbacks(mCallbacks);
        return binding;
    }

    @NonNull
    @Override
    protected ToolViewHolder onCreateViewHolder(@NonNull final ToolsListItemToolBinding binding, final int viewType) {
        return new ToolViewHolder(binding);
    }

    @Override
    protected void onBindViewHolder(@NonNull final ToolViewHolder holder, @Nullable final Cursor cursor,
                                    final int position) {
        holder.bind(cursor);
        super.onBindViewHolder(holder, cursor, position);
    }

    @Override
    protected void onBindViewDataBinding(@NonNull final ToolsListItemToolBinding binding, @Nullable final Cursor cursor,
                                         final int position) {
        final String code = cursor != null ? CursorUtils.getString(cursor, ToolTable.COLUMN_CODE, Tool.INVALID_CODE) :
                Tool.INVALID_CODE;
        final ToolsAdapterToolViewModel viewModel =
                mViewModelProvider.get(VIEW_MODEL_KEY_PREFIX + code, ToolsAdapterToolViewModel.class);
        viewModel.getToolCode().setValue(code);
        binding.setViewModel(viewModel);
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
    // endregion Lifecycle

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
        final Callbacks callbacks = mCallbacks.get();
        if (callbacks != null) {
            final int count = getItemCount();
            final long[] ids = new long[count];
            for (int i = 0; i < count; i++) {
                ids[i] = getItemId(i);
            }

            callbacks.onToolsReordered(ids);
        }
    }

    class ToolViewHolder extends BaseViewHolder<ToolsListItemToolBinding> implements DraggableItemViewHolder {
        @Nullable
        @BindView(R.id.title)
        TextView mTitleView;
        int mTitleTextStyle = Typeface.NORMAL;
        @Nullable
        @BindView(R.id.tagline)
        TextView mTaglineView;
        @Nullable
        @BindView(R.id.language_parallel)
        TextView mParallelLanguageView;

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
        Locale mPrimaryLanguage;
        @Nullable
        Locale mParallelLanguage;
        @Nullable
        Locale mDefaultLanguage;

        private final DraggableItemState mDragState = new DraggableItemState();

        ToolViewHolder(@NonNull final ToolsListItemToolBinding binding) {
            super(binding);
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
                mPrimaryLanguage = CursorUtils.getLocale(cursor, COL_PRIMARY_LANGUAGE, null);
                mDefaultLanguage = CursorUtils.getLocale(cursor, COL_DEFAULT_LANGUAGE, null);
                mParallelLanguage = CursorUtils.getLocale(cursor, COL_PARALLEL_LANGUAGE, null);
            } else {
                mId = Tool.INVALID_ID;
                mCode = Tool.INVALID_CODE;
                mType = Tool.Type.DEFAULT;
                mTitle = null;
                mTitleLanguage = null;
                mTagline = null;
                mTaglineLanguage = null;
                mPrimaryLanguage = null;
                mDefaultLanguage = null;
                mParallelLanguage = null;
            }

            // update any bound views
            if (mTaglineView != null) {
                mTaglineView.setText(mTagline);
                mTaglineView.setTypeface(LocaleTypefaceUtils.getTypeface(mTaglineView.getContext(), mTaglineLanguage));
            }
        }

        @NonNull
        @Override
        public DraggableItemState getDragState() {
            return mDragState;
        }

        @Override
        public void setDragStateFlags(@DraggableItemStateFlags final int flags) {
            mDragState.setFlags(flags);
        }

        @Override
        @DraggableItemStateFlags
        public int getDragStateFlags() {
            return mDragState.getFlags();
        }

        @Optional
        @OnClick(R.id.root)
        void select() {
            final Callbacks callbacks = mCallbacks.get();
            if (callbacks != null) {
                if (mPrimaryLanguage != null) {
                    callbacks.onToolSelect(mCode, mType, mPrimaryLanguage, mParallelLanguage);
                } else if (mDefaultLanguage != null) {
                    callbacks.onToolSelect(mCode, mType, mDefaultLanguage, mParallelLanguage);
                } else if (mParallelLanguage != null) {
                    callbacks.onToolSelect(mCode, mType, mParallelLanguage);
                }
            }
        }
    }
}
