package org.keynote.godtools.renderer.crureader.bo.GPage;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.keynote.godtools.renderer.crureader.bo.GPage.Base.GBaseTextAttributes;
import org.keynote.godtools.renderer.crureader.bo.GPage.Base.GCoordinator;
import org.keynote.godtools.renderer.crureader.bo.GPage.RenderHelpers.RenderConstants;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.ElementListUnion;
import org.simpleframework.xml.Root;

import java.util.ArrayList;

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
                viewGroup, GCoordinatorArrayList, position, 40);
    }

}
