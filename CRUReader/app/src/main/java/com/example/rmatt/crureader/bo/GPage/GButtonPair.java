package com.example.rmatt.crureader.bo.GPage;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.example.rmatt.crureader.bo.GCoordinator;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by rmatt on 10/25/2016.
 */
@Root(name = "button-pair")
public class GButtonPair extends GCoordinator {


    @Element(name = "positive-button", required = false)
    public GSimpleButton positiveButton;

    @Element(name = "negative-button", required = false)
    public GSimpleButton negativeButton;

    @Override
    public LinearLayout render(ViewGroup viewGroup, int position) {
        Context context = viewGroup.getContext();
        LinearLayout ll = new LinearLayout(context);
        ll.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
        ll.setOrientation(LinearLayout.HORIZONTAL);

        ll.addView(negativeButton.render(viewGroup, position), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        ll.addView(positiveButton.render(viewGroup, position), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        return ll;
    }
}
