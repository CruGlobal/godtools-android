package org.keynote.godtools.renderer.crureader.bo.GDocument;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import org.keynote.godtools.renderer.crureader.bo.GPage.Base.GCoordinator;
import org.keynote.godtools.renderer.crureader.bo.GPage.Compat.RenderViewCompat;

import org.simpleframework.xml.Root;

@Root(name = "instructions")
public class GInstructions extends GCoordinator {


    private static final String TAG = "GInstructions";

    @Override
    public int render(LayoutInflater inflater, ViewGroup viewGroup, int position) {
        TextView v = new TextView(viewGroup.getContext());
        v.setId(RenderViewCompat.generateViewId());
        updateBaseAttributes(v);
        viewGroup.addView(v);
        return v.getId();
    }
}
