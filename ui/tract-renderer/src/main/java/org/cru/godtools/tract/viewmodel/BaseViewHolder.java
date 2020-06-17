package org.cru.godtools.tract.viewmodel;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.annimon.stream.Stream;

import org.cru.godtools.base.model.Event;
import org.cru.godtools.tract.R;
import org.cru.godtools.tract.analytics.model.ContentAnalyticsActionEvent;
import org.cru.godtools.xml.model.AnalyticsEvent;
import org.cru.godtools.xml.model.Base;
import org.cru.godtools.xml.model.BaseKt;
import org.greenrobot.eventbus.EventBus;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import androidx.annotation.CallSuper;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.core.view.ViewCompat;
import androidx.lifecycle.Observer;
import butterknife.ButterKnife;

@UiThread
abstract class BaseViewHolder<T extends Base> implements Observer<T> {
    private final Handler mHandler;

    @Nullable
    final BaseViewHolder mParentViewHolder;
    @NonNull
    public final View mRoot;

    @NonNull
    final Class<T> mModelType;
    @Nullable
    T mModel;

    boolean mVisible = false;

    protected BaseViewHolder(@NonNull final Class<T> modelType, @NonNull final ViewGroup parent,
                             @LayoutRes final int layout, @Nullable final BaseViewHolder parentViewHolder) {
        this(modelType, LayoutInflater.from(parent.getContext()).inflate(layout, parent, false), parentViewHolder);
    }

    protected BaseViewHolder(@NonNull final Class<T> modelType, @NonNull final View root,
                   @Nullable final BaseViewHolder parentViewHolder) {
        mHandler = new Handler(Looper.getMainLooper());

        mParentViewHolder = parentViewHolder;
        mModelType = modelType;
        mRoot = root;
        ButterKnife.bind(this, mRoot);
        mRoot.setTag(R.id.view_holder, this);
    }

    @Nullable
    public static BaseViewHolder forView(@NonNull final View view) {
        return forView(view, BaseViewHolder.class);
    }

    @Nullable
    public static <T extends BaseViewHolder> T forView(@Nullable final View view, @NonNull final Class<T> clazz) {
        if (view != null) {
            final Object holder = view.getTag(R.id.view_holder);
            if (clazz.isInstance(holder)) {
                return clazz.cast(holder);
            }
        }

        return null;
    }

    // region Lifecycle
    @Override
    public final void onChanged(final T t) {
        bind(t);
    }

    @CallSuper
    void onBind() {
        updateLayoutDirection();
    }

    @CallSuper
    void onVisible() {}

    boolean onValidate() {
        // default to being valid
        return true;
    }

    void onBuildEvent(@NonNull final Event.Builder builder, final boolean recursive) {}

    @CallSuper
    public void onContentEvent(@NonNull final Event event) {}

    @CallSuper
    void onHidden() {}
    // endregion Lifecycle

    @Nullable
    public final T getModel() {
        return mModel;
    }

    public final void bind(@Nullable final T model) {
        if (model == null) {
            markHidden();
        }
        mModel = model;
        onBind();
    }

    public final void markVisible() {
        if (!mVisible && mModel != null) {
            mVisible = true;
            onVisible();
        }
    }

    public final void markHidden() {
        if (mVisible && mModel != null) {
            mVisible = false;
            onHidden();
        }
    }

    protected void updateLayoutDirection() {
        // HACK: In theory we should be able to set this on the root page only.
        // HACK: But updating the direction doesn't seem to trigger a re-layout of descendant views.
        ViewCompat.setLayoutDirection(mRoot, BaseKt.getLayoutDirection(mModel));
    }

    public final void sendEvents(@NonNull final Set<Event.Id> ids) {
        // short-circuit if there are no events being triggered
        if (ids.isEmpty()) {
            return;
        }

        // short-circuit if validation fails when it's required
        if (!validate(ids)) {
            return;
        }

        // try letting a parent build the event object
        final Event.Builder builder = Event.builder();
        if (mModel != null) {
            builder.locale(mModel.getManifest().getLocale());
        }
        if (!buildEvent(builder)) {
            // populate the event with our local state since it wasn't populated by a parent
            onBuildEvent(builder, false);
        }

        // trigger an event for every id provided
        Stream.of(ids)
                .map(builder::id)
                .map(Event.Builder::build)
                .forEach(EventBus.getDefault()::post);
    }

    /**
     * Trigger the specified analytics events.
     *
     * @param events All analytics events
     * @param types  The types of analytics events to actually trigger
     * @return Any pending analytics events.
     */
    @NonNull
    final List<Runnable> triggerAnalyticsEvents(final Collection<AnalyticsEvent> events,
                                                final AnalyticsEvent.Trigger... types) {
        return Stream.of(events)
                .filter(e -> e.isTriggerType(types))
                .map(this::sendAnalyticsEvent)
                .withoutNulls()
                .toList();
    }

    @Nullable
    private Runnable sendAnalyticsEvent(@NonNull final AnalyticsEvent event) {
        if (event.getDelay() > 0) {
            final Runnable task = () -> EventBus.getDefault().post(new ContentAnalyticsActionEvent(event));
            mHandler.postDelayed(task, event.getDelay() * 1000);
            return task;
        }

        EventBus.getDefault().post(new ContentAnalyticsActionEvent(event));
        return null;
    }

    final void cancelPendingAnalyticsEvents(@NonNull final List<Runnable> pendingTasks) {
        Stream.of(pendingTasks)
                .forEach(mHandler::removeCallbacks);
    }

    /**
     * @return true if the event has been built by a parent view holder.
     */
    boolean buildEvent(@NonNull final Event.Builder builder) {
        return mParentViewHolder != null && mParentViewHolder.buildEvent(builder);
    }

    boolean validate(@NonNull final Set<Event.Id> ids) {
        // navigate up hierarchy before performing validation
        if (mParentViewHolder != null) {
            return mParentViewHolder.validate(ids);
        }

        // no validation is necessary
        return true;
    }
}
