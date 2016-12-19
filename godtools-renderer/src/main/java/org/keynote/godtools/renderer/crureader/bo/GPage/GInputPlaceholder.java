package org.keynote.godtools.renderer.crureader.bo.GPage;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import org.keynote.godtools.renderer.crureader.bo.GPage.Base.GCoordinator;
import org.keynote.godtools.renderer.crureader.bo.GPage.Compat.RenderViewCompat;

import org.simpleframework.xml.Root;
import org.simpleframework.xml.Text;

/**
 * Created by rmatt on 10/26/2016.
 * <input-placeholder>john.doe@gmail.com</input-placeholder>
 */
@Root(name = "input-placeholder")
public class GInputPlaceholder extends GCoordinator {

    private static final String TAG = "GInputPlaceholder";
    @Text
    public String content;

    @Override
    public int render(LayoutInflater inflater, ViewGroup viewGroup, int position) {
        TextView v = new TextView(viewGroup.getContext());
        v.setId(RenderViewCompat.generateViewId());
        updateBaseAttributes(v);
        viewGroup.addView(v);
        return v.getId();
    }
}
