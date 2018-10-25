package org.cru.godtools.tract.viewmodel;

import android.view.ViewGroup;

import org.ccci.gto.android.common.picasso.view.PicassoImageView;
import org.cru.godtools.base.tool.model.view.ResourceViewUtils;
import org.cru.godtools.tract.R;
import org.cru.godtools.tract.R2;
import org.cru.godtools.xml.model.Image;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import butterknife.BindView;
import butterknife.OnClick;

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

    @OnClick(R2.id.image)
    void click() {
        if (mModel != null) {
            sendEvents(mModel.getEvents());
        }
    }
}
