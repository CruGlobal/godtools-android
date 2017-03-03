package org.keynote.godtools.android.newnew.appcompat;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.content.res.AppCompatResources;
import android.util.AttributeSet;
import android.widget.TextView;

import org.keynote.godtools.android.R;

/**
 * Created by rmatt on 2/21/2017.
 */

public class VectorCompat {
    public static void renderSVGAndTint(TextView appCompatTextView, Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray attributeArray = context.obtainStyledAttributes(
                    attrs,
                    R.styleable.Vector);
            final int colorTint = attributeArray.getColor(R.styleable.Vector_drawableTint, -1);
            Drawable drawableLeft = null;
            Drawable drawableRight = null;
            Drawable drawableBottom = null;
            Drawable drawableTop = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                drawableLeft = attributeArray.getDrawable(R.styleable.Vector_drawableLeftCompat);
                drawableRight = attributeArray.getDrawable(R.styleable.Vector_drawableRightCompat);
                drawableBottom = attributeArray.getDrawable(R.styleable.Vector_drawableBottomCompat);
                drawableTop = attributeArray.getDrawable(R.styleable.Vector_drawableTopCompat);
            } else {
                final int drawableLeftId = attributeArray.getResourceId(R.styleable.Vector_drawableLeftCompat, -1);
                final int drawableRightId = attributeArray.getResourceId(R.styleable.Vector_drawableRightCompat, -1);
                final int drawableBottomId = attributeArray.getResourceId(R.styleable.Vector_drawableBottomCompat, -1);
                final int drawableTopId = attributeArray.getResourceId(R.styleable.Vector_drawableTopCompat, -1);

                if (drawableLeftId != -1)
                    drawableLeft = AppCompatResources.getDrawable(context, drawableLeftId);
                if (drawableRightId != -1)
                    drawableRight = AppCompatResources.getDrawable(context, drawableRightId);
                if (drawableBottomId != -1)
                    drawableBottom = AppCompatResources.getDrawable(context, drawableBottomId);
                if (drawableTopId != -1)
                    drawableTop = AppCompatResources.getDrawable(context, drawableTopId);
            }

            if (colorTint != -1) {
                if (drawableLeft != null)
                    drawableLeft.setColorFilter(colorTint, PorterDuff.Mode.SRC_ATOP);
                if (drawableRight != null)
                    drawableRight.setColorFilter(colorTint, PorterDuff.Mode.SRC_ATOP);
                if (drawableBottom != null)
                    drawableBottom.setColorFilter(colorTint, PorterDuff.Mode.SRC_ATOP);
                if (drawableTop != null)
                    drawableTop.setColorFilter(colorTint, PorterDuff.Mode.SRC_ATOP);
            }

            appCompatTextView.setCompoundDrawablesWithIntrinsicBounds(drawableLeft, drawableTop, drawableRight, drawableBottom);
            attributeArray.recycle();
        }

    }
}
