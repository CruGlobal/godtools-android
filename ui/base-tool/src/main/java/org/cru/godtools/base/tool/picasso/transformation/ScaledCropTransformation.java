package org.cru.godtools.base.tool.picasso.transformation;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import com.squareup.picasso.Transformation;

import org.cru.godtools.xml.model.ImageScaleType;

import jp.wasabeef.picasso.transformations.CropTransformation;
import jp.wasabeef.picasso.transformations.CropTransformation.GravityHorizontal;
import jp.wasabeef.picasso.transformations.CropTransformation.GravityVertical;

import static com.google.common.base.MoreObjects.toStringHelper;

public final class ScaledCropTransformation implements Transformation {
    private final float mAspectRatio;
    private final ImageScaleType mScaleType;
    private final GravityHorizontal mGravityHorizontal;
    private final GravityVertical mGravityVertical;

    public ScaledCropTransformation(final int width, final int height, @NonNull final ImageScaleType scaleType,
                                    @NonNull final GravityHorizontal gravityHorizontal,
                                    @NonNull final GravityVertical gravityVertical) {
        mAspectRatio = height != 0 ? ((float) width) / ((float) height) : 1;
        mScaleType = scaleType;
        mGravityHorizontal = gravityHorizontal;
        mGravityVertical = gravityVertical;
    }

    @NonNull
    @Override
    public Bitmap transform(@NonNull final Bitmap source) {
        int width = 0;
        int height = 0;
        switch (mScaleType) {
            case FILL_X:
                width = source.getWidth();
                break;
            case FILL_Y:
                height = source.getHeight();
                break;
        }

        return new CropTransformation(width, height, mAspectRatio, mGravityHorizontal, mGravityVertical)
                .transform(source);
    }

    @Override
    public String key() {
        return toStringHelper(this)
                .add("aspectRatio", mAspectRatio)
                .add("scaleType", mScaleType)
                .add("gravityHorizontal", mGravityHorizontal)
                .add("gravityVertical", mGravityVertical)
                .toString();
    }
}
