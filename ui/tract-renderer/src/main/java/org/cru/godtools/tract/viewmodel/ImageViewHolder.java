package org.cru.godtools.tract.viewmodel;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.view.ViewGroup;

import org.ccci.gto.android.common.picasso.view.PicassoImageView;
import org.cru.godtools.base.tool.model.view.ResourceViewUtils;
import org.cru.godtools.tract.R;
import org.cru.godtools.tract.R2;
import org.cru.godtools.xml.model.Image;

import butterknife.BindView;

@UiThread
final class ImageViewHolder extends BaseViewHolder<Image> {
    @BindView(R2.id.image)
    PicassoImageView mImage;

    ImageViewHolder(@NonNull final ViewGroup parent, @Nullable final BaseViewHolder parentViewHolder) {
        super(Image.class, parent, R.layout.tract_content_image, parentViewHolder);
    }

    @Override
    void onBind() {
        super.onBind();
        ResourceViewUtils.bind(Image.getResource(mModel), mImage);
    }
}
