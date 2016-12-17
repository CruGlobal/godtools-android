package com.example.rmatt.crureader.bo.GPage.Views;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;
import android.util.Log;

import com.example.rmatt.crureader.R;
import com.example.rmatt.crureader.bo.GPage.RenderHelpers.RenderSingleton;

/**
 * Created by rmatt on 12/11/2016.
 */

public class AutoScaleButtonView extends AppCompatButton {

    private int textSizeScalar;

    public AutoScaleButtonView(Context context) {
        super(context);
        initialize(context, null, 0);
    }

    public AutoScaleButtonView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs, 0);
    }

    public AutoScaleButtonView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs, defStyleAttr);
    }

    private void initialize(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AutoScaleButtonView, defStyleAttr, R.style.Widget_GodTools_AutoScaleButtonView);
        textSizeScalar = a.getInteger(R.styleable.AutoScaleButtonView_buttonTextSizeScalar, 17);
    }

    @Override
    public void setTextSize(int unit, float size) {

        float adjustedSize = ((size) * (float)textSizeScalar * RenderSingleton.getInstance().getScreenHeightForNonRotationDesign()) / 80.0F;
        Log.w("TextSize", "Size: " + size + " adjustedSize: " + adjustedSize + " textSizeScalar: " + textSizeScalar);
        super.setTextSize(unit, adjustedSize);
        invalidate();
    }



}
