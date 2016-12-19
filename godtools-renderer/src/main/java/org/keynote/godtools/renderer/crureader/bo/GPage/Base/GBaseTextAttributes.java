package org.keynote.godtools.renderer.crureader.bo.GPage.Base;

import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.keynote.godtools.renderer.crureader.R;
import org.keynote.godtools.renderer.crureader.bo.GPage.Compat.RenderViewCompat;
import org.keynote.godtools.renderer.crureader.bo.GPage.RenderHelpers.RenderConstants;
import org.keynote.godtools.renderer.crureader.bo.GPage.RenderHelpers.RenderSingleton;
import org.keynote.godtools.renderer.crureader.bo.GPage.Views.AutoScaleTextView;
import org.simpleframework.xml.Attribute;

import me.grantland.widget.AutofitHelper;

/**
 * Created by rmatt on 10/31/2016.
 */

public class GBaseTextAttributes extends GCoordinator {


    private static final String TAG = "GBaseTextAttributes";
    @org.simpleframework.xml.Text(required = false, empty = "")
    public String content;
    @Attribute(required = false)
    public String textalign;
    @Attribute(required = false, name = "size")
    public Integer textSize;
    @Attribute(required = false, name = "color")
    public String textColor;
    @Attribute(required = false, name = "modifier")
    public String textModifier;


    //TODO: cast T to skip unboxing.
    @Override
    public void updateBaseAttributes(View view) {
        super.updateBaseAttributes(view);
        if (view != null && view instanceof AutoScaleTextView) {

            AutoScaleTextView textViewCast = (AutoScaleTextView) view;
            applyTextColor(textViewCast);
            applyTextModifier(textViewCast);
            applyTextSize(textViewCast);
            applyTextAlign(textViewCast);
            applyTextContent(textViewCast);
        }
        else
            new Exception("Must be autoscaletextview to extend GBaseTextAttributes");
    }

    private void applyTextContent(AutoScaleTextView textViewCast) {
        if(content != null && !content.equalsIgnoreCase("")) {
            textViewCast.setVisibility(View.VISIBLE);
            textViewCast.setText(content);
        }
        else
        {
            textViewCast.setVisibility(View.GONE);
        }
    }

    private void applyTextAlign(AutoScaleTextView textViewCast) {
        if (textalign != null && !textalign.equalsIgnoreCase("")) {

            RenderViewCompat.textViewAlign(textViewCast, textalign);

            layoutAlign = textalign;
            updateAlignment(textViewCast);
        }
    }

    private void applyTextSize(AutoScaleTextView textViewCast) {
        if (width != null && height != null && textSize == null)
        {
            Log.e(TAG, "Should scale this~!~ + " + textViewCast.getText() + textViewCast.getId());
            AutofitHelper.create(textViewCast);
        }
        else {
            if (textSize != null) {
                textViewCast.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
            } else {
                textViewCast.setTextSize(TypedValue.COMPLEX_UNIT_SP, 100.0f);
            }
        }

    }

    @Override
    public int render(LayoutInflater inflater, ViewGroup viewGroup, int position) {


        AutoScaleTextView textView = new AutoScaleTextView(inflater.getContext(), null, R.style.AutoScaleTextView);
        textView.setId(RenderViewCompat.generateViewId());
        textView.setGravity(Gravity.TOP);
        textView.setTextColor(Color.WHITE);

        viewGroup.addView(textView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        updateBaseAttributes(textView);
        return textView.getId();
    }


    public void applyTextColor(AutoScaleTextView textView) {
        if (textColor != null && !textColor.equalsIgnoreCase("")) {
            textView.setTextColor(RenderConstants.parseColor(textColor));
        }
    }

    public void applyTextModifier(AutoScaleTextView textView) {
        if (textModifier != null && !textModifier.equalsIgnoreCase(""))
            textView.setTypeface(Typeface.defaultFromStyle(RenderConstants.getTypefaceFromModifier(textModifier)));

        if(shouldUnderline())
            RenderConstants.underline(textView);

    }


    public void defaultColor(int position) {
        if(textColor == null) textColor = RenderSingleton.getInstance().getPositionGlobalColorAsString(position);
    }

    public Integer getTextSize() {
        return textSize;
    }

    @Override
    public boolean hasSpace()
    {
        return false;
    }


}
