package com.example.rmatt.crureader.bo.GPage;

import android.graphics.Typeface;
import android.support.percent.PercentRelativeLayout;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.rmatt.crureader.bo.GCoordinator;
import com.example.rmatt.crureader.bo.GPage.Compat.RenderViewCompat;
import com.example.rmatt.crureader.bo.GPage.RenderHelpers.RenderConstants;

import org.simpleframework.xml.Attribute;

/**
 * Created by rmatt on 10/31/2016.
 */

public class GBaseTextAttributes extends GCoordinator {


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
        if (view != null && view instanceof TextView) {

            TextView textViewCast = (TextView) view;
            applyTextColor(textViewCast);
            applyTextModifier(textViewCast);
            applyTextSize(textViewCast);
            applyTextAlign(textViewCast);
            applyTextContent(textViewCast);
        }
    }

    private void applyTextContent(TextView textViewCast) {
        textViewCast.setText(content);
    }

    private void applyTextAlign(TextView textViewCast) {
        if (textalign != null && !textalign.equalsIgnoreCase("")) {
            RenderViewCompat.textViewAlign(textViewCast, textalign);
        }
    }

    private void applyTextSize(TextView textViewCast) {
        if (textSize != null) {
            textViewCast.setTextSize(TypedValue.COMPLEX_UNIT_SP, RenderConstants.getTextSizeFromXMLSize(textSize));
        }
    }

    @Override
    public int render(LayoutInflater inflater, ViewGroup viewGroup, int position) {

        TextView textView = new TextView(viewGroup.getContext());
        textView.setId(RenderViewCompat.generateViewId());
        viewGroup.addView(textView, new PercentRelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        updateBaseAttributes(textView);
        return textView.getId();
    }


    public void applyTextColor(TextView textView) {
        if (textColor != null && !textColor.equalsIgnoreCase("")) {
            textView.setTextColor(RenderConstants.parseColor(textColor));
        }
    }

    public void applyTextModifier(TextView textView) {
        if (textModifier != null && !textModifier.equalsIgnoreCase(""))
            textView.setTypeface(Typeface.defaultFromStyle(RenderConstants.getTypefaceFromModifier(textModifier)));
    }


}
