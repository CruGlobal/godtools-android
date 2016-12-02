package com.example.rmatt.crureader.bo.GPage;

import android.view.ViewGroup;
import android.widget.TextView;

import com.example.rmatt.crureader.R;
import com.example.rmatt.crureader.bo.GPage.RenderHelpers.RenderConstants;

import org.simpleframework.xml.Root;

/**
 * Created by rmatt on 10/19/2016.
 */
@Root(name="buttontext")
public class GButtonText extends GBaseTextAttributes
{

    public static final String TAG = "GText";
    public TextView render(ViewGroup viewGroup, int position) {
        setDefaultValues();
        TextView tv = super.render(viewGroup, position);
        tv.setId(R.id.button_tv);
        return tv;
    }

    private void setDefaultValues() {
        if (textSize == 0 || textalign == "") {
            textSize = RenderConstants.DEFAULT_BUTTON_TEXT_SIZE;
        }

        if(textalign == null || textalign == "")
        {
            textalign = RenderConstants.DEFAULT_BUTTON_TEXT_ALIGN;
        }

        if(textColor == null || textColor == "")
        {
            textColor = RenderConstants.DEFAULT_TEXT_COLOR;
        }
    }

}