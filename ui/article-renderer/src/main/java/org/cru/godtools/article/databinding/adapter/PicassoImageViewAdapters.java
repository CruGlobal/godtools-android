package org.cru.godtools.article.databinding.adapter;

import org.ccci.gto.android.common.picasso.view.SimplePicassoImageView;
import org.cru.godtools.base.tool.model.view.ResourceViewUtils;
import org.cru.godtools.xml.model.Resource;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.BindingAdapter;

public final class PicassoImageViewAdapters {
    @BindingAdapter({"picassoFile"})
    public static void setPicassoFile(@NonNull final SimplePicassoImageView view, @Nullable final Resource resource) {
        ResourceViewUtils.bind(resource, view);
    }
}
