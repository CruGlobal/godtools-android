package org.cru.godtools.tract.model;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.annimon.stream.IntStream;
import com.annimon.stream.Optional;
import com.annimon.stream.Stream;
import com.google.common.collect.ImmutableList;

import org.cru.godtools.base.model.Event;
import org.cru.godtools.tract.R2;

import java.util.List;

import butterknife.BindView;

@UiThread
public abstract class ParentViewHolder<T extends Base & Parent> extends BaseViewHolder<T> {
    @Nullable
    @BindView(R2.id.content)
    LinearLayout mContent;

    ParentViewHolder(@NonNull final Class<T> modelType, @NonNull final ViewGroup parent, final int layout,
                     @Nullable final BaseViewHolder parentViewHolder) {
        super(modelType, parent, layout, parentViewHolder);
    }

    ParentViewHolder(@NonNull final Class<T> modelType, @NonNull final View root,
                     @Nullable final BaseViewHolder parentViewHolder) {
        super(modelType, root, parentViewHolder);
    }

    // region Lifecycle Events

    @Override
    @CallSuper
    void onBind() {
        super.onBind();
        bindContent();
    }

    @Override
    boolean onValidate() {
        // only return true if all children validate
        // XXX: we don't want to short-circuit execution, so we don't use allMatch
        return streamChildViewHolders().filterNot(BaseViewHolder::onValidate).count() == 0;
    }

    @Override
    @CallSuper
    void onBuildEvent(@NonNull final Event.Builder builder, final boolean recursive) {
        // if we are in recursive mode, process any children objects
        if (recursive) {
            streamChildViewHolders().forEach(vh -> vh.onBuildEvent(builder, true));
        }
    }

    // endregion Lifecycle Events

    private void bindContent() {
        if (mContent != null) {
            final List<Content> content = mModel != null ? mModel.getContent() : ImmutableList.of();
            mContent.removeAllViews();

            for (final Content item : content) {
                Optional.ofNullable(ContentViewUtils.createViewHolder(item.getClass(), mContent, this))
                        .ifPresent(holder -> {
                            //noinspection unchecked
                            holder.bind(item);
                            mContent.addView(holder.mRoot);
                        });
            }
        }
    }

    private Stream<BaseViewHolder> streamChildViewHolders() {
        if (mContent != null) {
            return IntStream.range(0, mContent.getChildCount())
                    .mapToObj(mContent::getChildAt)
                    .withoutNulls()
                    .map(BaseViewHolder::forView)
                    .select(BaseViewHolder.class);
        }
        return Stream.empty();
    }
}
