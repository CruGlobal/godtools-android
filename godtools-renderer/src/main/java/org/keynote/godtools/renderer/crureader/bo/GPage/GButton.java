package org.keynote.godtools.renderer.crureader.bo.GPage;

import android.graphics.Color;
import android.os.Build;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import org.keynote.godtools.renderer.crureader.R;
import org.keynote.godtools.renderer.crureader.bo.GPage.Base.GBaseButtonAttributes;
import org.keynote.godtools.renderer.crureader.bo.GPage.Base.GBaseTextAttributes;
import org.keynote.godtools.renderer.crureader.bo.GPage.Compat.RenderViewCompat;
import org.keynote.godtools.renderer.crureader.bo.GPage.RenderHelpers.RenderConstants;
import org.keynote.godtools.renderer.crureader.bo.GPage.RenderHelpers.RenderSingleton;
import org.keynote.godtools.renderer.crureader.bo.GPage.Views.AutoScaleButtonView;
import org.keynote.godtools.renderer.crureader.bo.GPage.Views.AutoScaleTextView;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "button")
public class GButton extends GBaseButtonAttributes {

    public static final int BACKGROUND_COLOR_KEY = "background".hashCode();

    private static final String TAG = "GButton";

    public String textColor;

    @Element(required = false)
    public GImage image;

    @Element(name = "buttontext", required = false)
    public GBaseTextAttributes buttonText;

    @Element(name = "panel", required = false)
    public GPanel panel;

    @Attribute(name = "label", required = false)
    public String label;
    private boolean fixed = false;
    private String text;

    @Override
    public int render(final LayoutInflater inflater, ViewGroup viewGroup, final int position) {

        if (mode != null && mode == ButtonMode.big) {
            return methodBig(inflater, viewGroup, position);
        }
        if (mode == null) {

            return methodDefault(inflater, viewGroup, position);
        } else {
            return methodURL(inflater, viewGroup, position);
        }

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

    private int methodURL(LayoutInflater inflater, ViewGroup viewGroup, int position) {

        View inflate = inflater.inflate(R.layout.g_button_url, viewGroup);
        defaultColor(position);

        AutoScaleButtonView button = (AutoScaleButtonView) inflate.findViewById(R.id.g_simple_button);

        if (textColor != null)
            button.setTextColor(Color.parseColor(textColor));

        applyTextSize(button);
        updateBaseAttributes(button);

        button.setId(RenderViewCompat.generateViewId());
        button.setTag(tapEvents);

        button.setText(text);
        RenderConstants.setupUrlButtonHandler(button, mode, text, position);

        return button.getId();
    }

    private int methodDefault(final LayoutInflater inflater, ViewGroup viewGroup, int position) {

        FrameLayout imageFrame = null;
        String content = "";
        View buttonLayout = inflater.inflate(R.layout.g_button_default, viewGroup);

        LinearLayout outerLayout = (LinearLayout) buttonLayout.findViewById(R.id.g_button_outer_linearlayout);
        this.updateBaseAttributes(outerLayout);

        outerLayout.setId(RenderViewCompat.generateViewId());

        AutoScaleTextView buttonTextView = (AutoScaleTextView) buttonLayout.findViewById(R.id.g_button_g_textview);

        //************* Sets up top and bottom dividers asynch loading them into the view, because some avenues don't need them ***********//

        lazyInflateDividers(inflater, buttonLayout, true, true);

        //************** end dividers****************************************//

        setOuterLayoutToTransistion(outerLayout);
        this.updateBaseAttributes(outerLayout);

        content = setButtonText(buttonTextView);
        if (panel != null) {
            RenderConstants.addOnClickPanelListener(position, content, panel, outerLayout);
        } else {

            buttonTextView.setGravity(Gravity.CENTER);
            outerLayout.findViewById(R.id.g_button_expand_imageview).setVisibility(View.GONE);
            buttonTextView.setText(text);
            buttonTextView.setVisibility(View.VISIBLE);
            buttonTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 100);
            if (buttonTextView.getLayoutParams() instanceof FrameLayout.LayoutParams) {
                FrameLayout.LayoutParams frameLayouts = (FrameLayout.LayoutParams) buttonTextView.getLayoutParams();
                frameLayouts.gravity = RenderConstants.getGravityFromAlign(layoutAlign);
            }
            RenderConstants.setupUrlButtonHandler(buttonTextView, mode, text, position);
        }

        return outerLayout.getId();

    }

    private int methodBig(LayoutInflater inflater, ViewGroup viewGroup, final int position) {

        int imageId = 0;
        String imageContent = null;
        buttonText.textalign = "center";
        View buttonLayout = inflater.inflate(R.layout.g_big_button, viewGroup);
        final LinearLayout outerLayout = (LinearLayout) buttonLayout.findViewById(R.id.g_big_button_outer_linearlayout);
        FrameLayout imageFrame = (FrameLayout) buttonLayout.findViewById(R.id.g_big_button_image_framelayout);
//
//        if (!firstElementInList) {
//            imageFrame.removeAllViews();
//        }
        imageFrame.setId(RenderViewCompat.generateViewId());
        if (image != null) {
            imageContent = image.content;
            imageId = image.render(inflater, imageFrame, position);

        }

        setOuterLayoutToTransistion(outerLayout);
        lazyInflateDividers(inflater, buttonLayout, false, true);

        AutoScaleTextView buttonTextView = (AutoScaleTextView) buttonLayout.findViewById(R.id.g_button_g_textview);
        outerLayout.setId(RenderViewCompat.generateViewId());

        this.updateBaseAttributes(outerLayout);

        final String content = setButtonText(buttonTextView);

        if (imageFrame != null) {

            final String finalImageContent = imageContent;
            final FrameLayout finalImageFrame = imageFrame;
            final int finalImageId = imageId;
            RenderConstants.addOnClickPanelListener(position, content, finalImageContent, panel, outerLayout);
        }
        return outerLayout.getId();

    }

    private String setButtonText(AutoScaleTextView buttonTextView) {
        if (buttonText != null && buttonText.content != null) {

            buttonText.updateBaseAttributes(buttonTextView);
            buttonTextView.setId(RenderViewCompat.generateViewId());
            buttonTextView.setText(buttonText.content);
            return buttonText.content;
        }
        return "";

    }

    private void setOuterLayoutToTransistion(View view) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            view.setTransitionName(view.getContext().getString(R.string.inner_ll_transistion_title));
        }
    }

    private void lazyInflateDividers(LayoutInflater inflater, View buttonLayout, boolean topShown, boolean bottomShown) {
        ViewStub bottomButtonDivider = (ViewStub) buttonLayout.findViewById(R.id.g_button_bottom_horizontal_rule);
        ViewStub topButtonDivider = (ViewStub) buttonLayout.findViewById(R.id.g_button_top_horizontal_rule);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            bottomButtonDivider.setLayoutInflater(inflater);
            topButtonDivider.setLayoutInflater(inflater);
        }
        if (topShown) topButtonDivider.inflate();
        if (bottomShown)
            bottomButtonDivider.inflate();
    }

    public void setText(String text) {
        this.text = text;
    }

    public void defaultColor(int position) {
        textColor = RenderSingleton.getInstance().getPositionGlobalColorAsString(position);
    }

}
