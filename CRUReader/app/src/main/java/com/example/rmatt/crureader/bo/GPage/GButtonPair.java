package com.example.rmatt.crureader.bo.GPage;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.example.rmatt.crureader.bo.GCoordinator;
import com.example.rmatt.crureader.bo.GPage.Compat.RenderViewCompat;

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
    public int render(LayoutInflater inflater, ViewGroup viewGroup, int position) {
        Context context = viewGroup.getContext();
        LinearLayout ll = new LinearLayout(context);
        ll.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
        ll.setOrientation(LinearLayout.HORIZONTAL);
        negativeButton.render(inflater, ll, position);
        positiveButton.render(inflater, ll, position);
        ll.setId(RenderViewCompat.generateViewId());
        return ll.getId();
    }
}
