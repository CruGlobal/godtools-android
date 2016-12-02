package com.example.rmatt.crureader.bo.GPage;

import android.content.Context;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.rmatt.crureader.bo.Gtapi;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.ElementListUnion;
import org.simpleframework.xml.Root;

import java.util.ArrayList;

/**
 * Created by rmatt on 10/26/2016.
 *   <thank-you listeners="follow-up-send">
 <text modifier="bold" size="100" textalign="center" x-trailing-offset="30"
 xoffset="30">Thank you
 </text>
 <text size="100" textalign="left" x-trailing-offset="30" xoffset="30">Check your
 email soon for your first study in following Jesus Christ.
 </text>
 <text size="100" textalign="left" x-trailing-offset="30" xoffset="30">If you don't
 receive it, please check your spam folder.
 </text>
 <link-button tap-events="follow-up-thank-you-done" textalign="center">Done
 </link-button>
 </thank-you>
 */

/*
    This is an activity
 */
@Root(name="thank-you")
public class GThankYou extends Gtapi {

    private static final String TAG = "GThankYou";
    @Attribute
    public String listeners;

    @ElementListUnion({@ElementList(inline = true, required = false, entry = "text", type = GBaseTextAttributes.class),
            @ElementList(inline = true, required = false, entry = "image", type = GImage.class),
            @ElementList(inline = true, required = false, entry = "button-pair", type = GButtonPair.class),
            @ElementList(inline = true, required = false, entry = "link-button", type = GLinkButtonAttributes.class)})
    public ArrayList<Gtapi> panelArrayList = new ArrayList<Gtapi>();

    @Override
    public TextView render(ViewGroup viewGroup, int position) {
        Context context = viewGroup.getContext();
        TextView v = new TextView(context);
        v.setText(TAG + ": " + listeners);
        Log.i(TAG, "render in: " +  TAG);
        return v;
    }

    @Override
    public ViewGroup group(ViewGroup viewGroup, int position) {
        return null;
    }
}
