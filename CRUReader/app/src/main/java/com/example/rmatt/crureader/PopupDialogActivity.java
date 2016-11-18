package com.example.rmatt.crureader;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.percent.PercentFrameLayout;
import android.support.percent.PercentLayoutHelper;
import android.support.v4.app.DialogFragment;
import android.text.Html;
import android.transition.Transition;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.rmatt.crureader.bo.GPage.GPanel;
import com.example.rmatt.crureader.bo.GPage.GText;
import com.example.rmatt.crureader.bo.GPage.RenderHelpers.RenderConstants;
import com.example.rmatt.crureader.bo.GPage.RenderHelpers.RenderSingleton;
import com.example.rmatt.crureader.bo.Gtapi;

/**
 * Created by rmatt on 11/14/2016.
 */
public class PopupDialogActivity extends Activity {

    public static final String CONSTANTS_BACKGROUND_COLOR_STRING_EXTRA = "backgroundcolor";
    public static final String CONSTANTS_PERCENT_FROM_TOP_EXTRA = "percentfromtop";
    public static final String CONSTANTS_DISTANCE_FROM_BOTTOM = "distancefrombottom";
    public static final String CONSTANTS_PANEL_HASH_KEY_INT_EXTRA = "panelhash";
    public static final String CONSTANTS_PANEL_TITLE_STRING_EXTRA = "title";
    public static final String CONSTANTS_Y_FROM_TOP_FLOAT_EXTRA = "Y";
    private boolean fixed = false;
    public static final String TAG = "PopupDialogActivity";
    LinearLayout ll;
    TextView descriptionTV;
    int distanceToBottomOfScreen;
    FrameLayout extraContent;
    TextView tv;
    GPanel gPanel;
    public float Y;
    int screenHeight;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        descriptionTV.setAnimation(null);
        descriptionTV.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_popupdialog);
        DisplayMetrics metrics = PopupDialogActivity.this.getResources().getDisplayMetrics();
        screenHeight = metrics.heightPixels;
        String backgroundColor = this.getIntent().getExtras().getString(CONSTANTS_BACKGROUND_COLOR_STRING_EXTRA);
        Y = this.getIntent().getExtras().getFloat(CONSTANTS_Y_FROM_TOP_FLOAT_EXTRA);
       /// distanceToBottomOfScreen = this.getIntent().getExtras().getInt(CONSTANTS_DISTANCE_FROM_BOTTOM);
        int hashkey = this.getIntent().getExtras().getInt(CONSTANTS_PANEL_HASH_KEY_INT_EXTRA);
        String title = this.getIntent().getExtras().getString(CONSTANTS_PANEL_TITLE_STRING_EXTRA);
        gPanel = RenderSingleton.getInstance().gPanelHashMap.get(hashkey);

        descriptionTV = (TextView) findViewById(R.id.description_tv);
        extraContent = (FrameLayout) findViewById(R.id.extra_wrapper_fl);
        //Log.i(TAG, "|============ percent from top============| " + percentFromTop);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getSharedElementEnterTransition().addListener(new Transition.TransitionListener() {
                @Override
                public void onTransitionStart(Transition transition) {
                    System.out.println("#¤ PopupDialogActivity.onTransitionStart - Enter");
                }

                @Override
                public void onTransitionEnd(Transition transition) {
                    System.out.println("#¤ PopupDialogActivity.onTransitionEnd - Enter");
                    fadeIn();

                }

                @Override
                public void onTransitionCancel(Transition transition) {

                }

                @Override
                public void onTransitionPause(Transition transition) {

                }

                @Override
                public void onTransitionResume(Transition transition) {

                }
            });
        } else {
            fadeIn();
        }


        ll = (LinearLayout) findViewById(R.id.popup_innerLinearLayout);
        PercentLayoutHelper.PercentLayoutParams layoutParams = (PercentLayoutHelper.PercentLayoutParams) ll.getLayoutParams();
        PercentLayoutHelper.PercentLayoutInfo percentLayoutInfo = layoutParams.getPercentLayoutInfo();

        ll.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                Log.i(TAG, "LL.getMeasuredHeight(): " + ll.getMeasuredHeight());
                Log.i(TAG, "LL.getHeight(): " + ll.getHeight());
                Log.i(TAG, "LL getY(): " + ll.getY());
                Log.i(TAG, "descriptionTV.getMeasuredHeight(): " + descriptionTV.getMeasuredHeight());
                Log.i(TAG, "descriptionTV.getHeight(): " + descriptionTV.getHeight());
                Log.i(TAG, "descriptionTV distanceToBottomOfScreen(): " + descriptionTV.getY());

                DisplayMetrics metrics = PopupDialogActivity.this.getResources().getDisplayMetrics();
                int height = metrics.heightPixels;

                ll.measure(View.MeasureSpec.makeMeasureSpec(ll.getWidth(), View.MeasureSpec.AT_MOST),
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                descriptionTV.measure(View.MeasureSpec.makeMeasureSpec(descriptionTV.getWidth(), View.MeasureSpec.AT_MOST), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));


                // ViewGroup.LayoutParams parms = (ViewGroup.LayoutParams) ll.getLayoutParams();
                //final int width = ll.getWidth() - parms.l - parms.rightMargin;
//
//                Log.i(TAG, "LL.getMeasuredHeight(): " + ll.getMeasuredHeight());
//                Log.i(TAG, "LL.getHeight(): " + ll.getHeight());
//                Log.i(TAG, "LL distanceToBottomOfScreen(): " + ll.getY());
                int notificationBar = getStatusBarHeight(PopupDialogActivity.this);
                int absoluteTop = (int) (ll.getY() - notificationBar);
                int absoluteBottomY = (int) (tv.getY() + tv.getHeight());

                int distanceToBottomOfScreen = (int) (screenHeight - ll.getY());
                //float percentFromTop = new Float(absoluteTop) / new Float(height);

                Log.i(TAG, "descriptionTV.getMeasuredHeight(): " + descriptionTV.getMeasuredHeight());
                Log.i(TAG, "descriptionTV.getHeight(): " + descriptionTV.getHeight());
                Log.i(TAG, "descriptionTV distanceToBottomOfScreen(): " + descriptionTV.getY());
                Log.i(TAG, "A LL.getMeasuredHeight(): " + ll.getMeasuredHeight());
                Log.i(TAG, "A LL.getHeight(): " + ll.getHeight());
                Log.i(TAG, "A LL getY(): " + ll.getY());
                Log.i(TAG, "distanceToBottomOfScreen: " + distanceToBottomOfScreen);
                //Log.i(TAG, "percent from top" + percentFromTop);
                if (ll.getHeight() < ll.getMeasuredHeight() && !fixed) {
                    Log.i(TAG, "ONLY ONCE IN HERE");
                    fixed = true;
                    /*int yOffset = ll.getMeasuredHeight() - ll.getHeight();
                    Log.i(TAG, "yOffset: " + yOffset);
                    PercentLayoutHelper.PercentLayoutParams layoutParams = (PercentLayoutHelper.PercentLayoutParams) ll.getLayoutParams();
                    PercentLayoutHelper.PercentLayoutInfo percentLayoutInfo = layoutParams.getPercentLayoutInfo();
                    Log.i(TAG, "NavBar Height: " + (float) getNavBarHeight());
                    float yOffsetAsPercent = (((float) yOffset) + (float) getNavBarHeight() + (float) tv.getMeasuredHeight()) / ((float) ((View) ll.getParent()).getHeight());
                    float newTopOffset = percentFromTop - yOffsetAsPercent;//;*/
                    PercentLayoutHelper.PercentLayoutParams layoutParams = (PercentLayoutHelper.PercentLayoutParams) ll.getLayoutParams();
                    PercentLayoutHelper.PercentLayoutInfo percentLayoutInfo = layoutParams.getPercentLayoutInfo();


                    percentLayoutInfo.topMarginPercent = percentLayoutInfo.topMarginPercent - (((float)ll.getMeasuredHeight() - (float)ll.getHeight()) / ((float)((View)ll.getParent()).getHeight())) - .01F;

                    //percentLayoutInfo.topMarginPercent = newTopOffset;


                }

                return true;

            }

        });

        percentLayoutInfo.topMarginPercent = Y / (float)screenHeight;
        Log.i(TAG, "topMarginPercent: "  + percentLayoutInfo.topMarginPercent);
        ll.setBackgroundColor(Color.parseColor(backgroundColor));
        tv = (TextView) findViewById(R.id.button_tv_popin);
        tv.setTextColor(Color.parseColor(RenderConstants.DEFAULT_BUTTON_COLOR));
        tv.setTextSize(RenderConstants.reduceTextSize(RenderConstants.DEFAULT_BUTTON_TEXT_SIZE));
        tv.setText(title);

        RenderConstants.setDefaultPadding(tv);
        descriptionTV.setTextColor(Color.parseColor(RenderConstants.DEFAULT_BUTTON_COLOR));
        descriptionTV.setTextSize(RenderConstants.reduceTextSize(RenderConstants.DEFAULT_BUTTON_TEXT_SIZE));
        RenderConstants.setDefaultPadding(descriptionTV);
        String concat = "";
        for (Gtapi gTap : gPanel.panelArrayList) {
            if (gTap instanceof GText) {
                concat += ((GText) gTap).content + "\n";


            }
        }
        descriptionTV.setText(concat);


    }

    private void fadeIn() {

        Animation fadeInAnim = AnimationUtils.loadAnimation(PopupDialogActivity.this, R.anim.textview_fadein);
        descriptionTV.setAnimation(fadeInAnim);
        descriptionTV.setVisibility(View.VISIBLE);
    }

    public int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public int getNavBarHeight() {
        Resources resources = this.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId);
        }
        return 0;
    }


}

