package com.example.rmatt.crureader.bo.GPage;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.percent.PercentRelativeLayout;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.rmatt.crureader.bo.GPage.RenderHelpers.RenderConstants;

import org.simpleframework.xml.Attribute;

/**
 * Created by rmatt on 10/31/2016.
 */

public abstract class GBaseTextAttributes extends GBaseAttributes {


    @org.simpleframework.xml.Text(required = false, empty = "")
    public String content;

    @Attribute(required = false)
    public String textalign;

    @Attribute(required = false)
    public String translate;

    @Attribute(required = false)
    public String align;

    @Attribute(required = false)
    public String modifier;

    @Attribute(name = "x-trailing-offset", required = false)
    public int xTrailingOffset;

    @Attribute(required = false)
    public String color;

    @Override
    public TextView render(ViewGroup viewGroup) {

        TextView textView = new TextView(viewGroup.getContext());
        textView.setText(content);
        textView.setTextColor(RenderConstants.parseColor(color));
        textView.setTypeface(Typeface.defaultFromStyle(RenderConstants.getTypefaceFromModifier(modifier)));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, RenderConstants.reduceTextSize(size));
        textView.setGravity(RenderConstants.getGravityFromAlign(textalign));
        RenderConstants.setDefaultPadding(textView);
        return textView;
    }


}
