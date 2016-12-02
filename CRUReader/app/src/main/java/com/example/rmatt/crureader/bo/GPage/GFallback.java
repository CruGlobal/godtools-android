package com.example.rmatt.crureader.bo.GPage;

import android.util.Log;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.rmatt.crureader.bo.GCoordinator;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.ElementListUnion;
import org.simpleframework.xml.Root;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;

/**
 * Created by rmatt on 10/26/2016.
 * This is a bottom sheet
 */
@Root(name = "fallback")
public class GFallback extends GCoordinator<TextView, ViewGroup> {

    @ElementListUnion({@ElementList(inline = true, required = false, entry = "text", type = GBaseTextAttributes.class),
            @ElementList(inline = true, required = false, entry = "image", type = GImage.class),
            @ElementList(inline = true, required = false, entry = "button-pair", type = GButtonPair.class),
            @ElementList(inline = true, required = false, entry = "link-button", type = GLinkButtonAttributes.class),
            @ElementList(inline = true, required = false, entry = "thank-you", type = GThankYou.class),
            @ElementList(inline = true, required = false, entry = "input-field", type = GInputField.class),
            @ElementList(inline = true, required = false, entry = "followup-body", type = GFollowUpBody.class),
            @ElementList(inline = true, required = false, entry = "followup-title", type = GFollowUpTitle.class)})
    public ArrayList<GCoordinator> panelArrayList = new ArrayList<GCoordinator>();


    @Override
    public TextView render(ViewGroup viewGroup, int position) {
        TextView tv = new TextView(viewGroup.getContext());
        Log.i(TAG, "GButtonPair render");
        tv.setText("Not implemented yet");
        return tv;
    }

    @Override
    public ViewGroup group(ViewGroup viewGroup, int position) {
        return null;
    }
}
