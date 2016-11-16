package com.example.rmatt.crureader.bo.GPage;

import android.content.Context;
import android.graphics.Color;
import android.provider.Settings;
import android.support.percent.PercentRelativeLayout;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.rmatt.crureader.bo.GPage.IDO.IRender;
import com.example.rmatt.crureader.bo.GPage.RenderHelpers.RenderSingleton;
import com.example.rmatt.crureader.bo.Gtapi;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.ElementListUnion;
import org.simpleframework.xml.Root;

import java.util.ArrayList;

/**
 * Created by rmatt on 10/18/2016.
 */
@Root(name = "panel")
public class GPanel implements IRender {





    private static final String TAG = "GPanel";
    @ElementListUnion({@ElementList(inline = true, required = false, entry = "text", type = GText.class),
            @ElementList(inline = true, required = false, entry = "image", type = GImage.class),
            @ElementList(inline = true, required = false, entry = "button-pair", type = GButtonPair.class),
            @ElementList(inline = true, required = false, entry = "link-button", type = GLinkButtonAttributes.class),
            @ElementList(inline = true, required = false, entry = "button", type = GButton.class)})
    public ArrayList<Gtapi> panelArrayList = new ArrayList<Gtapi>();



    @Override
    public TextView render(ViewGroup viewGroup) {
        Context context = viewGroup.getContext();
        String backgroundColor = RenderSingleton.getInstance().globalColor;

        TextView v = new TextView(context);
        v.setBackgroundColor(Color.parseColor(backgroundColor ));
        v.setText("GPanel");
        Log.i(TAG, "render in GPanel");
        return v;
    }

    public String getColor() {
        return RenderSingleton.getInstance().globalColor;
    }


    //##################################################################
    // Non-XML properties
    //##################################################################




}
