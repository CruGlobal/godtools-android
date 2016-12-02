package com.example.rmatt.crureader.bo.GPage;

import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.rmatt.crureader.bo.GPage.Compat.RenderViewCompat;
import com.example.rmatt.crureader.bo.GPage.RenderHelpers.RenderConstants;
import com.example.rmatt.crureader.bo.Gtapi;

import org.simpleframework.xml.Attribute;

/**
 * Created by rmatt on 10/31/2016.
 */

public class GBaseTextAttributes extends Gtapi {


    //TODO: cast T to skip unboxing.
    @Override
    public void updateBaseAttributes(View view) {
        super.updateBaseAttributes(view);
        if (view != null && view instanceof TextView) {

            TextView textViewCast = (TextView)view;
            applyTextColor(textViewCast);
            applyTextModifier(textViewCast);
            applyTextSize(textViewCast);
            applyTextAlign(textViewCast);
            applyTextContent(textViewCast);
        }
    }

    @org.simpleframework.xml.Text(required = false, empty = "")
    public String content;

    private void applyTextContent(TextView textViewCast) {
        textViewCast.setText(content);
    }


    @Attribute(required = false)
    public String textalign;

    private void applyTextAlign(TextView textViewCast) {
        if(textalign != null && !textalign.equalsIgnoreCase(""))
        {
            RenderViewCompat.textViewAlign(textViewCast, textalign);
        }
    }


    @Attribute(required = false, name="size")
    public Integer textSize;

    private void applyTextSize(TextView textViewCast) {
        if(textSize != null)
        {
            textViewCast.setTextSize(TypedValue.COMPLEX_UNIT_SP, RenderConstants.getTextSizeFromXMLSize(textSize));
        }
    }



    @Override
    public TextView render(ViewGroup viewGroup, int position) {
        TextView textView = new TextView(viewGroup.getContext());
        textView.setText(content);
        textView.setTextColor(RenderConstants.parseColor(textColor));
        textView.setTypeface(Typeface.defaultFromStyle(RenderConstants.getTypefaceFromModifier(textModifier)));
        applyTextSize(textView);


        textView.setIncludeFontPadding(false);
        RenderViewCompat.textViewAlign(textView, textalign);
        //This needs to be a base attributes property.
        //textView.setGravity(RenderConstants.getGravityFromAlign(textalign));
       // RenderConstants.setDefaultPadding(textView);
        return textView;
    }



    @Override
    public ViewGroup group(ViewGroup viewGroup, int position) {
        return null;
    }




    @Attribute(required = false, name="color")
    public String textColor;

    public void applyTextColor(TextView textView) {
        if(textColor != null && !textColor.equalsIgnoreCase("")) {
            textView.setTextColor(RenderConstants.parseColor(textColor));
        }
    }


    @Attribute(required = false, name="modifier")
    public String textModifier;

    public void applyTextModifier(TextView textView) {
        if(textModifier != null && !textModifier.equalsIgnoreCase(""))
        textView.setTypeface(Typeface.defaultFromStyle(RenderConstants.getTypefaceFromModifier(textModifier)));
    }



}
