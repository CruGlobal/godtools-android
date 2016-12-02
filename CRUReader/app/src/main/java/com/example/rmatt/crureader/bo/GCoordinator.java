package com.example.rmatt.crureader.bo;

import android.support.percent.PercentFrameLayout;
import android.support.percent.PercentLayoutHelper;
import android.support.percent.PercentRelativeLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.rmatt.crureader.bo.GPage.RenderHelpers.RenderConstants;

import org.simpleframework.xml.Attribute;

/**
 * Created by rmatt on 10/24/2016.
 */

public abstract class GCoordinator {

    private static final String TAG = "GCoordinator";

    @Attribute(name = "gtapi-trx-id", required = false)
    public String gtapiTrxId;

    @Attribute(name = "tnt-trx-ref-value", required = false)
    public String tntTrxRefValue;

    @Attribute(name = "tnt-trx-translated", required = false)
    public String tntTrxTranslated;

    @Attribute(required = false)
    public String translate;
    @Attribute(required = false, name = "h")
    public Integer height;
    @Attribute(required = false, name = "w")
    public Integer width;
    @Attribute(required = false, name = "align")
    public String layoutAlign;
    @Attribute(required = false)
    public Integer x;
    @Attribute(required = false)
    public Integer y;
    @Attribute(required = false, name = "xoffset")
    public Integer startMargin;
    @Attribute(required = false, name = "yoffset")
    public Integer topMargin;
    @Attribute(name = "x-trailing-offset", required = false)
    public Integer endMargin;

    public void updateBaseAttributes(View view) {
        if (view != null && view.getLayoutParams() instanceof PercentLayoutHelper.PercentLayoutParams) {
            Log.i(TAG, "View is percent layout");

            PercentLayoutHelper.PercentLayoutParams p = (PercentLayoutHelper.PercentLayoutParams) view.getLayoutParams();
            PercentLayoutHelper.PercentLayoutInfo percentLayoutInfo = p.getPercentLayoutInfo();
            applyMargins(percentLayoutInfo);
            applyWidth(percentLayoutInfo);
            applyHeight(percentLayoutInfo);
            //updateAlignment(view);
        } else {
            Log.e(TAG, "View isn't in a percent layout");
        }
    }

    private void applyHeight(PercentLayoutHelper.PercentLayoutInfo percentLayoutInfo) {
        if (height != null && height > 0) {
            percentLayoutInfo.heightPercent = RenderConstants.getVerticalPercent(height);
        }
    }

    private void applyWidth(PercentLayoutHelper.PercentLayoutInfo percentLayoutInfo) {
        if (width != null && width > 0) {
            percentLayoutInfo.widthPercent = RenderConstants.getHorizontalPercent(width);
        }
    }

    /*
    //TODO: override this for imageview.
     */
    protected void updateAlignment(View view) {
        if (view.getLayoutParams() instanceof PercentRelativeLayout.LayoutParams) {
            PercentRelativeLayout.LayoutParams percentRelativeLayoutLayoutParams = (PercentRelativeLayout.LayoutParams) view.getLayoutParams();
            percentRelativeLayoutLayoutParams.addRule(PercentRelativeLayout.CENTER_IN_PARENT);

        } else if (view.getLayoutParams() instanceof PercentFrameLayout.LayoutParams) {

            PercentFrameLayout.LayoutParams percentFrameLayoutLayoutParams = (PercentFrameLayout.LayoutParams) view.getLayoutParams();
            percentFrameLayoutLayoutParams.gravity = Gravity.CENTER;
        }

    }

    private void applyMargins(PercentLayoutHelper.PercentLayoutInfo percentLayoutInfo) {

        if (startMargin != null) {
            percentLayoutInfo.startMarginPercent += RenderConstants.getHorizontalPercent(startMargin);
        }
        if (endMargin != null) {
            percentLayoutInfo.endMarginPercent += RenderConstants.getHorizontalPercent(endMargin);
        }
        if (topMargin != null) {

            percentLayoutInfo.topMarginPercent += RenderConstants.getVerticalPercent(topMargin);
        }
        if (y != null) {
            percentLayoutInfo.topMarginPercent += RenderConstants.getVerticalPercent(y);
        }
        if (x != null) {
            percentLayoutInfo.leftMarginPercent += RenderConstants.getHorizontalPercent(x);
        }

    }


    public abstract int render(LayoutInflater inflater, ViewGroup viewGroup, int position);


}
