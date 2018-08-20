package org.cru.godtools.tract.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import org.ccci.gto.android.common.picasso.view.PicassoImageView;
import org.cru.godtools.tract.widget.ScaledPicassoImageView;

import jp.wasabeef.picasso.transformations.CropTransformation;

import static android.widget.RelativeLayout.ALIGN_PARENT_BOTTOM;
import static android.widget.RelativeLayout.ALIGN_PARENT_TOP;
import static android.widget.RelativeLayout.CENTER_HORIZONTAL;
import static android.widget.RelativeLayout.CENTER_VERTICAL;
import static org.cru.godtools.base.util.FileUtils.getFile;
import static org.cru.godtools.tract.compat.RelativeLayoutCompat.ALIGN_PARENT_END;
import static org.cru.godtools.tract.compat.RelativeLayoutCompat.ALIGN_PARENT_START;

public class ResourceViewUtils {
    static void bind(@Nullable final Resource resource, @Nullable final PicassoImageView view) {
        if (view != null) {
            view.setPicassoFile(resource != null ? getFile(view.getContext(), resource.getLocalName()) : null);
        }
    }

    static void bindBackgroundImage(@NonNull final ScaledPicassoImageView image, @Nullable final Resource resource,
                                    @NonNull final ImageScaleType scale, final int gravity) {
        image.toggleBatchUpdates(true);

        // set the background image visibility
        final ImageView view = image.asImageView();
        view.setVisibility(resource != null ? View.VISIBLE : View.GONE);

        // update the background image itself
        bind(resource, image);
        image.setScaleType(scale);
        final boolean rtl = Resource.getLayoutDirection(resource) == ViewCompat.LAYOUT_DIRECTION_RTL;
        image.setGravityHorizontal(
                ImageGravity.isStart(gravity) ? (!rtl ? CropTransformation.GravityHorizontal.LEFT : CropTransformation.GravityHorizontal.RIGHT) :
                        ImageGravity.isEnd(gravity) ? (!rtl ? CropTransformation.GravityHorizontal.RIGHT : CropTransformation.GravityHorizontal.LEFT) :
                                CropTransformation.GravityHorizontal.CENTER);
        image.setGravityVertical(ImageGravity.isTop(gravity) ? CropTransformation.GravityVertical.TOP :
                                         ImageGravity.isBottom(gravity) ? CropTransformation.GravityVertical.BOTTOM :
                                                 CropTransformation.GravityVertical.CENTER);

        image.toggleBatchUpdates(false);

        // update layout params
        final ViewGroup.LayoutParams lp = view.getLayoutParams();
        if (lp instanceof RelativeLayout.LayoutParams) {
            // set default layout for background image first
            final RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) lp;
            rlp.addRule(ALIGN_PARENT_START, 0);
            rlp.addRule(ALIGN_PARENT_END, 0);
            rlp.addRule(CENTER_HORIZONTAL, 0);
            rlp.addRule(ALIGN_PARENT_TOP, 0);
            rlp.addRule(ALIGN_PARENT_BOTTOM, 0);
            rlp.addRule(CENTER_VERTICAL, 0);

            // update gravity (X-Axis)
            if (ImageGravity.isStart(gravity)) {
                rlp.addRule(ALIGN_PARENT_START);
            } else if (ImageGravity.isEnd(gravity)) {
                rlp.addRule(ALIGN_PARENT_END);
            } else if (ImageGravity.isCenterX(gravity)) {
                rlp.addRule(CENTER_HORIZONTAL);
            }

            // update gravity (Y-Axis)
            if (ImageGravity.isTop(gravity)) {
                rlp.addRule(ALIGN_PARENT_TOP);
            } else if (ImageGravity.isBottom(gravity)) {
                rlp.addRule(ALIGN_PARENT_BOTTOM);
            } else if (ImageGravity.isCenterY(gravity)) {
                rlp.addRule(CENTER_VERTICAL);
            }

            // set the layout params back on the image
            view.setLayoutParams(lp);
        }
    }
}
