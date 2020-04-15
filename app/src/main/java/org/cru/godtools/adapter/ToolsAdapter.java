package org.cru.godtools.adapter;

import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemState;
import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemViewHolder;
import com.h6ah4i.android.widget.advrecyclerview.draggable.ItemDraggableRange;
import com.h6ah4i.android.widget.advrecyclerview.draggable.annotation.DraggableItemStateFlags;

import org.ccci.gto.android.common.db.util.CursorUtils;
import org.ccci.gto.android.common.recyclerview.adapter.DataBindingViewHolder;
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
    protected void onBindViewDataBinding(@NonNull final ToolsListItemToolBinding binding, @Nullable final Cursor cursor,
                                         final int position) {
        final String code = cursor != null ? CursorUtils.getString(cursor, ToolTable.COLUMN_CODE, Tool.INVALID_CODE) :
                Tool.INVALID_CODE;
        final ToolsAdapterToolViewModel viewModel =
                mViewModelProvider.get(VIEW_MODEL_KEY_PREFIX + code, ToolsAdapterToolViewModel.class);
        viewModel.getToolCode().setValue(code);

        binding.setTool(viewModel.getTool());
        binding.setDownloadProgress(viewModel.getDownloadProgress());
        binding.setBanner(viewModel.getBanner());
        binding.setPrimaryTranslation(viewModel.getFirstTranslation());
        binding.setParallelTranslation(viewModel.getParallelTranslation());
        binding.setParallelLanguage(viewModel.getParallelLanguage());
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

    static class ToolViewHolder extends DataBindingViewHolder<ToolsListItemToolBinding>
            implements DraggableItemViewHolder {
        private final DraggableItemState mDragState = new DraggableItemState();

        ToolViewHolder(@NonNull final ToolsListItemToolBinding binding) {
            super(binding);
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
    }
}
