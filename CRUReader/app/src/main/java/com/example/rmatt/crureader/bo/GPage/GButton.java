package com.example.rmatt.crureader.bo.GPage;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.example.rmatt.crureader.R;
import com.example.rmatt.crureader.bo.GPage.Compat.RenderViewCompat;
import com.example.rmatt.crureader.bo.GPage.RenderHelpers.RenderConstants;
import com.example.rmatt.crureader.bo.GPage.Views.AutoScaleTextView;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;


@Root(name = "button")
public class GButton extends GBaseButtonAttributes {

    public static final int BACKGROUND_COLOR_KEY = "background".hashCode();

    private static final String TAG = "GButton";

    @Element(required = false)
    public GImage image;

    @Element(name = "buttontext", required = false)
    public GBaseTextAttributes buttonText;

    @Element(name = "panel", required = false)
    public GPanel panel;

    @Attribute(name ="label", required = false)
    public String label;


    @Override
    public int render(final LayoutInflater inflater, ViewGroup viewGroup, final int position) {
        View v = inflater.inflate(R.layout.g_button_default, viewGroup);
        final LinearLayout buttonLinearLayout = (LinearLayout) v.findViewById(R.id.g_button_outer_linearlayout);
        AutoScaleTextView buttonTextView = (AutoScaleTextView)v.findViewById(R.id.g_button_g_textview);
        buttonLinearLayout.setId(RenderViewCompat.generateViewId());

        String content = "";

        this.updateBaseAttributes(buttonLinearLayout);

        if (buttonText != null && buttonText.content != null) {
            buttonText.updateBaseAttributes(buttonTextView);
            buttonTextView.setId(RenderViewCompat.generateViewId());
            content = buttonText.content;
        }
        RenderConstants.addOnClickPanelListener(content, panel, buttonLinearLayout);

        return buttonLinearLayout.getId();
    }



    private boolean fixed = false;
}
