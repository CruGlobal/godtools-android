package com.example.rmatt.crureader.bo.GPage;

import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.rmatt.crureader.R;
import com.example.rmatt.crureader.bo.GPage.Base.GBaseButtonAttributes;
import com.example.rmatt.crureader.bo.GPage.Base.GBaseTextAttributes;
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

    @Attribute(name = "label", required = false)
    public String label;
    private boolean fixed = false;

    @Override
    public int render(final LayoutInflater inflater, ViewGroup viewGroup, final int position) {
        View buttonLayout;
        final LinearLayout outerLayout;
        final int imageWidth = 0;
        final int imageHeight = 0;
        FrameLayout imageFrame = null;
        int imageId = 0;
        String imageContent = null;
        if (mode != null && mode == ButtonMode.big) {
            buttonText.textalign = "center";
            buttonLayout = inflater.inflate(R.layout.g_big_button, viewGroup);
            outerLayout = (LinearLayout) buttonLayout.findViewById(R.id.g_big_button_outer_linearlayout);
            imageFrame = (FrameLayout) buttonLayout.findViewById(R.id.g_big_button_image_framelayout);
            if (!firstElementInList) {
                imageFrame.removeAllViews();
            }
            imageFrame.setId(RenderViewCompat.generateViewId());
            if (image != null) {
                imageContent = image.content;
                imageId = image.render(inflater, imageFrame, position);

            }
        } else {
            buttonLayout = inflater.inflate(R.layout.g_button_default, viewGroup);
            outerLayout = (LinearLayout) buttonLayout.findViewById(R.id.g_button_outer_linearlayout);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            outerLayout.setTransitionName(inflater.getContext().getString(R.string.inner_ll_transistion_title));
        }

        AutoScaleTextView buttonTextView = (AutoScaleTextView) buttonLayout.findViewById(R.id.g_button_g_textview);
        outerLayout.setId(RenderViewCompat.generateViewId());

        String content = "";

        this.updateBaseAttributes(outerLayout);

        if (buttonText != null && buttonText.content != null) {

            buttonText.updateBaseAttributes(buttonTextView);
            buttonTextView.setId(RenderViewCompat.generateViewId());
            content = buttonText.content;
        }

        if (imageFrame != null) {

            final String finalContent = content;
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
                        Log.i(TAG, "imageWidth: " + imageWidth);

                        RenderConstants.addOnClickPanelListener(finalContent, finalImageContent, panel, outerLayout, imageWidth, imageHeight);
                    }
                }

            });
        } else {
            RenderConstants.addOnClickPanelListener(content, imageContent, panel, outerLayout, imageWidth, imageHeight);
        }
        return outerLayout.getId();
    }
}
