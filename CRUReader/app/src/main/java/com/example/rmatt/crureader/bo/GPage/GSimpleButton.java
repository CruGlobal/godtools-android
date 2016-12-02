package com.example.rmatt.crureader.bo.GPage;

import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.rmatt.crureader.R;
import com.example.rmatt.crureader.bo.GPage.Compat.RenderViewCompat;
import com.example.rmatt.crureader.bo.GPage.RenderHelpers.RenderConstants;
import com.example.rmatt.crureader.bo.GPage.RenderHelpers.RenderSingleton;

import org.simpleframework.xml.Root;


@Root(name = "button")
public class GSimpleButton extends GBaseButtonAttributes {


    private static final String TAG = "GButton";

    @org.simpleframework.xml.Text(required = false)
    public String content;

    @Override
    public int render(LayoutInflater inflater, ViewGroup viewGroup, int position) {

        ContextThemeWrapper newContext = new ContextThemeWrapper(viewGroup.getContext(), R.style.Widget_GodTools_Button);

        AppCompatButton button = new AppCompatButton(newContext);
        button.setTextColor(RenderSingleton.getInstance().getPositionGlobalColorAsInt(position));
        button.setText(content);
        button.setTextSize(TypedValue.COMPLEX_UNIT_SP, RenderConstants.getTextSizeFromXMLSize(100));
        button.setId(RenderViewCompat.generateViewId());
        button.setTag(tapEvents);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(view.getContext(), (String) view.getTag(), Toast.LENGTH_LONG).show();
                String[] tapEvents = RenderConstants.getTapEvents((String) view.getTag());
                for (String tap : tapEvents) {
                    Log.i(TAG, "tap: " + tap + " tap as hash " + tap.hashCode());
                    if (RenderSingleton.getInstance().gPanelHashMap.get(tap.hashCode()) != null) {
                        Log.i(TAG, "tap contained in map start activity");

                    }
                }
            }
        });

        viewGroup.addView(button);
        return button.getId();

    }
}
