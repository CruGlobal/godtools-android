package org.keynote.godtools.renderer.crureader.bo.GPage.Base;

import android.os.Build;
import android.support.percent.PercentRelativeLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import org.keynote.godtools.renderer.crureader.bo.GPage.Compat.RenderViewCompat;
import org.keynote.godtools.renderer.crureader.bo.GPage.Event.GodToolsEvent;
import org.keynote.godtools.renderer.crureader.bo.GPage.RenderHelpers.RenderConstants;
import org.simpleframework.xml.Attribute;

import java.util.List;

public abstract class GCoordinator {
    private static final String TAG = "GCoordinator";

    public List<GodToolsEvent.EventID> eventListeners;

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
    public Integer translationX;
    @Attribute(required = false, name = "yoffset")
    public String translationY;
    @Attribute(name = "x-trailing-offset", required = false)
    public Integer endMargin;
   // protected boolean firstElementInList;

    public Integer getTranslationY() {
        if (translationY != null && !translationY.trim().equalsIgnoreCase("")) {
            return Integer.valueOf(translationY.trim());
        }
        return 0;
    }

    public void updateBaseAttributes(View view) {
        if (view != null && view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {

            ViewGroup.MarginLayoutParams percentLayoutInfo = (ViewGroup.MarginLayoutParams) view.getLayoutParams();

            // PercentLayoutHelper.PercentLayoutInfo percentLayoutInfo = p.getPercentLayoutInfo();
            applyMargins(percentLayoutInfo);
            applyWidth(percentLayoutInfo);
            applyHeight(percentLayoutInfo);
            updateAlignment(view);
            translateViews(view);
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
        if (width != null && width > 0 && !RenderViewCompat.SDK_ICS_OR_PRIOR) {

            percentLayoutInfo.width = RenderConstants.getHorizontalPixels(width);

        }
    }

    /*
    //TODO: override this for imageview.
     */
    protected void updateAlignment(View view) {
        if (layoutAlign != null) {
            if (view.getLayoutParams() instanceof RelativeLayout.LayoutParams) {
                RelativeLayout.LayoutParams relativeLayoutLayoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                relativeLayoutLayoutParams.addRule(RenderConstants.getRelativeLayoutRuleFromAlign(layoutAlign));
            } else if (view.getLayoutParams() instanceof PercentRelativeLayout.LayoutParams) {
                PercentRelativeLayout.LayoutParams percentRelativeLayoutLayoutParams = (PercentRelativeLayout.LayoutParams) view.getLayoutParams();
                percentRelativeLayoutLayoutParams.addRule(RenderConstants.getRelativeLayoutRuleFromAlign(layoutAlign));
            } else if (view.getLayoutParams() instanceof FrameLayout.LayoutParams) {
                FrameLayout.LayoutParams frameLayouts = (FrameLayout.LayoutParams) view.getLayoutParams();
                frameLayouts.gravity = RenderConstants.getGravityFromAlign(layoutAlign);
            }
        }
    }

    private void translateViews(View view) {
        if (translationX != null && translationX != 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                view.setTranslationX(RenderConstants.getHorizontalPixels(translationX));
            }
        }

        if (translationY != null && getTranslationY() != 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                view.setTranslationY(RenderConstants.getVerticalPixels(getTranslationY() / 4));
            }
        }

    }

    private void applyMargins(ViewGroup.MarginLayoutParams percentLayoutInfo) {

        if (percentLayoutInfo.topMargin == -1)
            percentLayoutInfo.topMargin = 0;
        if (percentLayoutInfo.leftMargin == -1)
            percentLayoutInfo.leftMargin = 0;
        if (percentLayoutInfo.rightMargin == -1)
            percentLayoutInfo.rightMargin = 0;

        if (endMargin != null) {
            percentLayoutInfo.rightMargin += RenderConstants.getHorizontalPixels(endMargin);
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

    public boolean shouldUnderline() {
        return false;
    }
}
