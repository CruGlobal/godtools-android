package com.example.rmatt.crureader.bo.GPage;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.example.rmatt.crureader.R;
import com.example.rmatt.crureader.bo.GPage.Base.GCoordinator;
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
        View inflate = inflater.inflate(R.layout.g_button_pair, viewGroup);
        LinearLayout buttonPairLinearLayout = (LinearLayout)inflate.findViewById(R.id.g_button_pair_linear_layout);
        buttonPairLinearLayout.setId(RenderViewCompat.generateViewId());
        negativeButton.render(inflater, buttonPairLinearLayout, position);
        positiveButton.render(inflater, buttonPairLinearLayout, position);

        return buttonPairLinearLayout.getId();
    }
}
