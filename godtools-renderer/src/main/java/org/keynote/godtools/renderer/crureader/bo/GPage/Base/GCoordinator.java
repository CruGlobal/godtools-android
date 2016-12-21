package org.keynote.godtools.renderer.crureader.bo.GPage.Base;

import android.support.percent.PercentRelativeLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import org.keynote.godtools.renderer.crureader.bo.GPage.RenderHelpers.RenderConstants;
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
    public String yoffset;
    @Attribute(name = "x-trailing-offset", required = false)
    public Integer endMargin;

    public Integer getTopMargin()
    {
        if(yoffset != null && !yoffset.trim().equalsIgnoreCase(""))
        {
            return Integer.valueOf(yoffset.trim());
        }
        return 0;
    }

    public void updateBaseAttributes(View view) {
        if (view != null && view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            Log.i(TAG, "View is percent layout");

            ViewGroup.MarginLayoutParams percentLayoutInfo = (ViewGroup.MarginLayoutParams) view.getLayoutParams();

            // PercentLayoutHelper.PercentLayoutInfo percentLayoutInfo = p.getPercentLayoutInfo();
            applyMargins(percentLayoutInfo);
            applyWidth(percentLayoutInfo);
            applyHeight(percentLayoutInfo);
            updateAlignment(view);
        } else {
            Log.e(TAG, "View isn't in a percent layout");
        }
    }

    private void applyHeight(ViewGroup.MarginLayoutParams percentLayoutInfo) {
        if (height != null && height > 0) {
            percentLayoutInfo.height = RenderConstants.getVerticalPixels(height);
        }
    }

    private void applyWidth(PercentRelativeLayout.MarginLayoutParams percentLayoutInfo) {
        if (width != null && width > 0) {
            percentLayoutInfo.width = RenderConstants.getHorizontalPixels(width);
        }
    }

    /*
    //TODO: override this for imageview.
     */
    protected void updateAlignment(View view) {

        if (layoutAlign != null) {
            if (view.getLayoutParams() instanceof PercentRelativeLayout.LayoutParams) {
                PercentRelativeLayout.LayoutParams percentRelativeLayoutLayoutParams = (PercentRelativeLayout.LayoutParams) view.getLayoutParams();
                percentRelativeLayoutLayoutParams.addRule(RenderConstants.getRelativeLayoutRuleFromAlign(layoutAlign));

            }
            else if(view.getLayoutParams() instanceof FrameLayout.LayoutParams)
            {
                FrameLayout.LayoutParams frameLayouts = (FrameLayout.LayoutParams)view.getLayoutParams();
                frameLayouts.gravity = RenderConstants.getGravityFromAlign(layoutAlign);
            }
        }
    }

    private void applyMargins(ViewGroup.MarginLayoutParams percentLayoutInfo) {

        if (percentLayoutInfo.topMargin == -1) percentLayoutInfo.topMargin = 0;
        if (percentLayoutInfo.leftMargin == -1) percentLayoutInfo.leftMargin = 0;
        if (percentLayoutInfo.rightMargin == -1) percentLayoutInfo.rightMargin = 0;

        if (startMargin != null) {
            percentLayoutInfo.leftMargin += RenderConstants.getHorizontalPixels(startMargin);
            Log.i(TAG, "RenderConstants get horizontal screen size");
        }
        if (endMargin != null) {
            percentLayoutInfo.rightMargin += RenderConstants.getHorizontalPixels(endMargin);
        }
        if (getTopMargin() != null) {

            percentLayoutInfo.topMargin += RenderConstants.getVerticalPixels(getTopMargin());
        }
        if (y != null) {

            percentLayoutInfo.topMargin += RenderConstants.getVerticalPixels(y);
        }
        if (x != null) {
            percentLayoutInfo.leftMargin += RenderConstants.getHorizontalPixels(x);
        }


    }

    public boolean hasSpace() {
        return true;
    }

    public boolean isManuallyLaidOut() {
        return y != null;
    }


    public abstract int render(LayoutInflater inflater, ViewGroup viewGroup, int position);

    protected boolean firstElementInList;

    public void setFirstElementInList(boolean firstElementInList) {
        this.firstElementInList = firstElementInList;
    }

    public boolean shouldUnderline() { return false; }
}
