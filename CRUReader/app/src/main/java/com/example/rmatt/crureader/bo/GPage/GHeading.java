package com.example.rmatt.crureader.bo.GPage;

import android.content.Context;
import android.graphics.Typeface;
import android.support.percent.PercentRelativeLayout;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.rmatt.crureader.bo.GPage.RenderHelpers.RenderConstants;
import com.example.rmatt.crureader.bo.GPage.RenderHelpers.RenderSingleton;

import org.simpleframework.xml.*;

/**
 * Created by rmatt on 10/18/2016.
 */

@Root(name = "heading")
public class GHeading extends GBaseTextAttributes
{

    private static final String TAG = "GHeading";



    @Override
    public TextView render(ViewGroup viewGroup) {
        setDefaultValues();
        return super.render(viewGroup);
    }

    private void setDefaultValues() {
        //If no size is specified for header set size to 200
        if (size == 0) {
            size = RenderConstants.DEFAULT_HEADER_TEXT_SIZE;
        }
        if(color == null || color == "") {
            color = RenderSingleton.getInstance().globalColor;
        }

    }



}

