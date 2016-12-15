package com.example.rmatt.crureader.bo.GDocument;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.rmatt.crureader.bo.GPage.Base.GCoordinator;
import com.example.rmatt.crureader.bo.GPage.Compat.RenderViewCompat;

import org.simpleframework.xml.Root;

/**
 * Created by rmatt on 10/24/2016.
 * <instructions gtapi-trx-id="cc494223-8e9e-445e-aaa9-0982be68b65e" translate="true"/>
 */

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
