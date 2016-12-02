package com.example.rmatt.crureader.bo.GPage;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.rmatt.crureader.bo.GPage.Compat.RenderViewCompat;

import org.simpleframework.xml.Root;

/**
 * Created by rmatt on 10/26/2016.
 * <followup-title modifier="bold" x="0" x-trailing-offset="20" xoffset="20">Jesus has come
 * into your life as he promised.
 * </followup-title>
 */
@Root(name = "followup-title")
public class GFollowUpTitle extends GBaseTextAttributes {

    private static final String TAG = "GFollowUpTitle";


    @Override
    public int render(LayoutInflater inflater, ViewGroup viewGroup, int position) {
        TextView v = new TextView(viewGroup.getContext());
        v.setId(RenderViewCompat.generateViewId());
        updateBaseAttributes(v);
        viewGroup.addView(v);
        return v.getId();
    }
}
