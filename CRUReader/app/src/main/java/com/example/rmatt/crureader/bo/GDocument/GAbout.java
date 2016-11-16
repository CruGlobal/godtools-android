package com.example.rmatt.crureader.bo.GDocument;

import android.content.Context;
import android.support.percent.PercentRelativeLayout;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.rmatt.crureader.bo.Gtapi;

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
public class GAbout extends Gtapi {

    @Text
    public String content;

    @Attribute
    public String filename;

    @Attribute
    public Boolean translate;

    @Override
    public View render(ViewGroup viewGroup) {
        TextView t = new TextView(viewGroup.getContext());
        t.setText("About");
        return t;
    }
}
