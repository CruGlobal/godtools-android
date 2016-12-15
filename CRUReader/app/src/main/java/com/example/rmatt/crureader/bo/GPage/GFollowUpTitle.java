package com.example.rmatt.crureader.bo.GPage;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.rmatt.crureader.R;
import com.example.rmatt.crureader.bo.GPage.Base.GBaseTextAttributes;
import com.example.rmatt.crureader.bo.GPage.Compat.RenderViewCompat;
import com.example.rmatt.crureader.bo.GPage.Views.AutoScaleTextView;

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
        View view = inflater.inflate(R.layout.g_followup_title, viewGroup);
        AutoScaleTextView tv = (AutoScaleTextView)view.findViewById(R.id.g_followup_title_textview);
        tv.setId(RenderViewCompat.generateViewId());
        updateBaseAttributes(tv);
        return tv.getId();
    }
}
