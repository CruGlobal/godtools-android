package com.example.rmatt.crureader.bo.GPage;

import android.content.Context;
import android.support.percent.PercentRelativeLayout;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.TextView;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

/**
 * Created by rmatt on 10/25/2016.
 */
@Root(name="link-button")
public class GLinkButtonAttributes extends GBaseTextAttributes {

    private static final String TAG = "GLinkButtonAttributes";
    @Attribute(name="tap-events", required=false)
    public String tapEvents;

    @Override
    public TextView render(ViewGroup viewGroup) {
        TextView v = new TextView(viewGroup.getContext());
        v.setText("GLinkButtonAttributes");
        Log.i(TAG, "render in GLinkButtonAttributes");
        return v;
    }

}
