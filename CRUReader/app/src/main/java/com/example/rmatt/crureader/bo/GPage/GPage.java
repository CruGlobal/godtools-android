package com.example.rmatt.crureader.bo.GPage;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.percent.PercentRelativeLayout;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.example.rmatt.crureader.bo.GPage.Compat.RenderViewCompat;
import com.example.rmatt.crureader.bo.GPage.RenderHelpers.Diagnostics;
import com.example.rmatt.crureader.bo.GPage.RenderHelpers.RenderConstants;
import com.example.rmatt.crureader.bo.GPage.RenderHelpers.RenderSingleton;
import com.example.rmatt.crureader.bo.Gtapi;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.ElementListUnion;
import org.simpleframework.xml.Root;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by rmatt on 10/18/2016.
 */
@Root(name = "page")
public class GPage extends Gtapi<PercentRelativeLayout, ViewGroup> {
    private static final String TAG = "GPage";
    @Attribute(required = false)
    public String watermark;

    @Element(required = false)
    public GTitle title;

    @Attribute(required = false)
    public int bigbuttons;

    @ElementListUnion({@ElementList(inline = true, required = false, entry = "text", type = GText.class),
            @ElementList(inline = true, required = false, entry = "button", type = GButton.class)
    })
    public ArrayList<Gtapi> gtapiArrayList = new ArrayList<Gtapi>();

    @ElementList(inline = true, required = false, entry = "followup-modal", type = GFollowupModal.class)
    public ArrayList<GFollowupModal> followupModalsArrayList = new ArrayList<GFollowupModal>();


    @Attribute
    public String color;
    @Attribute(required = false)
    public String buttons;

    @Attribute(required = false)
    public String backgroundimage;

    @Element(name = "question", required = false)
    public GQuestion gQuestion;

    public String getBackgroundColor() {
        if (color != null) return color;
        else return RenderConstants.DEFAULT_BACKGROUND_COLOR;
    }

    @Override
    public PercentRelativeLayout render(ViewGroup container, int position) {
        //setDefaultValues();
        Context context = container.getContext();
        /* Background color */

        container.setBackgroundColor(RenderSingleton.getInstance().getPositionGlobalColorAsInt(position));


        Integer lastId = 9999;
        Integer topId = -1;
        Integer bottomId = -1;


        PercentRelativeLayout percentRelativeLayout = new PercentRelativeLayout(context);
        //    percentRelativeLayout.setPadding(40, 40, 40,40);
        loadBackground(percentRelativeLayout, position);


        PercentRelativeLayout.LayoutParams params = null;


        if (title != null) {

            ViewGroup vgTop = title.render(percentRelativeLayout, position);


            vgTop.setId(RenderViewCompat.generateViewId());
            topId = vgTop.getId();
            Log.i(TAG, "View Compat top Id: " + topId);
            percentRelativeLayout.addView(vgTop);
        }


        if (gQuestion != null) {
            View vgBottom = gQuestion.render(percentRelativeLayout, position);
            PercentRelativeLayout.LayoutParams vgBottomParams = new PercentRelativeLayout.LayoutParams(PercentRelativeLayout.LayoutParams.MATCH_PARENT,
                    PercentRelativeLayout.LayoutParams.WRAP_CONTENT);
            vgBottomParams.addRule(PercentRelativeLayout.ALIGN_PARENT_BOTTOM);
            vgBottom.setId(RenderViewCompat.generateViewId());
            bottomId = vgBottom.getId();
            Log.i(TAG, "View Compat bottom Id: " + bottomId);
            percentRelativeLayout.addView(vgBottom, vgBottomParams);
        }


        if (gtapiArrayList != null && gtapiArrayList.size() > 0) {

            LinearLayout midSection = RenderConstants.renderLinearLayoutListWeighted(context, gtapiArrayList, position);

            params = new PercentRelativeLayout.LayoutParams(PercentRelativeLayout.LayoutParams.MATCH_PARENT, PercentRelativeLayout.LayoutParams.WRAP_CONTENT);

            if (topId > 0) params.addRule(PercentRelativeLayout.BELOW, topId);
            if (bottomId > 0) params.addRule(PercentRelativeLayout.ABOVE, bottomId);
            percentRelativeLayout.addView(midSection, params);
        }

        RenderConstants.setUpFollowups(container, followupModalsArrayList);

        return percentRelativeLayout;

    }

    @Override
    public ViewGroup group(ViewGroup viewGroup, int position) {
        return null;
    }

    private void loadBackground(ViewGroup viewGroup, int position) {

        String resourceName = (watermark != null && watermark.length() > 0) ? watermark : backgroundimage;
        Diagnostics.StartMethodTracingWithKey(position + "_" + resourceName);
        if (resourceName != null) {

            try {
                Drawable d = Drawable.createFromStream(viewGroup.getContext().getAssets().open(resourceName), null);
                ViewCompat.setBackground(viewGroup, d);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Diagnostics.StopMethodTracingByKey(position + "_" + resourceName);

    }

    private String getImageURL() {
        return "file:///android_asset/" + backgroundimage;
    }


}

