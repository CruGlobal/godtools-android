package org.cru.godtools.tract.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import org.ccci.gto.android.common.picasso.view.SimplePicassoImageView;
import org.cru.godtools.base.tool.widget.ScaledPicassoImageView;
import org.cru.godtools.xml.model.ImageScaleType;

import jp.wasabeef.picasso.transformations.CropTransformation.GravityHorizontal;
import jp.wasabeef.picasso.transformations.CropTransformation.GravityVertical;

public class TractPicassoImageView extends SimplePicassoImageView implements ScaledPicassoImageView {
    public TractPicassoImageView(@NonNull final Context context) {
        this(context, null);
    }

    public TractPicassoImageView(@NonNull final Context context, @Nullable final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TractPicassoImageView(@NonNull final Context context, @Nullable final AttributeSet attrs,
                                 final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public TractPicassoImageView(@NonNull final Context context, @Nullable final AttributeSet attrs,
                                 final int defStyleAttr, final int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @NonNull
    @Override
    protected Helper createHelper(@Nullable final AttributeSet attrs, final int defStyleAttr, final int defStyleRes) {
        return new ScaleHelper(this, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void setScaleType(@NonNull final ImageScaleType type) {
        if (mHelper instanceof ScaleHelper) {
            ((ScaleHelper) mHelper).setScaleType(type);
        }
    }

    @Override
    public void setGravityHorizontal(@NonNull final GravityHorizontal gravity) {
        if (mHelper instanceof ScaleHelper) {
            ((ScaleHelper) mHelper).setGravityHorizontal(gravity);
        }
    }

    @Override
    public void setGravityVertical(@NonNull final GravityVertical gravity) {
        if (mHelper instanceof ScaleHelper) {
            ((ScaleHelper) mHelper).setGravityVertical(gravity);
        }
    }
}
