package com.example.rmatt.crureader.bo.GPage;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.rmatt.crureader.R;
import com.example.rmatt.crureader.bo.GPage.Compat.RenderViewCompat;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

/**
 * Created by rmatt on 10/25/2016.
 */
@Root(name = "link-button")
public class GLinkButtonAttributes extends GBaseTextAttributes {

    private static final String TAG = "GLinkButtonAttributes";
    @Attribute(name = "tap-events", required = false)
    public String tapEvents;

    @Override
    public int render(LayoutInflater inflater, ViewGroup viewGroup, int position) {
        View inflated = inflater.inflate(R.layout.g_button_link, viewGroup);
        TextView tv = (TextView) inflated.findViewById(R.id.g_button_link_textview);
        tv.setId(RenderViewCompat.generateViewId());

        updateBaseAttributes(tv);
        return tv.getId();
    }
    @Override
    public boolean shouldUnderline()
    {
        return true;
    }

}
