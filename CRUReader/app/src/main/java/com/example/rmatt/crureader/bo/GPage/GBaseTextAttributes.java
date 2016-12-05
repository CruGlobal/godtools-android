package com.example.rmatt.crureader.bo.GPage;

import android.graphics.Typeface;
import android.support.percent.PercentRelativeLayout;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.rmatt.crureader.bo.GCoordinator;
import com.example.rmatt.crureader.bo.GPage.Compat.RenderViewCompat;
import com.example.rmatt.crureader.bo.GPage.RenderHelpers.RenderConstants;
import com.example.rmatt.crureader.bo.GPage.RenderHelpers.RenderSingleton;
import com.example.rmatt.crureader.bo.GPage.Views.AutoScaleTextView;

import org.simpleframework.xml.Attribute;

/**
 * Created by rmatt on 10/31/2016.
 */

public class GBaseTextAttributes extends GCoordinator {


    private static final String TAG = "GBaseTextAttributes";
    @org.simpleframework.xml.Text(required = false, empty = "")
    public String content;
    @Attribute(required = false)
    public String textalign;
    @Attribute(required = false, name = "size")
    public Integer textSize;
    @Attribute(required = false, name = "color")
    public String textColor;
    @Attribute(required = false, name = "modifier")
    public String textModifier;

    //TODO: cast T to skip unboxing.
    @Override
    public void updateBaseAttributes(View view) {
        super.updateBaseAttributes(view);
        if (view != null && view instanceof AutoScaleTextView) {

            AutoScaleTextView textViewCast = (AutoScaleTextView) view;
            applyTextColor(textViewCast);
            applyTextModifier(textViewCast);
            applyTextSize(textViewCast);
            applyTextAlign(textViewCast);
            applyTextContent(textViewCast);
        }
    }

    private void applyTextContent(AutoScaleTextView textViewCast) {
        textViewCast.setText(content);
    }

    private void applyTextAlign(AutoScaleTextView textViewCast) {
        if (textalign != null && !textalign.equalsIgnoreCase("")) {

            RenderViewCompat.textViewAlign(textViewCast, textalign);

            layoutAlign = textalign;
            updateAlignment(textViewCast);
        }
    }

    private void applyTextSize(AutoScaleTextView textViewCast) {
        if (width != null && height != null)
        {
            Log.e(TAG, "Should scale this~!~ + " + textViewCast.getText() + textViewCast.getId());
        }
        if (textSize != null) {
            textViewCast.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
        }
        else
        {
            textViewCast.setTextSize(TypedValue.COMPLEX_UNIT_SP, 100.0f); 
        }

    }

    @Override
    public int render(LayoutInflater inflater, ViewGroup viewGroup, int position) {

        AutoScaleTextView textView = new AutoScaleTextView(viewGroup.getContext());
        textView.setId(RenderViewCompat.generateViewId());
        viewGroup.addView(textView, new PercentRelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        updateBaseAttributes(textView);
        return textView.getId();
    }


    public void applyTextColor(AutoScaleTextView textView) {
        if (textColor != null && !textColor.equalsIgnoreCase("")) {
            textView.setTextColor(RenderConstants.parseColor(textColor));
        }
    }

    public void applyTextModifier(AutoScaleTextView textView) {
        if (textModifier != null && !textModifier.equalsIgnoreCase(""))
            textView.setTypeface(Typeface.defaultFromStyle(RenderConstants.getTypefaceFromModifier(textModifier)));
    }


    public void defaultColor(int position) {
        if(textColor == null) textColor = RenderSingleton.getInstance().getPositionGlobalColorAsString(position);
    }

    public Integer getTextSize() {
        return textSize;
    }
}
