package org.keynote.godtools.renderer.crureader.bo.GPage;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.keynote.godtools.renderer.crureader.R;
import org.keynote.godtools.renderer.crureader.bo.GPage.Base.GBaseTextAttributes;
import org.keynote.godtools.renderer.crureader.bo.GPage.Compat.RenderViewCompat;
import org.keynote.godtools.renderer.crureader.bo.GPage.Views.AutoScaleTextView;

import org.simpleframework.xml.Root;

@Root(name = "followup-body")
public class GFollowUpBody extends GBaseTextAttributes {
    @Override
    public int render(LayoutInflater inflater, ViewGroup viewGroup, int position) {
        View inflatedView = inflater.inflate(R.layout.g_followup_body, viewGroup);

        AutoScaleTextView tv = (AutoScaleTextView)inflatedView.findViewById(R.id.g_followup_body_textview);
        tv.setId(RenderViewCompat.generateViewId());
        updateBaseAttributes(tv);
        return tv.getId();
    }
}
