package com.example.rmatt.crureader.bo.GPage;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.AppCompatButton;
import android.view.View;
import android.view.ViewGroup;

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
    public AppCompatButton render(ViewGroup viewGroup, int position) {

        ContextThemeWrapper newContext = new ContextThemeWrapper(viewGroup.getContext(), R.style.Widget_GodTools_Button_Standard);

        AppCompatButton appCompatButton = new AppCompatButton(newContext);

        appCompatButton.setId(RenderViewCompat.generateViewId());
       /* Context context = viewGroup.getContext();
        LinearLayout outerLayout = new LinearLayout(context);
        outerLayout.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout ll = new LinearLayout(context);
        ll.setGravity(Gravity.CENTER_VERTICAL);
        ll.setClickable(true);
        ll.setId(RenderViewCompat.generateViewId());
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        outerLayout.addView(ll);*/

        //appCompatButton.setBac
        //buttonText.render(appCompatButton, position);
        if (buttonText != null && buttonText.content != null) {
            appCompatButton.setText(buttonText.content);
           /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                appCompatButton.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
            }*/
            appCompatButton.setTextSize(RenderConstants.getTextSizeFromXMLSize(RenderConstants.DEFAULT_BUTTON_TEXT_SIZE));

            appCompatButton.setBackgroundColor(RenderSingleton.getInstance().getPositionGlobalColorAsInt(position));

        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            appCompatButton.setTransitionName(viewGroup.getContext().getString(R.string.inner_ll_transistion_title));
        }

        appCompatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AppCompatButton appCompatButton = (AppCompatButton) view;

                Context context = ((ContextThemeWrapper) appCompatButton.getContext()).getBaseContext();

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

        return appCompatButton;
    }

    @Override
    public ViewGroup group(ViewGroup viewGroup, int position) {
        return null;
    }


//    private void addLines(View v, LinearLayout ll) {
//
//
//        Context context = v.getContext();
//
//
//        //TODO: redo this
//        ll.addView(getHRView(context, "#404c4c4c"));
//        ll.addView(getHRView(context, "#40ffffff"));
//        ll.addView(v, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
//        ll.addView(getHRView(context, "#404c4c4c"));
//        ll.addView(getHRView(context, "#40ffffff"));
//    }

 /*   private View getHRView(Context context, String color) {
        View hr = new View(context);
        hr.setBackgroundColor(Color.parseColor(color));
        hr.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, context.getResources().getDisplayMetrics())));
        return hr;
    }*/

}
