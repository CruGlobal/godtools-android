package org.cru.godtools.article.databinding.adapter;

import android.databinding.BindingAdapter;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.ccci.gto.android.common.picasso.view.SimplePicassoImageView;
import org.cru.godtools.base.tool.model.view.ResourceViewUtils;
import org.cru.godtools.xml.model.Resource;

public final class PicassoImageViewAdapters {
    @BindingAdapter({"picassoFile"})
    public static void setPicassoFile(@NonNull final SimplePicassoImageView view, @Nullable final Resource resource) {
        ResourceViewUtils.bind(resource, view);
    }
}
