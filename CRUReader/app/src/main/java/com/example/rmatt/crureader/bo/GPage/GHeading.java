package com.example.rmatt.crureader.bo.GPage;

import android.view.ViewGroup;
import android.widget.TextView;

import com.example.rmatt.crureader.bo.GPage.RenderHelpers.RenderConstants;
import com.example.rmatt.crureader.bo.GPage.RenderHelpers.RenderSingleton;

import org.simpleframework.xml.*;

@Root(name = "heading")
public class GHeading extends GBaseTextAttributes
{

    private static final String TAG = "GHeading";

    @Override
    public TextView render(ViewGroup viewGroup, int position) {
        setDefaultValues(position);
        return super.render(viewGroup, position);
    }

    private void setDefaultValues(int position) {
        //If no size is specified for header set size to 200
        if (size == 0) {
            size = RenderConstants.DEFAULT_HEADER_TEXT_SIZE;
        }
        if(color == null || color == "") {
            color = RenderSingleton.getInstance().getPositionGlobalColorAsString(position);
        }

    }



}

