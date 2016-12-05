package com.example.rmatt.crureader.bo.GPage.Views;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;
import android.util.Log;

import com.example.rmatt.crureader.R;

/**
 * Created by rmatt on 12/5/2016.
 */

public class AutoScaleTextButton extends AppCompatButton {
    private int textSizeScalar;

    public AutoScaleTextButton(Context context) {
        super(context);
        initialize(context, null, 0);
    }

    public AutoScaleTextButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs, 0);
    }

    public AutoScaleTextButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs, defStyleAttr);
    }

    private void initialize(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AutoScaleTextButton, defStyleAttr, R.style.AutoScaleTextButton);
        textSizeScalar = a.getResourceId(R.styleable.AutoScaleTextButton_textSizeScalarTB, 17);
    }

    @Override
    public void setTextSize(int unit, float size) {

        float adjustedSize = (size * (float) textSizeScalar) / 100.0f;
        Log.w("TextSize", "Size: " + size + " adjustedSize: " + adjustedSize + " textSizeScalar: " + textSizeScalar);
        super.setTextSize(unit, adjustedSize);
    }

}
