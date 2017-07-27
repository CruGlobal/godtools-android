package org.cru.godtools.tract.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.squareup.picasso.RequestCreator;

import org.ccci.gto.android.common.base.model.Dimension;
import org.ccci.gto.android.common.picasso.transformation.ScaleTransformation;
import org.ccci.gto.android.common.picasso.view.PicassoImageView;
import org.cru.godtools.tract.R;
import org.cru.godtools.tract.picasso.transformation.ScaledCropTransformation;

import jp.wasabeef.picasso.transformations.CropTransformation.GravityHorizontal;
import jp.wasabeef.picasso.transformations.CropTransformation.GravityVertical;

import static org.cru.godtools.tract.widget.ScaledPicassoImageView.ScaleType.FILL_X;
import static org.cru.godtools.tract.widget.ScaledPicassoImageView.ScaleType.FILL_Y;

public interface ScaledPicassoImageView extends PicassoImageView {
    enum ScaleType {
        FIT, FILL, FILL_X, FILL_Y
    }

    class ScaleHelper extends Helper {
        @NonNull
        private ScaleType mScaleType = ScaleType.FIT;
        @NonNull
        private GravityHorizontal mGravityHorizontal = GravityHorizontal.CENTER;
        @NonNull
        private GravityVertical mGravityVertical = GravityVertical.CENTER;

        ScaleHelper(@NonNull final ImageView view, @Nullable final AttributeSet attrs, final int defStyleAttr,
                    final int defStyleRes) {
            super(view, attrs, defStyleAttr, defStyleRes);
            init(view.getContext(), attrs, defStyleAttr, defStyleRes);
        }

        private void init(@NonNull final Context context, @Nullable final AttributeSet attrs, final int defStyleAttr,
                          final int defStyleRes) {
            final TypedArray a =
                    context.obtainStyledAttributes(attrs, R.styleable.ScaledPicassoImageView, defStyleAttr,
                                                   defStyleRes);
            mScaleType =
                    ScaleType.values()[a.getInt(R.styleable.ScaledPicassoImageView_scaleType, mScaleType.ordinal())];
            a.recycle();
        }

        public final void setScaleType(@NonNull final ScaleType type) {
            final ScaleType old = mScaleType;
            mScaleType = type;
            if (old != mScaleType) {
                triggerUpdate();
            }
        }

        public final void setGravityHorizontal(@NonNull final GravityHorizontal gravity) {
            final GravityHorizontal old = mGravityHorizontal;
            mGravityHorizontal = gravity;
            if (old != mGravityHorizontal) {
                triggerUpdate();
            }
        }

        public final void setGravityVertical(@NonNull final GravityVertical gravity) {
            final GravityVertical old = mGravityVertical;
            mGravityVertical = gravity;
            if (old != mGravityVertical) {
                triggerUpdate();
            }
        }

        @Override
        protected void onSetUpdateScale(@NonNull final RequestCreator update, final Dimension size) {
            switch (mScaleType) {
                case FILL:
                case FILL_X:
                case FILL_Y:
                    if (size.width > 0 && mScaleType == FILL_X) {
                        update.resize(size.width, 0);
                        update.onlyScaleDown();
                    } else if (size.height > 0 && mScaleType == FILL_Y) {
                        update.resize(0, size.height);
                        update.onlyScaleDown();
                    } else {
                        update.transform(new ScaleTransformation(size.width, size.height, true));
                    }

                    // crop with gravity
                    update.transform(
                            new ScaledCropTransformation(size.width, size.height, mScaleType, mGravityHorizontal,
                                                         mGravityVertical));
                    break;
                case FIT:
                    update.resize(size.width, size.height);
                    update.onlyScaleDown();
                    update.centerInside();
                    break;
            }
        }
    }

    void setScaleType(@NonNull ScaleType type);

    void setGravityHorizontal(@NonNull GravityHorizontal gravity);

    void setGravityVertical(@NonNull GravityVertical gravity);
}
