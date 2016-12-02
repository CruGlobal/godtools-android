package com.example.rmatt.crureader.bo.GPage;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.ActivityOptionsCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.rmatt.crureader.PopupDialogActivity;
import com.example.rmatt.crureader.R;
import com.example.rmatt.crureader.bo.GPage.Compat.RenderViewCompat;
import com.example.rmatt.crureader.bo.GPage.RenderHelpers.RenderConstants;
import com.example.rmatt.crureader.bo.GPage.RenderHelpers.RenderSingleton;

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
    public int render(LayoutInflater inflater, ViewGroup viewGroup, int position) {
        View v = inflater.inflate(R.layout.g_button, viewGroup);
        Button button = (Button) v.findViewById(R.id.g_button_g_button);

        button.setId(RenderViewCompat.generateViewId());
        updateBaseAttributes(button);
        if (buttonText != null && buttonText.content != null) {
            button.setText(buttonText.content);

            button.setTextSize(RenderConstants.getTextSizeFromXMLSize(RenderConstants.DEFAULT_BUTTON_TEXT_SIZE));

            button.setBackgroundColor(RenderSingleton.getInstance().getPositionGlobalColorAsInt(position));

        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Button appCompatButton = (Button) view;

                Context context = view.getContext();

                RenderSingleton.getInstance().gPanelHashMap.put(appCompatButton.getId(), GButton.this.panel);
                int cords[] = {0, 0};

                appCompatButton.getLocationInWindow(cords);

                Intent intent = new Intent(context, PopupDialogActivity.class);
                intent.putExtra(PopupDialogActivity.CONSTANTS_Y_FROM_TOP_FLOAT_EXTRA, (float) cords[1]);
                intent.putExtra(PopupDialogActivity.CONSTANTS_PANEL_HASH_KEY_INT_EXTRA, appCompatButton.getId());
                intent.putExtra(PopupDialogActivity.CONSTANTS_PANEL_TITLE_STRING_EXTRA, appCompatButton.getText() != null ? appCompatButton.getText() : "");

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) context, appCompatButton, context.getString(R.string.inner_ll_transistion_title));

                    ((Activity) context).startActivityForResult(intent, 999, options.toBundle());


                } else {
                    ((Activity) context).startActivityForResult(intent, 999);
                }


            }
        });
        this.updateBaseAttributes(button);
        return button.getId();
    }

}
