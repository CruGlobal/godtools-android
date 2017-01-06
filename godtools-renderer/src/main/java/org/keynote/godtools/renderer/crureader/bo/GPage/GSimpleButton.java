package org.keynote.godtools.renderer.crureader.bo.GPage;

import android.graphics.Color;
import android.support.design.widget.TextInputEditText;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.EventBus;
import org.keynote.godtools.renderer.crureader.R;
import org.keynote.godtools.renderer.crureader.bo.GPage.Base.GBaseButtonAttributes;
import org.keynote.godtools.renderer.crureader.bo.GPage.Compat.RenderViewCompat;
import org.keynote.godtools.renderer.crureader.bo.GPage.Event.GodToolsEvent;
import org.keynote.godtools.renderer.crureader.bo.GPage.RenderHelpers.RenderConstants;
import org.keynote.godtools.renderer.crureader.bo.GPage.RenderHelpers.RenderSingleton;
import org.keynote.godtools.renderer.crureader.bo.GPage.Util.SearchableViewUtil;
import org.keynote.godtools.renderer.crureader.bo.GPage.Views.AutoScaleButtonView;
import org.keynote.godtools.renderer.crureader.bo.GPage.Views.BottomSheetDialog;
import org.simpleframework.xml.Root;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Root(name = "button")
public class GSimpleButton extends GBaseButtonAttributes {

    private static final String TAG = "GButton";
    public String textColor;
    @org.simpleframework.xml.Text(required = false)
    public String content;
    private boolean shouldUnderline = false;

    @Override
    public int render(LayoutInflater inflater, ViewGroup viewGroup, final int position) {
        View inflate = null;
        if (mode != null) {
            switch (mode) {
                case big:
                    break;
                case url:
                    inflate = inflater.inflate(R.layout.g_button_url, viewGroup);
                    defaultColor(position);
                    break;
                case allurl: //TODO: rm
                    inflate = inflater.inflate(R.layout.g_button_url, viewGroup);
                    break;
                case email: //TODO: rm
                    inflate = inflater.inflate(R.layout.g_button_url, viewGroup);
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
        button.setTag(R.id.gpanel_posiiton, position);

        if (tapEvents != null && !tapEvents.equalsIgnoreCase("")) {

            button.setTag(tapEvents);
            button.setTag(R.id.button_position, position);
            button.setOnClickListener(new View.OnClickListener() {
                                          @Override
                                          public void onClick(View view) {

                                              String[] tapEvents = RenderConstants.getTapEvents((String) view.getTag());

                                              for (String tap : tapEvents) {

                                                  if (RenderSingleton.getInstance().gPanelHashMap.get(tap.hashCode()) != null) {
                                                      BottomSheetDialog bs = BottomSheetDialog.create((Integer) view.getTag(R.id.gpanel_posiiton), tap.hashCode());
                                                      bs.show(RenderConstants.searchForFragmentManager(view.getContext()), "test");
                                                  } else if (tap.equalsIgnoreCase("followup:subscribe")) {
                                                      View parentView = SearchableViewUtil.findFallBackPanel(view);
                                                      if (parentView != null) {
                                                          final GodToolsEvent event = new GodToolsEvent(GodToolsEvent.EventID.SUBSCRIBE_EVENT);
                                                          event.setPackageCode("kgp");
                                                          event.setLanguage("en");
                                                          event.setFollowUpId((int) parentView.getTag(R.string.fallback));

                                                          ArrayList<View> viewsByTag = SearchableViewUtil.getViewsByTag((ViewGroup) parentView, parentView.getContext().getString(R.string.scannable_text_input));
                                                          Map<String, String> mFields = new HashMap<>();
                                                          // set all input fields as data
                                                          if (mFields != null) {
                                                              for (View scannedView : viewsByTag) {

                                                                  if (scannedView instanceof TextInputEditText && scannedView.getTag(R.id.textinput_name) != null) {
                                                                      Log.i(TAG, "Scannedview name: " + scannedView.getTag(R.id.textinput_name) + " data: " + ((TextInputEditText) scannedView).getText().toString());
                                                                      event.setField((String) scannedView.getTag(R.id.textinput_name), ((TextInputEditText) scannedView).getText().toString());
                                                                  }
                                                              }
                                                          }

                                                          // send subscribe event
                                                          EventBus.getDefault().post(event);

                                                          Log.i(TAG, "View has been clicked");
                                                          parentView.setBackgroundColor(parentView.getContext().getResources().getColor(android.R.color.holo_orange_dark));
                                                      }

                                                  }
                                              }
                                          }
                                      }

            );
        } else {
            RenderConstants.setupUrlButtonHandler(button, mode, content);
        }

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

        if (shouldUnderline()) {
            RenderConstants.underline(buttonViewCast);
        }
    }

    @Override
    public boolean shouldUnderline() {
        return shouldUnderline;
    }
}
