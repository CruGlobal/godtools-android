package com.example.rmatt.crureader.bo.GPage;

import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.rmatt.crureader.R;
import com.example.rmatt.crureader.bo.GPage.Compat.RenderViewCompat;
import com.example.rmatt.crureader.bo.GPage.RenderHelpers.RenderConstants;
import com.example.rmatt.crureader.bo.GPage.RenderHelpers.RenderSingleton;
import com.example.rmatt.crureader.bo.GPage.Views.AutoScaleButtonView;
import com.example.rmatt.crureader.bo.GPage.Views.BottomSheetDialog;

import org.simpleframework.xml.Root;

import me.grantland.widget.AutofitHelper;


@Root(name = "button")
public class GSimpleButton extends GBaseButtonAttributes {


    private static final String TAG = "GButton";

    public String textColor;

    @org.simpleframework.xml.Text(required = false)
    public String content;

    @Override
    public int render(LayoutInflater inflater, ViewGroup viewGroup, int position) {
        View inflate = inflater.inflate(R.layout.g_button_simple, viewGroup);

        AutoScaleButtonView button = (AutoScaleButtonView)inflate.findViewById(R.id.g_simple_button);

        defaultColor(position);
        button.setTextColor(Color.parseColor(textColor));
        button.setText(content);
        applyTextSize(button);

        button.setId(RenderViewCompat.generateViewId());
        button.setTag(tapEvents);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               // Toast.makeText(view.getContext(), (String) view.getTag(), Toast.LENGTH_LONG).show();
                String[] tapEvents = RenderConstants.getTapEvents((String) view.getTag());
                for (String tap : tapEvents) {
                    Log.i(TAG, "tap: " + tap + " tap as hash " + tap.hashCode());
                    if (RenderSingleton.getInstance().gPanelHashMap.get(tap.hashCode()) != null) {
                        Log.i(TAG, "tap contained in map start activity");

                        BottomSheetDialog bs = new BottomSheetDialog();
                        bs.show(((FragmentActivity)view.getContext()).getSupportFragmentManager(), "test");

                    }
                }
            }
        });

        //viewGroup.addView(button);
        return button.getId();

    }

    public void defaultColor(int position) {
        textColor = RenderSingleton.getInstance().getPositionGlobalColorAsString(position);
    }

    private void applyTextSize(AutoScaleButtonView buttonViewCast) {
        if (width != null && height != null)
        {
            Log.e(TAG, "Should scale this~!~ + " + buttonViewCast.getText() + buttonViewCast.getId());
            AutofitHelper.create(buttonViewCast);
        }
        else {
            if (textSize != null) {
                buttonViewCast.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
            } else {
                buttonViewCast.setTextSize(TypedValue.COMPLEX_UNIT_SP, 100.0f);
            }
        }

    }

}
