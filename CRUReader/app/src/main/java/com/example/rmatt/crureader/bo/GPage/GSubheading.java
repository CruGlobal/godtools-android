package com.example.rmatt.crureader.bo.GPage;

import android.view.ViewGroup;
import android.widget.TextView;

import com.example.rmatt.crureader.bo.GPage.RenderHelpers.RenderSingleton;

import org.simpleframework.xml.Element;

/**
 * Created by rmatt on 10/18/2016.
 */

@Element(name = "subheading")
public class GSubheading extends GBaseTextAttributes {

    private static final String TAG = "GSubheading";


    @Override
    public TextView render(ViewGroup viewGroup, int position) {
        setDefaultValues(position);
        return super.render(viewGroup, position);
    }

    public void setDefaultValues(int position) {
        if (textColor == null || textColor == "") {
            textColor = RenderSingleton.getInstance().getPositionGlobalColorAsString(position);
        }
    }
}
