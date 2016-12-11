package com.example.rmatt.crureader;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.percent.PercentFrameLayout;
import android.support.percent.PercentLayoutHelper;
import android.support.percent.PercentRelativeLayout;
import android.support.v4.app.FragmentActivity;
import android.transition.Transition;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.LinearLayout;

import com.example.rmatt.crureader.bo.GCoordinator;
import com.example.rmatt.crureader.bo.GPage.Compat.RenderViewCompat;
import com.example.rmatt.crureader.bo.GPage.RenderHelpers.RenderConstants;
import com.example.rmatt.crureader.bo.GPage.RenderHelpers.RenderSingleton;
import com.example.rmatt.crureader.bo.GPage.Views.AutoScaleTextView;

/**
 * Created by rmatt on 11/14/2016.
 */
public class PopupDialogActivity extends FragmentActivity {


    public static final String CONSTANTS_PANEL_HASH_KEY_INT_EXTRA = "panelhash";
    public static final String CONSTANTS_PANEL_TITLE_STRING_EXTRA = "title";
    public static final String CONSTANTS_Y_FROM_TOP_FLOAT_EXTRA = "Y";
    public static final String TAG = "PopupDialogActivity";
    public float Y;
    LinearLayout ll;

    int distanceToBottomOfScreen;
    PercentRelativeLayout extraContent;
    AutoScaleTextView tv;
    GCoordinator gPanel;
    int screenHeight;
    String title;
    private boolean fixed = false;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //extraContent.setAnimation(null);
        //extraContent.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setAllowEnterTransitionOverlap(false);
            getWindow().setAllowReturnTransitionOverlap(true);
        }


        setContentView(R.layout.activity_popupdialog);

        bindLayouts();


        upwrapExtras();


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

        if (RenderViewCompat.SDK_JELLY_BEAN) {
            ll.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {

                    ll.measure(View.MeasureSpec.makeMeasureSpec(ll.getWidth(), View.MeasureSpec.AT_MOST),
                            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                   // extraContent.measure(View.MeasureSpec.makeMeasureSpec(extraContent.getWidth(), View.MeasureSpec.AT_MOST), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));


                    if (ll.getHeight() < ll.getMeasuredHeight() && !fixed) {
                        fixed = true;
                        PercentLayoutHelper.PercentLayoutParams layoutParams = (PercentLayoutHelper.PercentLayoutParams) ll.getLayoutParams();
                        PercentLayoutHelper.PercentLayoutInfo percentLayoutInfo = layoutParams.getPercentLayoutInfo();
                        percentLayoutInfo.topMarginPercent = percentLayoutInfo.topMarginPercent - (((float) ll.getMeasuredHeight() - (float) ll.getHeight())
                                / ((float) ((View) ll.getParent()).getHeight()));
                    }

                    return true;

                }

            });

        }
        bindHeader();
        bindPanelContent();

        if (RenderViewCompat.SDK_JELLY_BEAN) {
            TranslateView();
        } else {
            TranslateViewJellyBeanCompat();
        }


    }

    private void TranslateViewJellyBeanCompat() {

        PercentFrameLayout.LayoutParams layoutParams = (PercentFrameLayout.LayoutParams) ll.getLayoutParams();
        layoutParams.gravity = Gravity.CENTER;
    }

    private void upwrapExtras() {

        Y = this.getIntent().getExtras().getFloat(CONSTANTS_Y_FROM_TOP_FLOAT_EXTRA);
        int panelKey = this.getIntent().getExtras().getInt(CONSTANTS_PANEL_HASH_KEY_INT_EXTRA);
        title = this.getIntent().getExtras().getString(CONSTANTS_PANEL_TITLE_STRING_EXTRA);
        gPanel = RenderSingleton.getInstance().gPanelHashMap.get(panelKey);
    }

    private void bindHeader() {


        tv.setTextColor(Color.parseColor(RenderConstants.DEFAULT_TEXT_COLOR));
        tv.setText(title);
    }

    private void bindLayouts() {
        extraContent = (PercentRelativeLayout) findViewById(R.id.extra_wrapper_fl);
        tv = (AutoScaleTextView) findViewById(R.id.popin_button_tv);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 100.0f);
        ll = (LinearLayout) findViewById(R.id.popup_innerLinearLayout);
        ll.setBackgroundColor(Color.parseColor(RenderSingleton.getInstance().getCurrentPageGlobalColor()));
        findViewById(R.id.popup_percentFrameLayout).setClickable(false);
        findViewById(R.id.popup_percentFrameLayout).setAnimation(null);
        findViewById(R.id.popup_percentFrameLayout).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    finishAfterTransition();
                }
                else
                {
                    finish();
                }
                return false;
            }
        });

    }

    private void bindPanelContent() {
        gPanel.render(getLayoutInflater(), extraContent, RenderSingleton.getInstance().curPosition);
    }

    private void TranslateView() {
        DisplayMetrics metrics = PopupDialogActivity.this.getResources().getDisplayMetrics();
        screenHeight = metrics.heightPixels;
        PercentLayoutHelper.PercentLayoutParams layoutParams = (PercentLayoutHelper.PercentLayoutParams) ll.getLayoutParams();
        PercentLayoutHelper.PercentLayoutInfo percentLayoutInfo = layoutParams.getPercentLayoutInfo();
        percentLayoutInfo.topMarginPercent = Y / (float) screenHeight;
    }

    private void fadeIn() {
        extraContent.setVisibility(View.VISIBLE);
    }


}

