package com.example.rmatt.crureader.bo.GPage;

import android.content.Context;
import android.support.percent.PercentRelativeLayout;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.rmatt.crureader.bo.GPage.IDO.IRender;
import com.example.rmatt.crureader.bo.Gtapi;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Text;

/**
 * Created by rmatt on 10/25/2016.
 */

public class GBaseButtonAttributes extends GBaseAttributes implements IRender{


    private static final String TAG = "GBaseButtonAttributes";
    @Attribute(required = false)
    public String validation;

    @Attribute(required = false)
    public String mode;

    @Attribute(required = false)
    public String align;

    @Attribute(name="tap-events", required=false)
    public String tapEvents;

    @Attribute(name="x-trailing-offset", required = false)
    public int xTrailingOffset;

    @Override
    public LinearLayout render(ViewGroup viewGroup) {
        LinearLayout ll = new LinearLayout(viewGroup.getContext());

        TextView v = new TextView(viewGroup.getContext());
        v.setText("GBaseButtonAttributes");
        ll.addView(v, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        Log.i(TAG, "render in GBaseButtonAttributes");
        return ll;
    }
}
