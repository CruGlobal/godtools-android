package com.example.rmatt.crureader.bo.GPage;

import android.view.ViewGroup;
import android.widget.TextView;

import com.example.rmatt.crureader.bo.GPage.RenderHelpers.RenderSingleton;

import org.simpleframework.xml.Root;

@Root(name = "heading")
public class GHeading extends GBaseTextAttributes
{

    private static final String TAG = "GHeading";


    @Override
    public TextView render(ViewGroup viewGroup, int position) {
        setDefaultValues(position);
        return super.render(viewGroup, position);
    }

    public void setDefaultValues(int position) {
        if(textColor == null || textColor.equalsIgnoreCase("")) {
            textColor = RenderSingleton.getInstance().getPositionGlobalColorAsString(position);
        }

    }



}

