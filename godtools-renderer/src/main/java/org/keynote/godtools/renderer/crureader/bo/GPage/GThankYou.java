package org.keynote.godtools.renderer.crureader.bo.GPage;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.keynote.godtools.renderer.crureader.R;
import org.keynote.godtools.renderer.crureader.bo.GPage.Base.GCoordinator;
import org.keynote.godtools.renderer.crureader.bo.GPage.Base.GBaseTextAttributes;
import org.keynote.godtools.renderer.crureader.bo.GPage.Base.GModal;
import org.keynote.godtools.renderer.crureader.bo.GPage.RenderHelpers.RenderConstants;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.ElementListUnion;
import org.simpleframework.xml.Root;

import java.util.ArrayList;

@Root(name = "thank-you")
public class GThankYou extends GModal {

    private static final String TAG = "GThankYou";


    @ElementListUnion({@ElementList(inline = true, required = false, entry = "text", type = GBaseTextAttributes.class),
            @ElementList(inline = true, required = false, entry = "image", type = GImage.class),
            @ElementList(inline = true, required = false, entry = "button-pair", type = GButtonPair.class),
            @ElementList(inline = true, required = false, entry = "link-button", type = GLinkButtonAttributes.class)})
    public ArrayList<GCoordinator> mGCoordinatorArrayList = new ArrayList<GCoordinator>();

    @Override
    public int render(LayoutInflater inflater, ViewGroup viewGroup, int position) {
        int space = Math.round(inflater.getContext().getResources().getDimension(R.dimen.thankyou_element_space));

        return RenderConstants.renderLinearLayoutListWeighted(inflater, viewGroup,
                mGCoordinatorArrayList,
                position,space);
    }
}
