package com.example.rmatt.crureader.bo.GPage.RenderHelpers;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.ColorInt;
import android.support.annotation.IntDef;
import android.support.annotation.Px;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.rmatt.crureader.R;
import com.example.rmatt.crureader.bo.GPage.Compat.RenderViewCompat;
import com.example.rmatt.crureader.bo.Gtapi;

import java.util.ArrayList;

/**
 * Created by rmatt on 11/3/2016.
 */

public class RenderConstants {

    /* The dimensions w, h,
    */

    public static final float REFERENCE_DEVICE_HEIGHT = 480.0f;
    public static final float REFERENCE_DEVICE_WIDTH = 320.0f;



    /*
    The percent of screen height by taking xml value height and dividing by the xml's height basis.
     */
    public static float getVerticalPercent(int height) {
        return (float) ((float) height / REFERENCE_DEVICE_HEIGHT);
    }


    /*
        The percent of screen width by taking xml value width and dividing by the xml's width basis.
    */
    public static float getHorizontalPercent(int width) {
        return (float) ((float) width / REFERENCE_DEVICE_WIDTH);
    }


    /*******************************************************
     * Color constants
     *******************************************************/

    public static final String DEFAULT_BACKGROUND_COLOR = "#FFFFFFFF";



    /***************************************************
     * Font size constants
     ***************************************************/
    public static final float SCALE_TEXT_SIZE = 18.0F;
    public static final int DEFAULT_BUTTON_TEXT_SIZE = 100;
    public static final int DEFAULT_TEXT_SIZE = 60;
    public static final int DEFAULT_NUMBER_TEXT_SIZE = 200;
    public static final int DEFAULT_HEADER_TEXT_SIZE = 90;
    public static final int DEFAULT_SUBHEADER_TEXT_SIZE = 100;
    public static final String DEFAULT_BUTTON_TEXT_ALIGN = "left";


    public static final String DEFAULT_TEXT_COLOR = "#FFFFFFFF";

    public static float getTextSizeFromXMLSize(int xmlSize) {
        if (xmlSize == 0) {
            return SCALE_TEXT_SIZE;
        }
        return (xmlSize * SCALE_TEXT_SIZE) / 100.0F;
    }


    /********************************************
     * Helpers
     * /
     *******************************************/

    public static int getTypefaceFromModifier(String modifier) {
        if (modifier != null) {
            if (modifier.equalsIgnoreCase("italics"))
                return Typeface.ITALIC;
            if (modifier.equalsIgnoreCase("bold"))
                return Typeface.BOLD;
            if (modifier.equalsIgnoreCase("bold-italics"))
                return Typeface.BOLD_ITALIC;
        }
        return Typeface.NORMAL;
    }

    public static int getGravityFromAlign(String align) {
        if (align != null) {
            if (align.equalsIgnoreCase("right"))
                return Gravity.END;
            else if (align.equalsIgnoreCase("left"))
                return Gravity.START;
        }

        return Gravity.CENTER_HORIZONTAL;
    }


    public static int getTextAlign(String textAlign)
    {
        if(textAlign != null && textAlign != "")
        {
            if(textAlign == "center")
                return View.TEXT_ALIGNMENT_CENTER;
            else if(textAlign == "right")
                return View.TEXT_ALIGNMENT_TEXT_END;
        }
        return View.TEXT_ALIGNMENT_TEXT_START;
    }

    public static void setDefaultPadding(View numberTextView) {
        int dimensionPixelSize = numberTextView.getContext().getResources().getDimensionPixelSize(R.dimen.text_padding);
        numberTextView.setPadding(dimensionPixelSize, dimensionPixelSize, dimensionPixelSize, dimensionPixelSize);
    }

    public static int parseColor(String color) {
        if (color != null) {
            return Color.parseColor(color);
        } else {
            return Color.parseColor(DEFAULT_BACKGROUND_COLOR);
        }
    }


    public static LinearLayout renderLinearLayoutListWeighted(Context context, ArrayList<Gtapi> gtapiArrayList) {
        LinearLayout midSection = new LinearLayout(context);
        midSection.setOrientation(LinearLayout.VERTICAL);


        for (Gtapi tap : gtapiArrayList) {
            View view = tap.render(midSection);
            view.setId(RenderViewCompat.generateViewId());
            LinearLayout.LayoutParams midSectionChildLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1);
            midSection.addView(view, midSectionChildLayoutParams);

        }
        return midSection;
    }

    public static LinearLayout renderLinearLayoutList(Context context, ArrayList<Gtapi> gtapiArrayList) {
        LinearLayout midSection = new LinearLayout(context);
        midSection.setOrientation(LinearLayout.VERTICAL);


        for (Gtapi tap : gtapiArrayList) {
            View view = tap.render(midSection);
            view.setId(RenderViewCompat.generateViewId());
            LinearLayout.LayoutParams midSectionChildLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            midSection.addView(view, midSectionChildLayoutParams);

        }
        return midSection;
    }
}
