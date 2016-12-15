package com.example.rmatt.crureader.bo.GPage;

import android.graphics.Color;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.rmatt.crureader.R;
import com.example.rmatt.crureader.bo.GPage.Base.GBaseButtonAttributes;
import com.example.rmatt.crureader.bo.GPage.Compat.RenderViewCompat;
import com.example.rmatt.crureader.bo.GPage.RenderHelpers.RenderConstants;
import com.example.rmatt.crureader.bo.GPage.RenderHelpers.RenderSingleton;
import com.example.rmatt.crureader.bo.GPage.Views.AutoScaleButtonView;
import com.example.rmatt.crureader.bo.GPage.Views.BottomSheetDialog;

import org.simpleframework.xml.Root;


@Root(name = "button")
public class GSimpleButton extends GBaseButtonAttributes {

    private boolean shouldUnderline = false;
    private static final String TAG = "GButton";

    public String textColor;

    @org.simpleframework.xml.Text(required = false)
    public String content;

    @Override
    public int render(LayoutInflater inflater, ViewGroup viewGroup, final int position) {
        View inflate = null;
        if (mode != null) {
            switch (mode) {
                case big:
                    break;
                case url:
                    break;
                case allurl:
                    break;
                case email:
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
        button.setTag(tapEvents);
        button.setTag(R.integer.gpanel_posiiton, position);
        button.setOnClickListener(new View.OnClickListener() {
                                      @Override
                                      public void onClick(View view) {

                                          AutoScaleButtonView autoScaleButtonView = (AutoScaleButtonView) view;
                                          // Toast.makeText(view.getContext(), (String) view.getTag(), Toast.LENGTH_LONG).show();
                                          String[] tapEvents = RenderConstants.getTapEvents((String) view.getTag());
                                          for (String tap : tapEvents) {
                                              Log.i(TAG, "tap: " + tap + " tap as hash " + tap.hashCode());
                                              if (RenderSingleton.getInstance().gPanelHashMap.get(tap.hashCode()) != null) {
                                                  Log.i(TAG, "tap contained in map start activity");

                                                  BottomSheetDialog bs = BottomSheetDialog.create((Integer) view.getTag(R.integer.gpanel_posiiton), tap.hashCode());

                                                  bs.show(RenderConstants.searchForFragmentManager(view.getContext()), "test");

                                              }
                                          }
                                      }
                                  }

        );

        //viewGroup.addView(button);
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

        if(shouldUnderline())
        {
            RenderConstants.underline(buttonViewCast);
        }
    }

    @Override
    public boolean shouldUnderline()
    {
        return shouldUnderline;
    }
}
