package com.example.rmatt.crureader.bo.GDocument;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.rmatt.crureader.bo.GPage.Base.GCoordinator;
import com.example.rmatt.crureader.bo.GPage.Compat.RenderViewCompat;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Text;

/**
 * Created by rmatt on 10/24/2016.
 * <p>
 * <about filename="6c31dc4c-1cc8-47e1-aa34-1c86049af426.xml"
 * gtapi-trx-id="0c41ea49-9905-46f0-bfa9-400d3807545c" translate="true">About
 * </about>
 */

@Root(name = "about")
public class GAbout extends GCoordinator {

    @Text
    public String content;

    @Attribute
    public String filename;


    @Attribute(required = false)
    public String thumb;

    @Override
    public int render(LayoutInflater inflater, ViewGroup viewGroup, int position) {
        TextView v = new TextView(viewGroup.getContext());
        v.setId(RenderViewCompat.generateViewId());
        updateBaseAttributes(v);
        viewGroup.addView(v);
        return v.getId();
    }
}
