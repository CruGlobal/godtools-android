package com.example.rmatt.crureader.bo.GPage;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.example.rmatt.crureader.R;
import com.example.rmatt.crureader.bo.GPage.Base.GCoordinator;
import com.example.rmatt.crureader.bo.GPage.Base.GBaseTextAttributes;
import com.example.rmatt.crureader.bo.GPage.Base.GModal;
import com.example.rmatt.crureader.bo.GPage.RenderHelpers.RenderConstants;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.ElementListUnion;
import org.simpleframework.xml.Root;

import java.util.ArrayList;

/**
 * Created by rmatt on 10/26/2016.
 * This is a bottom sheet
 */
@Root(name = "fallback")
public class GFallback extends GCoordinator {

    @ElementListUnion({@ElementList(inline = true, required = false, entry = "text", type = GBaseTextAttributes.class),
            @ElementList(inline = true, required = false, entry = "image", type = GImage.class),
            @ElementList(inline = true, required = false, entry = "button-pair", type = GButtonPair.class),
            @ElementList(inline = true, required = false, entry = "link-button", type = GLinkButtonAttributes.class),
            @ElementList(inline = true, required = false, entry = "input-field", type = GInputField.class),
            @ElementList(inline = true, required = false, entry = "followup-body", type = GFollowUpBody.class),
            @ElementList(inline = true, required = false, entry = "followup-title", type = GFollowUpTitle.class)})
    public ArrayList<GCoordinator> GCoordinatorArrayList = new ArrayList<GCoordinator>();

    @ElementList(inline = true, required = false, entry = "thank-you", type = GThankYou.class)
    public ArrayList<GModal> GCoordinatorFollowupList = new ArrayList<>();


    @Override
    public int render(LayoutInflater inflater, ViewGroup viewGroup, int position) {
        Context context = viewGroup.getContext();
        RenderConstants.setUpFollowups(GCoordinatorFollowupList);
        return RenderConstants.renderLinearLayoutListWeighted(inflater,
                viewGroup, GCoordinatorArrayList, position, Math.round(inflater.getContext().getResources().getDimension(R.dimen.fallback_element_space)));
    }

}
