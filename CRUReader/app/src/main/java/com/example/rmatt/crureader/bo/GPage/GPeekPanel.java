package com.example.rmatt.crureader.bo.GPage;

import android.view.ViewGroup;
import android.widget.TextView;

import com.example.rmatt.crureader.bo.GPage.RenderHelpers.RenderSingleton;

import org.simpleframework.xml.Element;

/**
 * Created by rmatt on 12/1/2016.
 */

@Element(name = "peekpanel")
public class GPeekPanel extends GBaseTextAttributes {

    private static final String TAG = "PeakPanel";


    @Override
    public TextView render(ViewGroup viewGroup, int position) {
        setDefaultValues(position);
        return super.render(viewGroup, position);
    }

    private void setDefaultValues(int position) {
        /*if (super.textSize == 0) {
            size = RenderConstants.DEFAULT_SUBHEADER_TEXT_SIZE;
        }*/

        if (textColor == null || textColor == "") {
            textColor = RenderSingleton.getInstance().getPositionGlobalColorAsString(position);
        }
    }
}
