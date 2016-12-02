package com.example.rmatt.crureader.bo.GPage;

import android.util.Log;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.rmatt.crureader.bo.GCoordinator;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by rmatt on 10/26/2016.
 * <input-field name="email" type="email" x-trailing-offset="30" xoffset="30" yoffset="10">
 * <input-label modifier="bold" size="80">Email</input-label>
 * <input-placeholder>john.doe@gmail.com</input-placeholder>
 * </input-field>
 */
@Root(name = "input-field")
public class GInputField extends GCoordinator<TextView, ViewGroup> {

    private static final String TAG = "GInputField";
    @Attribute(name = "valid-format", required = false)
    public String validFormat;

    @Attribute
    public String type;

    @Attribute
    public String name;

    @Element(name = "input-label", required = false)
    public GBaseTextAttributes inputLabel;

    @Element(name = "input-placeholder", required = false)
    public GInputPlaceholder inputPlaceholder;

    @Override
    public TextView render(ViewGroup viewGroup, int position) {
        TextView v = new TextView(viewGroup.getContext());
        v.setText("GInputField");
        Log.i(TAG, "render in GInputField");
        return v;
    }

    @Override
    public ViewGroup group(ViewGroup viewGroup, int position) {
        return null;
    }
}
