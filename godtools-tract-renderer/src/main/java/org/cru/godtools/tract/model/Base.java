package org.cru.godtools.tract.model;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import org.cru.godtools.base.model.Event;
import org.cru.godtools.tract.R;
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

    String getDefaultEventNamespace() {
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

    @NonNull
    final Set<Event.Id> parseEvents(@NonNull final XmlPullParser parser, @NonNull final String attribute) {
        final String raw = parser.getAttributeValue(null, attribute);
        return Event.Id.parse(getDefaultEventNamespace(), raw);
    }

    static abstract class BaseViewHolder<T extends Base> {
        @NonNull
        public final View mRoot;

        @Nullable
        T mModel;

        BaseViewHolder(@NonNull final View root) {
            mRoot = root;
            ButterKnife.bind(this, mRoot);
            mRoot.setTag(R.id.view_holder, this);
        }

        public final void setModel(@Nullable final T model) {
            mModel = model;
            bind();
        }

        @CallSuper
        void bind() {}
    }
}
