package com.example.rmatt.crureader.bo.GPage;

import android.content.Context;
import android.support.percent.PercentRelativeLayout;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.rmatt.crureader.bo.Gtapi;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.ElementListUnion;
import org.simpleframework.xml.Root;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;

/**
 * Created by rmatt on 10/26/2016.
 */
@Root(name = "fallback")
public class GFallback extends Gtapi {

    @ElementListUnion({@ElementList(inline = true, required = false, entry = "text", type = GText.class),
            @ElementList(inline = true, required = false, entry = "image", type = GImage.class),
            @ElementList(inline = true, required = false, entry = "button-pair", type = GButtonPair.class),
            @ElementList(inline = true, required = false, entry = "link-button", type = GLinkButtonAttributes.class),
            @ElementList(inline = true, required = false, entry = "thank-you", type = GThankYou.class),
            @ElementList(inline = true, required = false, entry = "input-field", type = GInputField.class),
            @ElementList(inline = true, required = false, entry = "followup-body", type = GFollowUpBody.class),
            @ElementList(inline = true, required = false, entry = "followup-title", type = GFollowUpTitle.class)})
    public ArrayList<Gtapi> panelArrayList = new ArrayList<Gtapi>();


    @Override
    public View render(ViewGroup viewGroup) {
        TextView tv = new TextView(viewGroup.getContext());
        Log.i(TAG, "GButtonPair render");
        tv.setText("Not implemented yet");
        return tv;
    }
}
