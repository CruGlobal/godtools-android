package com.example.rmatt.crureader.bo.GPage.Views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.PaintDrawable;
import android.os.Build;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.view.View;

import com.example.rmatt.crureader.R;
import com.example.rmatt.crureader.bo.GPage.RenderHelpers.Diagnostics;

/**
 * Created by rmatt on 12/2/2016.
 */

public class RootTextColorTextView extends AppCompatTextView {

    private boolean inheritColorFromRootViewBackground = false;
    private boolean textColorSetFromBackground = false;
    private int parentId;

    public RootTextColorTextView(Context context) {
        super(context);
    }

    public RootTextColorTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs);
    }

    public RootTextColorTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs);
    }

    private void initialize(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RootTextColorTextView);

        inheritColorFromRootViewBackground = a.getBoolean(R.styleable.RootTextColorTextView_inheritColorFromRootViewBackground, false);
        parentId = a.getResourceId(R.styleable.RootTextColorTextView_parentId, -1);

    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        checkBackgroundColor();
    }


    private void checkBackgroundColor() {
        String diagnosticsKey = "LongRecursion? ";
        Diagnostics.StartMethodTracingByKey(diagnosticsKey);
        if (inheritColorFromRootViewBackground && !textColorSetFromBackground && getRootView() != null && parentId > -1) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {

                this.setTextColorFromBackground(((ColorDrawable) getParentRecursive((View) getParent(), parentId).getBackground()).getColor());
            } else {
                this.setTextColorFromBackground(((PaintDrawable) getParentRecursive((View) getParent(), parentId).getBackground()).getPaint().getColor());
            }
        }
        Diagnostics.StopMethodTracingByKey(diagnosticsKey);
    }

    private void setTextColorFromBackground(int color) {
        textColorSetFromBackground = true;
        this.setTextColor(color);
    }

    public View getParentRecursive(View view, int id) {
        if (view.getId() != id) {
            return getParentRecursive((View) view.getParent(), id);
        } else {
            return view;
        }
    }


    public boolean getInheritColorFromRootViewBackground() {
        return inheritColorFromRootViewBackground;
    }

    public int getParentId() {
        return parentId;
    }


}
