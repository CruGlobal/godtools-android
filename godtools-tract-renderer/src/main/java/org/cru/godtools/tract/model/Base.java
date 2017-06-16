package org.cru.godtools.tract.model;

import android.support.annotation.CallSuper;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.annimon.stream.Stream;

import org.cru.godtools.base.model.Event;
import org.cru.godtools.tract.R;
import org.cru.godtools.tract.model.Parent.ParentViewHolder;
import org.greenrobot.eventbus.EventBus;
import org.xmlpull.v1.XmlPullParser;

import java.util.Set;

import butterknife.ButterKnife;

abstract class Base {
    static final String XML_PRIMARY_COLOR = "primary-color";
    static final String XML_PRIMARY_TEXT_COLOR = "primary-text-color";
    static final String XML_TEXT_COLOR = "text-color";
    static final String XML_BACKGROUND_COLOR = "background-color";
    static final String XML_BACKGROUND_IMAGE = "background-image";
    static final String XML_BACKGROUND_IMAGE_GRAVITY = "background-image-align";
    static final String XML_BACKGROUND_IMAGE_SCALE_TYPE = "background-image-scale-type";

    @NonNull
    private final Base mParent;

    Base() {
        mParent = this;
    }

    Base(@NonNull final Base parent) {
        mParent = parent;
    }

    @NonNull
    protected Manifest getManifest() {
        if (mParent == this) {
            throw new IllegalStateException();
        } else {
            return mParent.getManifest();
        }
    }

    private String getDefaultEventNamespace() {
        return getManifest().getCode();
    }

    @Nullable
    Resource getResource(@Nullable final String name) {
        return getManifest().getResource(name);
    }

    @NonNull
    protected Page getPage() {
        if (mParent == this) {
            throw new IllegalStateException();
        } else {
            return mParent.getPage();
        }
    }

    @Nullable
    StylesParent getStylesParent() {
        if (mParent instanceof StylesParent) {
            return (StylesParent) mParent;
        } else if (mParent != this) {
            return mParent.getStylesParent();
        } else {
            return null;
        }
    }

    @Nullable
    static StylesParent getStylesParent(@Nullable final Base obj) {
        return obj != null ? obj.getStylesParent() : null;
    }

    @NonNull
    final Set<Event.Id> parseEvents(@NonNull final XmlPullParser parser, @NonNull final String attribute) {
        final String raw = parser.getAttributeValue(null, attribute);
        return Event.Id.parse(getDefaultEventNamespace(), raw);
    }

    @UiThread
    static abstract class BaseViewHolder<T extends Base> {
        @Nullable
        final ParentViewHolder<? extends Base> mParentViewHolder;

        @NonNull
        public final View mRoot;

        @NonNull
        final Class<T> mModelType;
        @Nullable
        T mModel;

        BaseViewHolder(@NonNull final Class<T> modelType, @NonNull final ViewGroup parent, @LayoutRes final int layout,
                       @Nullable final ParentViewHolder<?> parentViewHolder) {
            this(modelType, LayoutInflater.from(parent.getContext()).inflate(layout, parent, false), parentViewHolder);
        }

        BaseViewHolder(@NonNull final Class<T> modelType, @NonNull final View root,
                       @Nullable final ParentViewHolder<?> parentViewHolder) {
            mParentViewHolder = parentViewHolder;
            mModelType = modelType;
            mRoot = root;
            ButterKnife.bind(this, mRoot);
            mRoot.setTag(R.id.view_holder, this);
        }

        /* BEGIN lifecycle */

        @CallSuper
        void onBind() {}

        boolean onValidate() {
            // default to being valid
            return true;
        }

        void onBuildEvent(@NonNull final Event.Builder builder, final boolean recursive) {}

        /* END lifecycle */

        public final void bind(@Nullable final T model) {
            mModel = model;
            onBind();
        }

        final void sendEvents(@NonNull final Set<Event.Id> ids) {
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
}
