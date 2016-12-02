package com.example.rmatt.crureader.bo.GDocument;

import android.content.Context;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.rmatt.crureader.bo.Gtapi;

import org.simpleframework.xml.Root;

/**
 * Created by rmatt on 10/24/2016.
 * <instructions gtapi-trx-id="cc494223-8e9e-445e-aaa9-0982be68b65e" translate="true"/>
 */

@Root(name = "instructions")
public class GInstructions extends Gtapi<TextView, ViewGroup> {


    private static final String TAG = "GInstructions";

    @Override
    public TextView render(ViewGroup viewGroup, int position) {
        Context context = viewGroup.getContext();
        TextView v = new TextView(context);
        Log.i(TAG, "render in: " + TAG);
        return v;
    }
}
