package com.example.rmatt.crureader.bo.GPage;

import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.rmatt.crureader.bo.GPage.Compat.RenderViewCompat;
import com.example.rmatt.crureader.bo.GPage.RenderHelpers.RenderConstants;
import com.example.rmatt.crureader.bo.Gtapi;

import org.simpleframework.xml.Attribute;

/**
 * Created by rmatt on 10/31/2016.
 */

public abstract class GBaseTextAttributes extends Gtapi {


    @org.simpleframework.xml.Text(required = false, empty = "")
    public String content;

    @Attribute(required = false)
    public String textalign;



    @Attribute(required = false)
    public String modifier;


    @Attribute(required = false)
    public String color;

    @Override
    public TextView render(ViewGroup viewGroup, int position) {
        TextView textView = new TextView(viewGroup.getContext());
        textView.setText(content);
        textView.setTextColor(RenderConstants.parseColor(color));
        textView.setTypeface(Typeface.defaultFromStyle(RenderConstants.getTypefaceFromModifier(modifier)));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, RenderConstants.getTextSizeFromXMLSize(size));

        textView.setIncludeFontPadding(false);
        RenderViewCompat.textViewAlign(textView, textalign);
        //This needs to be a base attributes property.
        //textView.setGravity(RenderConstants.getGravityFromAlign(textalign));
        RenderConstants.setDefaultPadding(textView);
        return textView;
    }


    @Override
    public ViewGroup group(ViewGroup viewGroup, int position) {
        return null;
    }


}
