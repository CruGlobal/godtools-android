package com.example.rmatt.crureader.bo.GPage;

import android.content.Context;
import android.support.percent.PercentRelativeLayout;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.rmatt.crureader.bo.GPage.RenderHelpers.RenderConstants;

import org.simpleframework.xml.*;

/**
 * Created by rmatt on 10/18/2016.
 */

@Element(name = "subheading")
public class GSubheading extends GBaseTextAttributes {

    private static final String TAG = "GSubheading";

    @Override
    public TextView render(ViewGroup viewGroup) {
        setDefaultValues();
        return super.render(viewGroup);
    }

    private void setDefaultValues() {
        if (super.size == 0) {
            size = RenderConstants.DEFAULT_SUBHEADER_TEXT_SIZE;
        }
    }
}
