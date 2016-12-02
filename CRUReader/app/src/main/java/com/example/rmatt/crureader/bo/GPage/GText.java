package com.example.rmatt.crureader.bo.GPage;

import android.view.ViewGroup;
import android.widget.TextView;

import org.simpleframework.xml.Root;

@Root(name = "text")
public class GText extends GBaseTextAttributes {
    public static final String TAG = "GText";


    @Override
    public TextView render(ViewGroup viewGroup, int position) {
        setDefaultValues();
        return super.render(viewGroup, position);
    }


    private void setDefaultValues() {
        /*if (textSize == null00.) {
            size = RenderConstants.DEFAULT_TEXT_SIZE;
        }
        if(color == null || color == "")
        {
            color = RenderConstants.DEFAULT_TEXT_COLOR;
        }*/
    }
}