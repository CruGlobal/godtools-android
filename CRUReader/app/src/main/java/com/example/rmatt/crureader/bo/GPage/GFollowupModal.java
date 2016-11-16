package com.example.rmatt.crureader.bo.GPage;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.support.percent.PercentRelativeLayout;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.rmatt.crureader.bo.GPage.IDO.IRender;
import com.example.rmatt.crureader.bo.Gtapi;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by rmatt on 10/26/2016.
 */
@Root(name = "followup-modal")
public class GFollowupModal extends Gtapi implements IRender {

    private static final String TAG = "GFollowupModal";
    @Attribute(name = "followup-id")
    public int followUpID;

    @Attribute
    public String listeners;

    @Element(name = "fallback")
    public GFallback fallback;

    @Override
    public View render(ViewGroup viewGroup) {
        TextView v = new TextView(viewGroup.getContext());
        v.setText(followUpID + "");
        Log.i(TAG, "render in GFollowupModal");
        return v;
    }
}
