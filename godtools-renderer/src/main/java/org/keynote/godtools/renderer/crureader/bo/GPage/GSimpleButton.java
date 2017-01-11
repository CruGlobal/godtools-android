package org.keynote.godtools.renderer.crureader.bo.GPage;

import android.graphics.Color;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.keynote.godtools.renderer.crureader.R;
import org.keynote.godtools.renderer.crureader.bo.GPage.Base.GBaseButtonAttributes;
import org.keynote.godtools.renderer.crureader.bo.GPage.Compat.RenderViewCompat;
import org.keynote.godtools.renderer.crureader.bo.GPage.RenderHelpers.RenderConstants;
import org.keynote.godtools.renderer.crureader.bo.GPage.RenderHelpers.RenderSingleton;
import org.keynote.godtools.renderer.crureader.bo.GPage.Views.AutoScaleButtonView;
import org.simpleframework.xml.Root;

@Root(name = "button")
public class GSimpleButton extends GBaseButtonAttributes {

    private static final String TAG = "GButton";
    public String textColor;
    @org.simpleframework.xml.Text(required = false)
    public String content;
    private boolean shouldUnderline = false;

    @Override
    public int render(LayoutInflater inflater, ViewGroup viewGroup, final int position) {
        View inflate = null;
        if (mode != null) {
            switch (mode) {
                case big:
                    break;
                case url:
                    inflate = inflater.inflate(R.layout.g_button_url, viewGroup);
                    defaultColor(position);
                    break;
                case allurl: //TODO: rm
                    inflate = inflater.inflate(R.layout.g_button_url, viewGroup);
                    break;
                case email: //TODO: rm
                    inflate = inflater.inflate(R.layout.g_button_url, viewGroup);
                    break;
                case link:
                    inflate = inflater.inflate(R.layout.g_button_link, viewGroup);
                    shouldUnderline = true;
                    break;

            }
        } else {
            inflate = inflater.inflate(R.layout.g_button_simple, viewGroup);
            defaultColor(position);
        }

        AutoScaleButtonView button = (AutoScaleButtonView) inflate.findViewById(R.id.g_simple_button);

        if (textColor != null)
            button.setTextColor(Color.parseColor(textColor));
        if (content != null)
            button.setText(content);

        applyTextSize(button);
        updateBaseAttributes(button);

        button.setId(RenderViewCompat.generateViewId());

        if (tapEvents != null && !tapEvents.equalsIgnoreCase("")) {

            RenderConstants.addSimpleButtonOnClickListener(button, tapEvents, position);
        } else {
            RenderConstants.setupUrlButtonHandler(button, mode, content, position);
        }

        return button.getId();

    }

    public void defaultColor(int position) {
        textColor = RenderSingleton.getInstance().getPositionGlobalColorAsString(position);
    }

    private void applyTextSize(AutoScaleButtonView buttonViewCast) {
        if (textSize != null) {
            buttonViewCast.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
        } else {
            buttonViewCast.setTextSize(TypedValue.COMPLEX_UNIT_SP, 100.0f);
        }

        if (shouldUnderline()) {
            RenderConstants.underline(buttonViewCast);
        }
    }

    @Override
    public boolean shouldUnderline() {
        return shouldUnderline;
    }
}
