package com.example.rmatt.crureader.bo.GPage;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.rmatt.crureader.PopupDialogActivity;
import com.example.rmatt.crureader.R;
import com.example.rmatt.crureader.bo.GPage.IDO.IRender;
import com.example.rmatt.crureader.bo.GPage.RenderHelpers.RenderConstants;
import com.example.rmatt.crureader.bo.GPage.RenderHelpers.RenderSingleton;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;


@Root(name = "button")
public class GButton extends GBaseButtonAttributes implements IRender {

    public static final int BACKGROUND_COLOR_KEY = "background".hashCode();

    private static final String TAG = "GButton";

    @Element(required = false)
    public GImage image;

    @Element(name = "buttontext", required = false)
    public GButtonText buttonText;


    @Element(name = "panel", required = false)
    public GPanel panel;


    @Override
    public LinearLayout render(ViewGroup viewGroup) {

        setDefaultValues();

        Context context = viewGroup.getContext();
        LinearLayout outerLayout = new LinearLayout(context);
        outerLayout.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout ll = new LinearLayout(context);
        ll.setGravity(Gravity.CENTER_VERTICAL);
        ll.setClickable(true);
        ll.setId(View.generateViewId());
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        outerLayout.addView(ll);
        if (buttonText != null) {
            TextView v = buttonText.render(viewGroup);
            addLines(v, ll);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                v.setTransitionName(context.getString(R.string.button_tv_transistion_title));
            }

        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ll.setTransitionName(context.getString(R.string.inner_ll_transistion_title));
        }

        ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View ll) {

                RenderSingleton.getInstance().gPanelHashMap.put(ll.getId(), GButton.this.panel);
                Context context = ll.getContext();
                TextView tv = (TextView) ll.findViewById(R.id.button_tv);
                String backgroundColor = (String) ll.getTag(BACKGROUND_COLOR_KEY);

                int cords[] = {0, 0};

                ll.getLocationOnScreen(cords);
                Intent intent = new Intent(context, PopupDialogActivity.class);
                intent.putExtra(PopupDialogActivity.CONSTANTS_Y_FROM_TOP_FLOAT_EXTRA, (float) cords[1]);
                intent.putExtra(PopupDialogActivity.CONSTANTS_PANEL_HASH_KEY_INT_EXTRA, ll.getId());
                intent.putExtra(PopupDialogActivity.CONSTANTS_PANEL_TITLE_STRING_EXTRA, tv != null && tv.getText() != null ? tv.getText() : "");

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                            (Activity) context,
                            new Pair<View, String>(ll, context.getString(R.string.inner_ll_transistion_title)),
                            new Pair<View, String>(tv, context.getString(R.string.button_tv_transistion_title))
                    );
                    ((Activity) context).startActivityForResult(intent, 999, options.toBundle());


                } else {
                    ((Activity) context).startActivityForResult(intent, 999);
                }


            }
        });

        return outerLayout;
    }

    private void setDefaultValues() {
        GButton.this.panel.setBackground(RenderSingleton.getInstance().globalColor);
    }


    private void addLines(View v, LinearLayout ll) {


        Context context = v.getContext();

        ll.addView(getHRView(context, "#404c4c4c"));
        ll.addView(getHRView(context, "#40ffffff"));
        ll.addView(v, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        /*ImageView iv = new ImageView(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            iv.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_expand_less_white_48px, context.getTheme()));
        }

        ll.addView(iv);*/
        ll.addView(getHRView(context, "#404c4c4c"));
        ll.addView(getHRView(context, "#40ffffff"));
    }

    private View getHRView(Context context, String color) {
        View hr = new View(context);
        hr.setBackgroundColor(Color.parseColor(color));
        hr.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, context.getResources().getDisplayMetrics())));
        return hr;
    }

}
