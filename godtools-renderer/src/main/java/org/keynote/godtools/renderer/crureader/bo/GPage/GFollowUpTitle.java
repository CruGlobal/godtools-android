package org.keynote.godtools.renderer.crureader.bo.GPage;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.keynote.godtools.renderer.crureader.R;
import org.keynote.godtools.renderer.crureader.bo.GPage.Base.GBaseTextAttributes;
import org.keynote.godtools.renderer.crureader.bo.GPage.Compat.RenderViewCompat;
import org.keynote.godtools.renderer.crureader.bo.GPage.Views.AutoScaleTextView;

import org.simpleframework.xml.Root;

@Root(name = "followup-title")
public class GFollowUpTitle extends GBaseTextAttributes {
    private static final String TAG = "GFollowUpTitle";

    @Override
    public int render(LayoutInflater inflater, ViewGroup viewGroup, int position) {
        View view = inflater.inflate(R.layout.g_followup_title, viewGroup);
        AutoScaleTextView tv = (AutoScaleTextView)view.findViewById(R.id.g_followup_title_textview);
        tv.setId(RenderViewCompat.generateViewId());
        updateBaseAttributes(tv);
        return tv.getId();
    }
}
