package com.example.rmatt.crureader.bo.GPage.RenderHelpers;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.percent.PercentRelativeLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.example.rmatt.crureader.bo.GCoordinator;
import com.example.rmatt.crureader.bo.GPage.Compat.RenderViewCompat;
import com.example.rmatt.crureader.bo.GPage.GFollowupModal;
import com.example.rmatt.crureader.bo.GPage.Views.Space;

import java.util.ArrayList;

/**
 * Created by rmatt on 11/3/2016.
 */

public class RenderConstants {

    /* The dimensions w, h,
    */

    public static final float REFERENCE_DEVICE_HEIGHT = 480.0f;
    public static final float REFERENCE_DEVICE_WIDTH = 320.0f;
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
    private static final String TAG = "RenderConstants";

    /*
    The percent of screen height by taking xml value height and dividing by the xml's height basis.
     */
    public static float getVerticalPercent(int height) {
        float verticalPercent = (float) ((float) height / REFERENCE_DEVICE_HEIGHT);
        Log.i(TAG, "height: " + height + " - vertical percent - " + verticalPercent);
        return verticalPercent;
    }

    /*
        The percent of screen width by taking xml value width and dividing by the xml's width basis.
    */
    public static float getHorizontalPercent(int width) {
        float horizontalPercent = ((float) width / REFERENCE_DEVICE_WIDTH);
        Log.i(TAG, "width: " + width + " - horizontal percent - " + horizontalPercent);
        return horizontalPercent;
    }

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
                return Gravity.END + Gravity.TOP;
            else if (align.equalsIgnoreCase("center"))
                return Gravity.CENTER_HORIZONTAL + Gravity.TOP;
        }

        return Gravity.START + Gravity.TOP;
    }

    public static int getRelativeLayoutRuleFromAlign(String align) {
        if (align != null) {
            if (align.equalsIgnoreCase("right")) {
                return RelativeLayout.ALIGN_PARENT_END;
            } else if (align.equalsIgnoreCase("center")) {
                return RelativeLayout.CENTER_HORIZONTAL;
            }
        }

        return RelativeLayout.ALIGN_PARENT_START;
    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static int getTextAlign(String textAlign) {
        if (textAlign != null && !textAlign.equalsIgnoreCase("")) {
            if (textAlign.equalsIgnoreCase("center"))
                return Gravity.CENTER_HORIZONTAL + Gravity.TOP;
            else if (textAlign.equalsIgnoreCase("right"))
                return Gravity.END + Gravity.TOP;
        }
        return Gravity.START + Gravity.TOP;
    }


    public static int parseColor(String color) {
        if (color != null) {
            return Color.parseColor(color);
        } else {
            return Color.parseColor(DEFAULT_BACKGROUND_COLOR);
        }
    }

    public static int renderLinearLayoutListWeighted(LayoutInflater inflater, ViewGroup percentRelativeLayout, ArrayList<GCoordinator> GCoordinatorArrayList, int position) {
        return renderLinearLayoutListWeighted(inflater, percentRelativeLayout, GCoordinatorArrayList, position, 0);

    }

    public static int renderLinearLayoutListWeighted(LayoutInflater inflater, ViewGroup percentRelativeLayout, ArrayList<GCoordinator> GCoordinatorArrayList, int position, int maxSpace) {
        LinearLayout midSection = new LinearLayout(inflater.getContext());
        midSection.setOrientation(LinearLayout.VERTICAL);
        midSection.setId(RenderViewCompat.generateViewId());


        Space space = new Space(inflater.getContext());
        LinearLayout.LayoutParams evenSpreadDownSpaceLayoutParams;
        evenSpreadDownSpaceLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, maxSpace, 1.0f); //max space is to deal with popups that shouldn't take up the whole container.

        midSection.addView(space, evenSpreadDownSpaceLayoutParams);


        for (GCoordinator tap : GCoordinatorArrayList) {

            tap.render(inflater, tap.y == null ? midSection : percentRelativeLayout, position); // put into the relative layout if x, y are managing the positioning, or else put into the weight layout.
            if (!tap.isManuallyLaidOut()) //If items are manually laid out, we don't want to add space between them.
            {
                space = new Space(inflater.getContext());
                midSection.addView(space, evenSpreadDownSpaceLayoutParams);
            }


        }
        percentRelativeLayout.addView(midSection, new PercentRelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, maxSpace > 0 ? ViewGroup.LayoutParams.WRAP_CONTENT : ViewGroup.LayoutParams.MATCH_PARENT)); //If there is max space wrap_content because we only want to fill a small area.   If it isn't we want to fill the whole available area evenly.
        return midSection.getId();
    }

    public static String[] getTapEvents(String tapEvents) {
        String[] splitTapEvents = null;
        if (tapEvents != null && tapEvents.trim() != "") {
            splitTapEvents = tapEvents.split("[,]");
            for (String tapEvent : splitTapEvents) {
                Log.i(TAG, "Tap event post split: " + tapEvent);
            }

        }
        return splitTapEvents;

    }

    public static void setUpFollowups(ViewGroup container, ArrayList<GFollowupModal> followupModalsArrayList) {
        for (GFollowupModal modal : followupModalsArrayList) {
            if (modal.listeners != null) {
                Log.i(TAG, "modal listeners: " + modal.listeners + " as hash: " + modal.listeners.hashCode());

                RenderSingleton.getInstance().gPanelHashMap.put(modal.listeners.hashCode(), modal);
            }
        }
    }

    public static int getHorizontalPixels(Integer width) {
        return Math.round(getHorizontalPercent(width) * RenderSingleton.getInstance().screenWidth);
    }

    public static int getVerticalPixels(Integer height) {
        return Math.round(getVerticalPercent(height) * RenderSingleton.getInstance().screenHeight);
    }
}
