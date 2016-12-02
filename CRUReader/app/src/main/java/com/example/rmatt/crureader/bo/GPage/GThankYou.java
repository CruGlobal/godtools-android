package com.example.rmatt.crureader.bo.GPage;

import android.content.Context;
import android.support.percent.PercentRelativeLayout;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.example.rmatt.crureader.bo.GCoordinator;
import com.example.rmatt.crureader.bo.GPage.RenderHelpers.RenderConstants;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.ElementListUnion;
import org.simpleframework.xml.Root;

import java.util.ArrayList;

/**
 * Created by rmatt on 10/26/2016.
 * <thank-you listeners="follow-up-send">
 * <text modifier="bold" size="100" textalign="center" x-trailing-offset="30"
 * xoffset="30">Thank you
 * </text>
 * <text size="100" textalign="left" x-trailing-offset="30" xoffset="30">Check your
 * email soon for your first study in following Jesus Christ.
 * </text>
 * <text size="100" textalign="left" x-trailing-offset="30" xoffset="30">If you don't
 * receive it, please check your spam folder.
 * </text>
 * <link-button tap-events="follow-up-thank-you-done" textalign="center">Done
 * </link-button>
 * </thank-you>
 */

/*
    This is an activity
 */
@Root(name = "thank-you")
public class GThankYou extends GCoordinator {

    private static final String TAG = "GThankYou";
    @Attribute
    public String listeners;

    @ElementListUnion({@ElementList(inline = true, required = false, entry = "text", type = GBaseTextAttributes.class),
            @ElementList(inline = true, required = false, entry = "image", type = GImage.class),
            @ElementList(inline = true, required = false, entry = "button-pair", type = GButtonPair.class),
            @ElementList(inline = true, required = false, entry = "link-button", type = GLinkButtonAttributes.class)})
    public ArrayList<GCoordinator> GCoordinatorArrayList = new ArrayList<GCoordinator>();

    @Override
    public int render(LayoutInflater inflater, ViewGroup viewGroup, int position) {
        Context context = viewGroup.getContext();
        RenderConstants.renderLinearLayoutListWeighted(inflater, (PercentRelativeLayout) viewGroup, GCoordinatorArrayList, position);
        return 0;
    }
}
