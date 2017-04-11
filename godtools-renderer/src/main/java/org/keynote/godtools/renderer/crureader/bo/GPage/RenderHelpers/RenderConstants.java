package org.keynote.godtools.renderer.crureader.bo.GPage.RenderHelpers;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.keynote.godtools.renderer.crureader.PopupDialogActivity;
import org.keynote.godtools.renderer.crureader.R;
import org.keynote.godtools.renderer.crureader.bo.GPage.Base.GBaseButtonAttributes;
import org.keynote.godtools.renderer.crureader.bo.GPage.Base.GCoordinator;
import org.keynote.godtools.renderer.crureader.bo.GPage.Base.GModal;
import org.keynote.godtools.renderer.crureader.bo.GPage.Compat.RenderViewCompat;
import org.keynote.godtools.renderer.crureader.bo.GPage.Event.GodToolsEvent;
import org.keynote.godtools.renderer.crureader.bo.GPage.GInputField;
import org.keynote.godtools.renderer.crureader.bo.GPage.GPanel;
import org.keynote.godtools.renderer.crureader.bo.GPage.IDO.IContexual;
import org.keynote.godtools.renderer.crureader.bo.GPage.Util.SearchableViewUtil;
import org.keynote.godtools.renderer.crureader.bo.GPage.Views.BottomSheetDialog;
import org.keynote.godtools.renderer.crureader.bo.GPage.Views.Space;

import java.util.ArrayList;

public class RenderConstants {

    /* The dimensions w, h,
    */

    public static final float REFERENCE_DEVICE_HEIGHT = 480.0f;
    public static final float REFERENCE_DEVICE_WIDTH = 320.0f;
    /*******************************************************
     * Color constants
     *******************************************************/

    public static final String DEFAULT_BACKGROUND_COLOR = "#FFFFFFFF";
    /***************************************************
     * Font size constants
     ***************************************************/
    public static final float SCALE_TEXT_SIZE = 18.0F;
    public static final int DEFAULT_BUTTON_TEXT_SIZE = 100;
    public static final int DEFAULT_TEXT_SIZE = 60;
    public static final int DEFAULT_NUMBER_TEXT_SIZE = 200;
    public static final int DEFAULT_HEADER_TEXT_SIZE = 90;
    public static final int DEFAULT_SUBHEADER_TEXT_SIZE = 100;
    public static final String DEFAULT_BUTTON_TEXT_ALIGN = "left";
    public static final String DEFAULT_TEXT_COLOR = "#FFFFFFFF";
    public final static String DEFAULT_NAMESPACE = "default_namespace";
    private static final String TAG = "RenderConstants";
    static View.OnClickListener followupOnClick = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            BottomSheetDialog bs = new BottomSheetDialog();
            bs.show(((FragmentActivity) view.getContext()).getSupportFragmentManager(), "test");

        }
    };
    static final View.OnClickListener onClick = new View.OnClickListener() {

        public void onClick(View view) {

            final LinearLayout ll = (LinearLayout) view;
            Context context = view.getContext();
            int distanceTooTop = ll.getTop() + ((View) ll.getParent()).getTop();

            RenderSingleton.getInstance().gPanelHashMap.put(ll.getId(), (GPanel) ll.getTag(R.id.gpanel_tag));

            Intent intent = new Intent(context, PopupDialogActivity.class);
            intent.putExtra(PopupDialogActivity.CONSTANTS_Y_FROM_TOP_FLOAT_EXTRA, (float) distanceTooTop);
            intent.putExtra(PopupDialogActivity.CONSTANTS_PANEL_HASH_KEY_INT_EXTRA, ll.getId());
            intent.putExtra(PopupDialogActivity.CONSTANTS_PANEL_TITLE_STRING_EXTRA, ll.getTag() != null ? ll.getTag().toString() : null);
            intent.putExtra(PopupDialogActivity.CONSTANTS_IMAGE_LOCATION, ll.getTag(R.id.imageurl_tag) != null ? ll.getTag(R.id.imageurl_tag).toString() : null);
            intent.putExtra(PopupDialogActivity.CONSTANTS_IMAGE_WIDTH_INT_EXTRA, view.getMeasuredWidth());
            intent.putExtra(PopupDialogActivity.CONSTANTS_IMAGE_HEIGHT_INT_EXTRA, view.getMeasuredHeight());
            intent.putExtra(PopupDialogActivity.CONSTANTS_POSITION_INT_EXTRA, (int) ll.getTag(R.id.position));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) context, ll, context.getString(R.string.inner_ll_transistion_title));

                ((Activity) context).startActivityForResult(intent, 999, options.toBundle());

            } else {
                ((Activity) context).startActivityForResult(intent, 999);
            }

        }
    };

    //TODO: this is messy clean up.
    static final View.OnClickListener simpleOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            String[] tapEvents = RenderConstants.splitEvents((String) view.getTag());
            int position = (int) view.getTag(R.id.button_position);
            String packageName = RenderSingleton.getInstance().getGDocument().packagename.content;
            boolean valid = true;
            ArrayList<GodToolsEvent> sendList = new ArrayList<GodToolsEvent>();
            for (String tap : tapEvents) {

                if (RenderSingleton.getInstance().gPanelHashMap.get(tap.hashCode()) != null) {
                    GodToolsEvent godToolsEvent = new GodToolsEvent(new GodToolsEvent.EventID(packageName, tap));
                    godToolsEvent.setPosition((Integer) view.getTag(R.id.button_position));
                    sendList.add(godToolsEvent);

                    //BottomSheetDialog bs = BottomSheetDialog.create((Integer) view.getTag(R.id.button_position), tap.hashCode());
                    //bs.show(RenderConstants.searchForFragmentManager(view.getContext()), "test");
                } else if (tap.equalsIgnoreCase("followup:subscribe")) {
                    View parentView = SearchableViewUtil.findFallBackPanel(view);
                    if (parentView != null) {
                        final GodToolsEvent event = new GodToolsEvent(GodToolsEvent.EventID.SUBSCRIBE_EVENT);
                        event.setPackageCode(packageName);
                        event.setLanguage("en");
                        event.setFollowUpId((int) parentView.getTag(R.string.fallback));

                        ArrayList<View> viewsByTag = SearchableViewUtil.getViewsByTag((ViewGroup) parentView, parentView.getContext().getString(R.string.scannable_text_input));

                        // set all input fields as data
                        for (View scannedView : viewsByTag) {
                            if (scannedView instanceof TextInputLayout && scannedView.getTag() != null) {
                                TextInputEditText editText =
                                        (TextInputEditText) (((FrameLayout) ((TextInputLayout) scannedView)
                                                .getChildAt(0)).getChildAt(0));
                                String content = editText.getText() != null ? editText.getText().toString() : "";
                                GInputField gInputField = (GInputField) editText.getTag();
                                if (gInputField.hasValidation()) {
                                    if (gInputField.isValidValue(content)) {
                                        event.setField(gInputField.name, content);
                                    } else {
                                        valid = false;
                                        gInputField.showError((TextInputLayout) scannedView);
                                    }
                                }
                            }
                        }

                        sendList.add(event);

                    }
                } else {
                    GodToolsEvent.EventID eventId = new GodToolsEvent.EventID(packageName, tap);
                    int globalEventPosition = RenderSingleton.getInstance().getGDocument().getGlobalEventPosition(eventId);
                    if (globalEventPosition != GodToolsEvent.INVALID_ID) {
                        GodToolsEvent godToolsEvent = new GodToolsEvent(eventId);
                        godToolsEvent.setPosition(globalEventPosition);
                        godToolsEvent.setPackageCode(packageName);
                        godToolsEvent.setLanguage("en");
                        sendList.add(godToolsEvent);

                    }

                }
            }
            if (valid) {
                for (GodToolsEvent gte : sendList) {
                    EventBus.getDefault().post(gte);
                }
            }

        }
    };

    /*
    The percent of screen height by taking xml value height and dividing by the xml's height basis.
     */
    public static float getVerticalPercent(int height) {
        float verticalPercent = ((float) height / REFERENCE_DEVICE_HEIGHT);
        Log.i(TAG, "height: " + height + " - vertical percent - " + verticalPercent);
        return verticalPercent;
    }

    /*
        The percent of screen width by taking xml value width and dividing by the xml's width basis.
    */
    public static float getHorizontalPercent(int width) {
        float horizontalPercent = ((float) width / REFERENCE_DEVICE_WIDTH);
        Log.i(TAG, "width: " + width + " - horizontal percent - " + horizontalPercent);
        return horizontalPercent;
    }

    public static float getTextSizeFromXMLSize(int xmlSize) {
        if (xmlSize == 0) {
            return SCALE_TEXT_SIZE;
        }
        return (xmlSize * SCALE_TEXT_SIZE) / 100.0F;
    }

    /********************************************
     * Helpers
     * /
     *******************************************/

    public static int getTypefaceFromModifier(String modifier) {
        if (modifier != null) {
            if (modifier.equalsIgnoreCase("italics"))
                return Typeface.ITALIC;
            if (modifier.equalsIgnoreCase("bold"))
                return Typeface.BOLD;
            if (modifier.equalsIgnoreCase("bold-italics"))
                return Typeface.BOLD_ITALIC;
        }
        return Typeface.NORMAL;
    }

    public static int getGravityFromAlign(String align) {
        if (align != null) {
            if (align.equalsIgnoreCase("right"))
                return Gravity.END + Gravity.TOP;
            else if (align.equalsIgnoreCase("center"))
                return Gravity.CENTER;
        }

        return Gravity.START + Gravity.TOP;
    }

    public static int getRelativeLayoutRuleFromAlign(String align) {
        if (align != null) {
            if (align.equalsIgnoreCase("right")) {
                return RelativeLayout.ALIGN_PARENT_RIGHT;
            } else if (align.equalsIgnoreCase("center")) {
                return RelativeLayout.CENTER_IN_PARENT;
            }
        }

        return RelativeLayout.ALIGN_PARENT_LEFT;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static int getTextAlign(String textAlign) {
        if (textAlign != null && !textAlign.equalsIgnoreCase("")) {
            if (textAlign.equalsIgnoreCase("center"))
                return TextView.TEXT_ALIGNMENT_CENTER;
            else if (textAlign.equalsIgnoreCase("right"))
                return TextView.TEXT_ALIGNMENT_VIEW_END;
        }
        return TextView.TEXT_ALIGNMENT_VIEW_START;
    }

    public static int parseColor(String color) {
        if (color != null) {
            return Color.parseColor(color);
        } else {
            return Color.parseColor(DEFAULT_BACKGROUND_COLOR);
        }
    }

    public static int renderLinearLayoutListWeighted(LayoutInflater inflater, ViewGroup viewGroup, ArrayList<GCoordinator> GCoordinatorArrayList, int position) {
        return renderLinearLayoutListWeighted(inflater, viewGroup, GCoordinatorArrayList, position, 0);

    }

    public static int renderLinearLayoutListWeighted(LayoutInflater inflater, ViewGroup viewGroup, ArrayList<GCoordinator> GCoordinatorArrayList, int position, int maxSpace) {
        LinearLayout midSection = new LinearLayout(inflater.getContext());
        midSection.setOrientation(LinearLayout.VERTICAL);
        midSection.setId(RenderViewCompat.generateViewId());
        Space space = new Space(inflater.getContext());
        LinearLayout.LayoutParams evenSpreadDownSpaceLayoutParams;
        evenSpreadDownSpaceLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, maxSpace, 1.0f); //max space is to deal with popups that shouldn't take up the whole container.

        midSection.addView(space, evenSpreadDownSpaceLayoutParams);
        // }
        for (int i = 0; i < GCoordinatorArrayList.size(); i++) {
            GCoordinator tap = GCoordinatorArrayList.get(i);
            if (tap.isManuallyLaidOut()) {
                int layoutBelowId = tap.render(inflater, viewGroup, position);
                ((RelativeLayout.LayoutParams) viewGroup.findViewById(renderLinearLayoutListWeighted(inflater, viewGroup, new ArrayList<>(GCoordinatorArrayList.subList(i + 1, GCoordinatorArrayList.size())), position, maxSpace)).getLayoutParams()).addRule(RelativeLayout.BELOW, layoutBelowId);
                break;
            } else {
                tap.render(inflater, midSection, position); // put into the relative layout if x, y are managing the positioning, or else put into the weight layout.

                space = new Space(inflater.getContext());

                space.setId(RenderViewCompat.generateViewId());
                midSection.addView(space, evenSpreadDownSpaceLayoutParams);

            }
        }

        viewGroup.addView(midSection, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, maxSpace > 0 ? ViewGroup.LayoutParams.WRAP_CONTENT : ViewGroup.LayoutParams.MATCH_PARENT)); //If there is max space wrap_content because we only want to fill a small area.   If it isn't we want to fill the whole available area evenly.
        return midSection.getId();
    }

    public static int renderLinearLayoutList(LayoutInflater inflater, ViewGroup viewGroup, ArrayList<GCoordinator> GCoordinatorArrayList, int position) {
        LinearLayout midSection = new LinearLayout(inflater.getContext());
        midSection.setOrientation(LinearLayout.VERTICAL);
        midSection.setId(RenderViewCompat.generateViewId());
        for (GCoordinator tap : GCoordinatorArrayList) {
            tap.render(inflater, tap.y == null ? midSection : viewGroup, position); // put into the relative layout if x, y are managing the positioning, or else put into the weight layout.

        }

        viewGroup.addView(midSection, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)); //If there is max space wrap_content because we only want to fill a small area.   If it isn't we want to fill the whole available area evenly.
        return midSection.getId();
    }

    public static String[] splitEvents(String tapEvents) {
        String[] splitTapEvents = null;
        if (tapEvents != null && tapEvents.trim() != "") {
            splitTapEvents = tapEvents.split("[,]");
            for (String tapEvent : splitTapEvents) {
                Log.i(TAG, "Tap event post split: " + tapEvent);
            }

        }
        return splitTapEvents;

    }

    public static void setUpFollowups(ArrayList<GModal> followupModalsArrayList) {
        for (GModal modal : followupModalsArrayList) {
            if (modal.listeners != null) {
                Log.i(TAG, "modal listeners: " + modal.listeners + " as hash: " + modal.listeners.hashCode());

                RenderSingleton.getInstance().gPanelHashMap.put(modal.listeners.hashCode(), modal);
            }
        }
    }

    public static int getHorizontalPixels(Integer width) {
        return Math.round(getHorizontalPercent(width) * RenderSingleton.getInstance().screenWidth);
    }

    public static int getVerticalPixels(Integer height) {
        return Math.round(getVerticalPercent(height) * RenderSingleton.getInstance().screenHeight);
    }

    public static void addOnClickPanelListener(int position, String content, GCoordinator panel, View button) {
        if (panel instanceof GPanel) {
            addOnClickPanelListener(position, content, null, panel, button);
        }
    }

    public static void addOnClickPanelListener(int position, String content, String imageUrl, GCoordinator panel, View button) {
        if (panel instanceof GPanel) {
            button.setTag(content);
            button.setTag(R.id.gpanel_tag, panel);

            if (imageUrl != null)
                button.setTag(R.id.imageurl_tag, imageUrl);
            button.setTag(R.id.position, position);
            button.setOnClickListener(onClick);
        }
    }

    public static FragmentManager searchForFragmentManager(Context context) {
        if (context == null)
            return null;
        else if (context instanceof IContexual)
            return ((IContexual) context).getContexualFragmentActivity();
        else if (context instanceof ContextWrapper)
            return searchForFragmentManager(((ContextWrapper) context).getBaseContext());
        return null;
    }

    public static void underline(TextView textView) {
        textView.setPaintFlags(textView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
    }

    public static void sendEmailWithContent(Context context, String subjectLine, String msgBody) {
        try {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:"));
            intent.putExtra(Intent.EXTRA_SUBJECT, subjectLine);
            intent.putExtra(Intent.EXTRA_TEXT, msgBody);
            ///intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(Intent.createChooser(intent, RenderSingleton.getInstance().getAppConfig().getChooseYourEmailProvider()));
        } catch (Exception e) { //TODO: this is bad practice all of these should be removed and defensive coding should be used, discuss with team.
            //TODO: discussed, but hook from crashlytics register into renderer singleton.
            e.printStackTrace();
            showErrorDialog(RenderSingleton.getInstance().getAppConfig().getCannotSendEmail());
        }
    }

    public static void showErrorDialog(String errorMessage) {
        GodToolsEvent gte = new GodToolsEvent(GodToolsEvent.EventID.ERROR_EVENT);
        gte.setErrorContent(errorMessage);
        EventBus.getDefault().post(gte);

    }

    public static void setupUrlButtonHandler(View button, GBaseButtonAttributes.ButtonMode mode, String content, int position) {
        button.setTag(R.id.button_position, position);
        button.setTag(R.id.button_mode, mode.toString());
        button.setTag(R.id.button_content, content);
        button.setOnClickListener(RenderSingleton.getInstance().mLinksOnClick);
    }

    public static void addSimpleButtonOnClickListener(View button, String tapEvents, int position) {
        button.setTag(tapEvents);
        button.setTag(R.id.button_position, position);
        button.setOnClickListener(simpleOnClick);
    }

}
