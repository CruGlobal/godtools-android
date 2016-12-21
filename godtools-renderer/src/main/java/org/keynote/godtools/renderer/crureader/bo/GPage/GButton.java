package org.keynote.godtools.renderer.crureader.bo.GPage;

import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.keynote.godtools.renderer.crureader.R;
import org.keynote.godtools.renderer.crureader.bo.GPage.Base.GBaseButtonAttributes;
import org.keynote.godtools.renderer.crureader.bo.GPage.Base.GBaseTextAttributes;
import org.keynote.godtools.renderer.crureader.bo.GPage.Compat.RenderViewCompat;
import org.keynote.godtools.renderer.crureader.bo.GPage.RenderHelpers.RenderConstants;
import org.keynote.godtools.renderer.crureader.bo.GPage.Views.AutoScaleTextView;

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

    @Attribute(name = "label", required = false)
    public String label;
    private boolean fixed = false;

    @Override
    public int render(final LayoutInflater inflater, ViewGroup viewGroup, final int position) {


        if (mode != null && mode == ButtonMode.big) {
            return methodBig(inflater, viewGroup, position);
        } else {
            return methodDefault(inflater, viewGroup, position);

        }

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
        if(mode == ButtonMode.allurl) {
            buttonTextView.setGravity(Gravity.CENTER_HORIZONTAL|buttonTextView.getGravity());
            outerLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(inflater.getContext(), "all url", Toast.LENGTH_LONG).show();
                }
            });

        }
        else
        {
            RenderConstants.addOnClickPanelListener(position, content, panel, outerLayout);
        }


        return outerLayout.getId();

    }

    private int methodBig(LayoutInflater inflater, ViewGroup viewGroup, final int position) {

        final int imageWidth = 0;
        final int imageHeight = 0;
        int imageId = 0;
        String imageContent = null;
        buttonText.textalign = "center";
        View buttonLayout = inflater.inflate(R.layout.g_big_button, viewGroup);
        final LinearLayout outerLayout = (LinearLayout) buttonLayout.findViewById(R.id.g_big_button_outer_linearlayout);
        FrameLayout imageFrame = (FrameLayout) buttonLayout.findViewById(R.id.g_big_button_image_framelayout);

        if (!firstElementInList) {
            imageFrame.removeAllViews();
        }
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
            final ImageView buttonImageView = (ImageView) imageFrame.findViewById(imageId);
            buttonImageView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    final ImageView buttonImageView = (ImageView) finalImageFrame.findViewById(finalImageId);
                    if (buttonImageView != null) {
                        int imageWidth = buttonImageView.getMeasuredWidth();
                        int imageHeight = buttonImageView.getMeasuredHeight();

                        RenderConstants.addOnClickPanelListener(position, content, finalImageContent, panel, outerLayout, imageWidth, imageHeight);
                    }
                }

            });
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
}
