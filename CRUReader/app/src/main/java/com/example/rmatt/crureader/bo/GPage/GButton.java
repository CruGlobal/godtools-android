package com.example.rmatt.crureader.bo.GPage;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.ActivityOptionsCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.example.rmatt.crureader.PopupDialogActivity;
import com.example.rmatt.crureader.R;
import com.example.rmatt.crureader.bo.GPage.Compat.RenderViewCompat;
import com.example.rmatt.crureader.bo.GPage.RenderHelpers.RenderSingleton;
import com.example.rmatt.crureader.bo.GPage.Views.AutoScaleTextView;

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

    /*
          if (textSize == 0 || textalign == "") {
            textSize = RenderConstants.DEFAULT_BUTTON_TEXT_SIZE;
        }

        if(textalign == null || textalign == "")
        {
            textalign = RenderConstants.DEFAULT_BUTTON_TEXT_ALIGN;
        }

        if(textColor == null || textColor == "")
        {
            textColor = RenderConstants.DEFAULT_TEXT_COLOR;
        }
     */


    @Element(name = "panel", required = false)
    public GPanel panel;


    @Override
    public int render(final LayoutInflater inflater, ViewGroup viewGroup, final int position) {
        View v = inflater.inflate(R.layout.g_button, viewGroup);
        final LinearLayout buttonLinearLayout = (LinearLayout) v.findViewById(R.id.g_button_outer_linearlayout);
        AutoScaleTextView buttonTextView = (AutoScaleTextView)v.findViewById(R.id.g_button_g_textview);


//        Space space = (Space)v.findViewById(R.id.g_button_space_bottom);
        buttonLinearLayout.setId(RenderViewCompat.generateViewId());
        //space.setId(RenderViewCompat.generateViewId());
        this.updateBaseAttributes(buttonLinearLayout);

        if (buttonText != null && buttonText.content != null) {
            buttonText.updateBaseAttributes(buttonTextView);
            buttonTextView.setId(RenderViewCompat.generateViewId());
            buttonLinearLayout.setTag(buttonText.content);
            //button.setText(buttonText.content);

            //button.setTextSize(RenderConstants.getTextSizeFromXMLSize(RenderConstants.DEFAULT_BUTTON_TEXT_SIZE));

           // button.setBackgroundColor(RenderSingleton.getInstance().getPositionGlobalColorAsInt(position));

        }

        buttonLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final LinearLayout ll = (LinearLayout) view;
                Context context = view.getContext();
                int distanceTooTop = ll.getTop() + ((View) ll.getParent()).getTop();
                RenderSingleton.getInstance().gPanelHashMap.put(ll.getId(), GButton.this.panel);

                Intent intent = new Intent(context, PopupDialogActivity.class);
                intent.putExtra(PopupDialogActivity.CONSTANTS_Y_FROM_TOP_FLOAT_EXTRA, (float) distanceTooTop);
                intent.putExtra(PopupDialogActivity.CONSTANTS_PANEL_HASH_KEY_INT_EXTRA, ll.getId());
                intent.putExtra(PopupDialogActivity.CONSTANTS_PANEL_TITLE_STRING_EXTRA, ll.getTag() != null ? ll.getTag().toString() : "");

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) context, ll, context.getString(R.string.inner_ll_transistion_title));

                    ((Activity) context).startActivityForResult(intent, 999, options.toBundle());


                } else {
                    ((Activity) context).startActivityForResult(intent, 999);
                }



            }
        });
        return buttonLinearLayout.getId();
    }
    private boolean fixed = false;
}
