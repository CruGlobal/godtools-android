package org.keynote.godtools.renderer.crureader.bo.GPage;

import android.support.percent.PercentRelativeLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import org.keynote.godtools.renderer.crureader.R;
import org.keynote.godtools.renderer.crureader.bo.GPage.Base.GBaseTextAttributes;
import org.keynote.godtools.renderer.crureader.bo.GPage.Base.GCoordinator;
import org.keynote.godtools.renderer.crureader.bo.GPage.Base.GModal;
import org.keynote.godtools.renderer.crureader.bo.GPage.RenderHelpers.ImageAsyncTask;
import org.keynote.godtools.renderer.crureader.bo.GPage.RenderHelpers.RenderConstants;
import org.keynote.godtools.renderer.crureader.bo.GPage.RenderHelpers.RenderSingleton;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.ElementListUnion;
import org.simpleframework.xml.Root;

import java.util.ArrayList;

@Root(name = "page")
public class GPage extends GCoordinator {
    private static final String TAG = "GPage";

    @Attribute(required = false)
    public boolean shadows;

    @Attribute(required = false)
    public String watermark;

    @Element(required = false)
    public GTitle title;

    @Attribute(required = false)
    public Integer bigbuttons;

    @ElementListUnion({@ElementList(inline = true, required = false, entry = "text", type = GBaseTextAttributes.class),
            @ElementList(inline = true, required = false, entry = "button", type = GButton.class)
    })
    public ArrayList<GCoordinator> GCoordinatorArrayList = new ArrayList<GCoordinator>();

    @ElementList(inline = true, required = false, entry = "followup-modal", type = GFollowupModal.class)
    public ArrayList<GModal> followupModalsArrayList = new ArrayList<GModal>();

    @Attribute
    public String color;
    @Attribute(required = false)
    public String buttons;

    @Attribute(required = false, name = "backgroundimage")
    public String backgroundImage;

    @Element(name = "question", required = false)
    public GQuestion gQuestion;



    public String getBackgroundColor() {
        if (color != null) return color;
        else
            return RenderConstants.DEFAULT_BACKGROUND_COLOR;
    }

    @Override
    public int render(LayoutInflater inflater, ViewGroup container, int position) {
        //setDefaultValues();
        View rootView = (View) inflater.inflate(R.layout.g_page, container);
        PercentRelativeLayout percentRelativeLayout = (PercentRelativeLayout) rootView.findViewById(R.id.g_page_main_layout_percentrelativelayout);

        ImageView backgroundImageView = (ImageView) percentRelativeLayout.findViewById(R.id.g_page_background_imageview);

        /* Background color */

        percentRelativeLayout.setBackgroundColor(RenderSingleton.getInstance().getPositionGlobalColorAsInt(position));

        Integer topId = -1;
        Integer bottomId = -1;

        loadBackground(backgroundImageView, position);

        if (title != null) {

            title.render(inflater, percentRelativeLayout, position);

            View topView = percentRelativeLayout.findViewWithTag("top");

            if (topView != null) {
                topId = topView.getId();
            }

        }

        if (gQuestion != null)
            bottomId = gQuestion.render(inflater, percentRelativeLayout, position);

        if (GCoordinatorArrayList != null && GCoordinatorArrayList.size() > 0) {
            int midSectionId;
            if (bigbuttons != null && bigbuttons > 0) {
                midSectionId = RenderConstants.renderLinearLayoutList(inflater, percentRelativeLayout, GCoordinatorArrayList, position);
            } else {
                midSectionId = RenderConstants.renderLinearLayoutListWeighted(inflater, percentRelativeLayout, GCoordinatorArrayList, position, 0);
            }
            if (topId > 0)
                ((RelativeLayout.LayoutParams) percentRelativeLayout.findViewById(midSectionId).getLayoutParams()).addRule(PercentRelativeLayout.BELOW, topId);
            if (bottomId > 0)
                ((RelativeLayout.LayoutParams) percentRelativeLayout.findViewById(midSectionId).getLayoutParams()).addRule(PercentRelativeLayout.ABOVE, bottomId);

        }
        RenderConstants.setUpFollowups(followupModalsArrayList);

        return percentRelativeLayout.getId();

    }

    private void loadBackground(final ImageView iv, final int position) {

        final String resourceName = (watermark != null && watermark.length() > 0) ? watermark : backgroundImage;

        if (resourceName != null) {
            ImageAsyncTask.setImageView(resourceName, iv);
        }
    }


}

