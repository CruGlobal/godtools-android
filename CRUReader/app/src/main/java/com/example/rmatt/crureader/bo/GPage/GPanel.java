package com.example.rmatt.crureader.bo.GPage;

import android.content.Context;
import android.support.percent.PercentRelativeLayout;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.example.rmatt.crureader.bo.GPage.Base.GBaseTextAttributes;
import com.example.rmatt.crureader.bo.GPage.Base.GCoordinator;
import com.example.rmatt.crureader.bo.GPage.RenderHelpers.RenderConstants;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.ElementListUnion;
import org.simpleframework.xml.Root;

import java.util.ArrayList;

/**
 * Created by rmatt on 10/18/2016.
 */
@Root(name = "panel")
public class GPanel extends GCoordinator {

    private static final String TAG = "GPanel";
    /*
    This defaults all text to center for inside panel.
     */
    @Attribute(required = false, name = "textalign")
    public String textAlign;
    @ElementListUnion({@ElementList(inline = true, required = false, entry = "text", type = GBaseTextAttributes.class),
            @ElementList(inline = true, required = false, entry = "image", type = GImage.class),
            @ElementList(inline = true, required = false, entry = "button-pair", type = GButtonPair.class),
            @ElementList(inline = true, required = false, entry = "link-button", type = GLinkButtonAttributes.class),
            @ElementList(inline = true, required = false, entry = "button", type = GSimpleButton.class)})
    public ArrayList<GCoordinator> GCoordinatorArrayList = new ArrayList<GCoordinator>();

    @Override
    public int render(LayoutInflater inflater, ViewGroup viewGroup, int position) {
        Context context = viewGroup.getContext();

        return RenderConstants.renderLinearLayoutListWeighted(inflater,
                (PercentRelativeLayout) viewGroup, GCoordinatorArrayList, position, 40);
    }

}
