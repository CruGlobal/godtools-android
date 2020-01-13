package org.cru.godtools.base.tool.model.view;

import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import org.ccci.gto.android.common.picasso.view.PicassoImageView;
import org.cru.godtools.base.tool.widget.ScaledPicassoImageView;
import org.cru.godtools.xml.model.ImageGravity;
import org.cru.godtools.xml.model.ImageScaleType;
import org.cru.godtools.xml.model.Resource;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import jp.wasabeef.picasso.transformations.CropTransformation.GravityHorizontal;
import jp.wasabeef.picasso.transformations.CropTransformation.GravityVertical;

import static android.widget.RelativeLayout.ALIGN_PARENT_BOTTOM;
import static android.widget.RelativeLayout.ALIGN_PARENT_TOP;
import static android.widget.RelativeLayout.CENTER_HORIZONTAL;
import static android.widget.RelativeLayout.CENTER_VERTICAL;
import static org.cru.godtools.base.util.FileUtils.getGodToolsFile;

public class ResourceViewUtils {
    private static final int ALIGN_PARENT_START =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 ? RelativeLayout.ALIGN_PARENT_START :
                    RelativeLayout.ALIGN_PARENT_LEFT;
    private static final int ALIGN_PARENT_END =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 ? RelativeLayout.ALIGN_PARENT_END :
                    RelativeLayout.ALIGN_PARENT_RIGHT;

    public static void bind(@Nullable final Resource resource, @Nullable final PicassoImageView view) {
        if (view != null) {
            view.setPicassoFile(resource != null ? getGodToolsFile(view.getContext(), resource.getLocalName()) : null);
        }
    }

    public static void bindBackgroundImage(@Nullable final Resource resource,
                                           @NonNull final ScaledPicassoImageView image,
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
                ImageGravity.isStart(gravity) ? (!rtl ? GravityHorizontal.LEFT : GravityHorizontal.RIGHT) :
                        ImageGravity.isEnd(gravity) ? (!rtl ? GravityHorizontal.RIGHT : GravityHorizontal.LEFT) :
                                GravityHorizontal.CENTER);
        image.setGravityVertical(ImageGravity.isTop(gravity) ? GravityVertical.TOP :
                                         ImageGravity.isBottom(gravity) ? GravityVertical.BOTTOM :
                                                 GravityVertical.CENTER);

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
