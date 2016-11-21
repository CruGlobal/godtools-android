/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.captain_miao.optroundcardview;

import android.annotation.TargetApi;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;

import static com.github.captain_miao.optroundcardview.RoundRectDrawableWithShadow.calculateHorizontalPadding;
import static com.github.captain_miao.optroundcardview.RoundRectDrawableWithShadow.calculateVerticalPadding;

/**
 * Very simple drawable that draws a rounded rectangle background with arbitrary corners and also
 * reports proper outline for Lollipop.
 * <p>
 * Simpler and uses less resources compared to GradientDrawable or ShapeDrawable.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class RoundRectDrawable extends Drawable {
    protected float mRadius;
    protected final Paint mPaint;
    protected final RectF mBoundsF;
    protected final Rect mBoundsI;
    protected float mPadding;
    protected boolean mInsetForPadding = false;
    protected boolean mInsetForRadius = true;

    protected PorterDuffColorFilter mTintFilter;
    protected ColorStateList mTint;
    protected PorterDuff.Mode mTintMode;

    public RoundRectDrawable(int backgroundColor, float radius) {
        mRadius = radius;
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mPaint.setColor(backgroundColor);
        mBoundsF = new RectF();
        mBoundsI = new Rect();
    }

    void setPadding(float padding, boolean insetForPadding, boolean insetForRadius) {
        if (padding == mPadding && mInsetForPadding == insetForPadding &&
                mInsetForRadius == insetForRadius) {
            return;
        }
        mPadding = padding;
        mInsetForPadding = insetForPadding;
        mInsetForRadius = insetForRadius;
        updateBounds(null);
        invalidateSelf();
    }

    float getPadding() {
        return mPadding;
    }

    @Override
    public void draw(Canvas canvas) {
        final Paint paint = mPaint;

        final boolean clearColorFilter;
        if (mTintFilter != null && paint.getColorFilter() == null) {
            paint.setColorFilter(mTintFilter);
            clearColorFilter = true;
        } else {
            clearColorFilter = false;
        }

        canvas.drawRoundRect(mBoundsF, mRadius, mRadius, paint);

        if (clearColorFilter) {
            paint.setColorFilter(null);
        }
    }

    private void updateBounds(Rect bounds) {
        if (bounds == null) {
            bounds = getBounds();
        }
        mBoundsF.set(bounds.left, bounds.top, bounds.right, bounds.bottom);
        mBoundsI.set(bounds);
        if (mInsetForPadding) {
            float vInset = calculateVerticalPadding(mPadding, mRadius, mInsetForRadius);
            float hInset = calculateHorizontalPadding(mPadding, mRadius, mInsetForRadius);
            mBoundsI.inset((int) Math.ceil(hInset), (int) Math.ceil(vInset));
            // to make sure they have same bounds.
            mBoundsF.set(mBoundsI);
        }
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        updateBounds(bounds);
    }

    @Override
    public void getOutline(Outline outline) {
        outline.setRoundRect(mBoundsI, mRadius);
    }

    void setRadius(float radius) {
        if (radius == mRadius) {
            return;
        }
        mRadius = radius;
        updateBounds(null);
        invalidateSelf();
    }

    @Override
    public void setAlpha(int alpha) {
        mPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        mPaint.setColorFilter(cf);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    public float getRadius() {
        return mRadius;
    }

    public void setColor(int color) {
        mPaint.setColor(color);
        invalidateSelf();
    }

    @Override
    public void setTintList(ColorStateList tint) {
        mTint = tint;
        mTintFilter = createTintFilter(mTint, mTintMode);
        invalidateSelf();
    }

    @Override
    public void setTintMode(PorterDuff.Mode tintMode) {
        mTintMode = tintMode;
        mTintFilter = createTintFilter(mTint, mTintMode);
        invalidateSelf();
    }

    @Override
    protected boolean onStateChange(int[] stateSet) {
        if (mTint != null && mTintMode != null) {
            mTintFilter = createTintFilter(mTint, mTintMode);
            return true;
        }
        return false;
    }

    @Override
    public boolean isStateful() {
        return (mTint != null && mTint.isStateful()) || super.isStateful();
    }

    /**
     * Ensures the tint filter is consistent with the current tint color and
     * mode.
     */
    private PorterDuffColorFilter createTintFilter(ColorStateList tint, PorterDuff.Mode tintMode) {
        if (tint == null || tintMode == null) {
            return null;
        }
        final int color = tint.getColorForState(getState(), Color.TRANSPARENT);
        return new PorterDuffColorFilter(color, tintMode);
    }
}
