package org.keynote.godtools.renderer.crureader.bo.GPage;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.keynote.godtools.renderer.crureader.R;
import org.keynote.godtools.renderer.crureader.bo.GPage.Base.GModal;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "followup-modal")
public class GFollowupModal extends GModal {
    public static final long DEFAULT_CONTEXT = -1;

    private static final String TAG = "GFollowupModal";

    @Attribute(name = "followup-id")
    public int followUpID;

    @Element(name = "fallback")
    public GFallback fallback;

    @Override
    public int render(LayoutInflater inflater, ViewGroup viewGroup, int position) {
        int viewId = fallback.render(inflater, viewGroup, position);

        View fallBackContainer = viewGroup.findViewById(viewId);
        fallBackContainer.setTag(R.string.fallback, followUpID);

        return viewId;
    }
}
