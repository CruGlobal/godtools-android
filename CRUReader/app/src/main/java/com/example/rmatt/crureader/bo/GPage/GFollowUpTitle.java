package com.example.rmatt.crureader.bo.GPage;

import android.content.Context;
import android.support.percent.PercentRelativeLayout;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.rmatt.crureader.bo.Gtapi;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Text;

/**
 * Created by rmatt on 10/26/2016.
 *       <followup-title modifier="bold" x="0" x-trailing-offset="20" xoffset="20">Jesus has come
 into your life as he promised.
 </followup-title>
 */
@Root(name="followup-title")
public class GFollowUpTitle extends GBaseTextAttributes {

    private static final String TAG = "GFollowUpTitle";

    @Override
    public TextView render(ViewGroup viewGroup, int position) {
        TextView v = new TextView(viewGroup.getContext());
        v.setText("GFollowUpTitle");
        Log.i(TAG, "render in GFollowUpTitle");
        return v;
    }
}
