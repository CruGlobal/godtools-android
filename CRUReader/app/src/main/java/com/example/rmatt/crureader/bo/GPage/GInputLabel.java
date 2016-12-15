package com.example.rmatt.crureader.bo.GPage;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.rmatt.crureader.bo.GPage.Base.GBaseTextAttributes;
import com.example.rmatt.crureader.bo.GPage.Compat.RenderViewCompat;

import org.simpleframework.xml.Root;

/**
 * Created by rmatt on 10/26/2016.
 * <input-label modifier="bold" size="80">Email</input-label>
 */
@Root(name = "input-label")
public class GInputLabel extends GBaseTextAttributes {

    private static final String TAG = "GInputLabel";

    @Override
    public int render(LayoutInflater inflater, ViewGroup viewGroup, int position) {
        TextView v = new TextView(viewGroup.getContext());
        v.setId(RenderViewCompat.generateViewId());
        updateBaseAttributes(v);
        viewGroup.addView(v);
        return v.getId();
    }
}
