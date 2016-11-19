package com.example.rmatt.crureader.bo.GPage;

import android.app.ActionBar;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.percent.PercentLayoutHelper;
import android.support.percent.PercentRelativeLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.BitmapCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.ViewGroupCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.rmatt.crureader.R;
import com.example.rmatt.crureader.bo.GPage.IDO.IRender;
import com.example.rmatt.crureader.bo.GPage.RenderHelpers.RenderConstants;
import com.example.rmatt.crureader.bo.GPage.RenderHelpers.RenderSingleton;
import com.example.rmatt.crureader.bo.Gtapi;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.ElementListUnion;
import org.simpleframework.xml.Root;

import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.Inflater;

/**
 * Created by rmatt on 10/18/2016.
 */
@Root(name = "page")
public class GPage implements IRender {
    private static final String TAG = "GPage";
    @Attribute(required = false)
    public String watermark;

    @Element(required = false)
    public GTitle title;

    @Attribute(required = false)
    public int bigbuttons;

    @ElementListUnion({@ElementList(inline = true, required = false, entry = "text", type = GText.class),
            @ElementList(inline = true, required = false, entry = "button", type = GButton.class),
            @ElementList(inline = true, required = false, entry = "followup-modal", type = GFollowupModal.class)})
    public ArrayList<Gtapi> gtapiArrayList = new ArrayList<Gtapi>();
//    @ElementList(inline=true, required = false)
//    public List<GText> text;


    @Attribute
    public String color;

//    @ElementList(inline=true, required = false)
//    public List<GButton> button;

    @Attribute(required = false)
    public String buttons;

    @Attribute(required = false)
    public String backgroundimage;

    @Attribute(name = "tnt-trx-ref-value", required = false)
    public String tntTrxRefValue;

    @Attribute(name = "tnt-trx-translated", required = false)
    public String tntTrxTranslated;

    @Attribute(required = false)
    public Boolean translate;

    @Element(name = "question", required = false)
    public GQuestion gQuestion;

    public String getBackgroundColor() {
        if (color != null) return color;
        else return RenderConstants.DEFAULT_BACKGROUND_COLOR;
    }
    @Override
    public PercentRelativeLayout render(ViewGroup container) {
        //setDefaultValues();
        Context context = container.getContext();
        /* Background color */
        RenderSingleton.getInstance().globalColor = getBackgroundColor();
        container.setBackgroundColor(Color.parseColor(RenderSingleton.getInstance().globalColor));


        Integer lastId = 9999;
        Integer topId = -1;
        Integer bottomId = -1;


        PercentRelativeLayout percentRelativeLayout = new PercentRelativeLayout(context);

        loadBackground(percentRelativeLayout);


        PercentRelativeLayout.LayoutParams params = null;


        if (title != null) {

            ViewGroup vgTop = title.render(percentRelativeLayout);


            vgTop.setId(View.generateViewId());
            topId = vgTop.getId();

            Log.i(TAG, "lastId" + lastId);
            // vgTop.requestLayout();
            percentRelativeLayout.addView(vgTop);
        }


        if (gQuestion != null) {
            View vgBottom = gQuestion.render(percentRelativeLayout);
            PercentRelativeLayout.LayoutParams vgBottomParams = new PercentRelativeLayout.LayoutParams(PercentRelativeLayout.LayoutParams.MATCH_PARENT, PercentRelativeLayout.LayoutParams.WRAP_CONTENT);
            vgBottomParams.addRule(PercentRelativeLayout.ALIGN_PARENT_BOTTOM);
            vgBottom.setId(View.generateViewId());
            bottomId = vgBottom.getId();

            percentRelativeLayout.addView(vgBottom, vgBottomParams);
        }

        if (gtapiArrayList != null && gtapiArrayList.size() > 0) {

            LinearLayout midSection = new LinearLayout(context);
            midSection.setOrientation(LinearLayout.VERTICAL);


            for (Gtapi tap : gtapiArrayList) {

                View view = tap.render(midSection);

                view.setId(View.generateViewId());

                LinearLayout.LayoutParams midSectionChildLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1);

                midSection.addView(view, midSectionChildLayoutParams);

            }
            params = new PercentRelativeLayout.LayoutParams(PercentRelativeLayout.LayoutParams.MATCH_PARENT, PercentRelativeLayout.LayoutParams.WRAP_CONTENT);

            if (topId > 0) params.addRule(PercentRelativeLayout.BELOW, topId);
            if (bottomId > 0) params.addRule(PercentRelativeLayout.ABOVE, bottomId);
            percentRelativeLayout.addView(midSection, params);
        }

        return percentRelativeLayout;

    }

    private void loadBackground(ViewGroup viewGroup) {
        String resourceName = (watermark != null && watermark != "") ? watermark : backgroundimage;
        if (resourceName != null) {

            try {
                Drawable d = Drawable.createFromStream(viewGroup.getContext().getAssets().open(resourceName), null);
                viewGroup.setBackground(d);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private String getImageURL() {
        return "file:///android_asset/" + backgroundimage;
    }



}

