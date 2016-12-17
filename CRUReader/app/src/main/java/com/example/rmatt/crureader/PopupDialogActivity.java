package com.example.rmatt.crureader;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.percent.PercentLayoutHelper;
import android.support.percent.PercentRelativeLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.rmatt.crureader.bo.GPage.Base.GCoordinator;
import com.example.rmatt.crureader.bo.GPage.Compat.RenderViewCompat;
import com.example.rmatt.crureader.bo.GPage.IDO.IContexual;
import com.example.rmatt.crureader.bo.GPage.RenderHelpers.RenderConstants;
import com.example.rmatt.crureader.bo.GPage.RenderHelpers.RenderSingleton;
import com.example.rmatt.crureader.bo.GPage.Views.AutoScaleButtonView;
import com.example.rmatt.crureader.bo.GPage.Views.AutoScaleTextView;

import java.io.IOException;

/**
 * Created by rmatt on 11/14/2016.
 */
public class PopupDialogActivity extends FragmentActivity implements IContexual {

    public static final String CONSTANTS_PANEL_HASH_KEY_INT_EXTRA = "panelhash";
    public static final String CONSTANTS_PANEL_TITLE_STRING_EXTRA = "title";
    public static final String CONSTANTS_Y_FROM_TOP_FLOAT_EXTRA = "Y";
    public static final String CONSTANTS_IMAGE_WIDTH_INT_EXTRA = "ImageWidth";
    public static final String CONSTANTS_IMAGE_HEIGHT_INT_EXTRA = "ImageHeight";
    public static final String CONSTANTS_IMAGE_LOCATION = "imageLocation";
    public static final String TAG = "PopupDialogActivity";
    public float Y;
    LinearLayout ll;
    int distanceToBottomOfScreen;
    PercentRelativeLayout extraContent;
    AutoScaleTextView tv;
    ImageView iv;
    GCoordinator gPanel;
    int screenHeight;
    String title;
    private String mImageLocation;
    private boolean fixed = false;
    private int mImageWidth;
    private int mImageHeight;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setAllowEnterTransitionOverlap(false);
            getWindow().setAllowReturnTransitionOverlap(false);
        }
        bindLayouts();
        this.getWindow().getDecorView().setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    finishAfterTransition();
                } else {
                    finish();
                }
                return true;
            }
        });
        upwrapExtras();

        setUpImageView();
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
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
        }*/

        if (RenderViewCompat.SDK_JELLY_BEAN) {
            iv.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    Log.i(TAG, "In IV predraw");
                    Log.i(TAG, "IV Measured Height: " + iv.getMeasuredHeight());
                    Log.i(TAG, "IV Height: " + iv.getHeight());
                    //ll.measure(View.MeasureSpec.makeMeasureSpec(ll.getWidth(), View.MeasureSpec.AT_MOST),
                      //      View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

//                    if (ll.getHeight() < ll.getMeasuredHeight() && !fixed) {
//                        fixed = true;
//                        PercentLayoutHelper.PercentLayoutParams layoutParams = (PercentLayoutHelper.PercentLayoutParams) ll.getLayoutParams();
//                        PercentLayoutHelper.PercentLayoutInfo percentLayoutInfo = layoutParams.getPercentLayoutInfo();
//                        percentLayoutInfo.topMarginPercent = percentLayoutInfo.topMarginPercent - (((float) ll.getMeasuredHeight() - (float) ll.getHeight())
//                                / ((float) ((View) ll.getParent()).getHeight()));
//                        return false;
//                    }
//
                    return true;
                }
            });
            ll.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    Log.i(TAG, "In  LL predraw");
                    Log.i(TAG, "LL Measured Height: " + ll.getMeasuredHeight());
                    Log.i(TAG, "LL Height: " + ll.getHeight());
                    ll.measure(View.MeasureSpec.makeMeasureSpec(ll.getWidth(), View.MeasureSpec.AT_MOST),
                            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

                    if (ll.getHeight() < ll.getMeasuredHeight() && !fixed) {
                        fixed = true;
                        PercentLayoutHelper.PercentLayoutParams layoutParams = (PercentLayoutHelper.PercentLayoutParams) ll.getLayoutParams();
                        PercentLayoutHelper.PercentLayoutInfo percentLayoutInfo = layoutParams.getPercentLayoutInfo();
                        percentLayoutInfo.topMarginPercent = percentLayoutInfo.topMarginPercent - ((
                                (float) ll.getMeasuredHeight() - (float) ll.getHeight())
                                / ((float) ((View) ll.getParent()).getHeight()));

//                        if(hasImageExtraFromBigButton())
//                        {
//                            percentLayoutInfo.topMarginPercent = percentLayoutInfo.topMarginPercent - (((float)iv.getMeasuredHeight() - (float)iv.getHeight())
//                                    / ((float) ((View) ll.getParent().getParent()).getHeight()));
//                        }
                        return true;
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

        PercentRelativeLayout.LayoutParams layoutParams = (PercentRelativeLayout.LayoutParams) ll.getLayoutParams();
        layoutParams.addRule(PercentRelativeLayout.CENTER_IN_PARENT);
    }

    private void upwrapExtras() {

        Y = this.getIntent().getExtras().getFloat(CONSTANTS_Y_FROM_TOP_FLOAT_EXTRA);
        int panelKey = this.getIntent().getExtras().getInt(CONSTANTS_PANEL_HASH_KEY_INT_EXTRA);
        title = this.getIntent().getExtras().getString(CONSTANTS_PANEL_TITLE_STRING_EXTRA);
        mImageLocation = this.getIntent().getExtras().getString(CONSTANTS_IMAGE_LOCATION);
        gPanel = RenderSingleton.getInstance().gPanelHashMap.get(panelKey);
        mImageWidth = this.getIntent().getExtras().getInt(CONSTANTS_IMAGE_WIDTH_INT_EXTRA);
        mImageHeight = this.getIntent().getExtras().getInt(CONSTANTS_IMAGE_HEIGHT_INT_EXTRA);

    }

    private void bindHeader() {

        tv.setTextColor(Color.parseColor(RenderConstants.DEFAULT_TEXT_COLOR));
        tv.setText(title);
    }

    private void bindLayouts() {
        setContentView(R.layout.activity_popupdialog);
        extraContent = (PercentRelativeLayout) findViewById(R.id.extra_wrapper_fl);
        tv = (AutoScaleTextView) findViewById(R.id.popin_button_tv);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 100.0f);
        ll = (LinearLayout) findViewById(R.id.popup_innerLinearLayout);
        iv = (ImageView) findViewById(R.id.popup_imageView);
        ll.setBackgroundColor(Color.parseColor(RenderSingleton.getInstance().getCurrentPageGlobalColor()));

    }

    private void setUpImageView() {
        if (hasImageExtraFromBigButton()) {
            try {
                Drawable d = Drawable.createFromStream(RenderSingleton.getInstance().getContext().getAssets().open(mImageLocation), null);
                iv.setImageDrawable(d);
                Log.i(TAG, "IV: painting");
                //ll.childDrawableStateChanged(iv);
                iv.setMinimumWidth(mImageWidth);
                iv.setMaxWidth(mImageWidth);
                iv.getLayoutParams().height = mImageHeight;
                iv.setMaxHeight(mImageHeight);
                iv.setMinimumHeight(mImageHeight);
                //Try this sync becuase measurement is off.
//                ImageAsyncTask.setImageView(mImageLocation, iv);
                //This means it's big, so center labels.
                tv.setGravity(Gravity.CENTER_HORIZONTAL);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //iv.setVisibility(View.VISIBLE);

        }
    }

    private boolean hasImageExtraFromBigButton() {
        return mImageLocation != null && !mImageLocation.equalsIgnoreCase("");
    }

    private void bindPanelContent() {
        gPanel.render(getLayoutInflater(), extraContent, RenderSingleton.getInstance().curPosition);
        if (hasImageExtraFromBigButton()) {
            centerAllChildren(extraContent);
        }
    }

    private void centerAllChildren(View v) {
        if (v instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) v).getChildCount(); i++) {
                centerAllChildren(((ViewGroup) v).getChildAt(i));
            }
        } else if (v instanceof AutoScaleTextView) {
            LinearLayout.LayoutParams llParams = (LinearLayout.LayoutParams) v.getLayoutParams();
            llParams.gravity = Gravity.CENTER_HORIZONTAL;

        } else if (v instanceof AutoScaleButtonView) {

            LinearLayout.LayoutParams llParams = (LinearLayout.LayoutParams) v.getLayoutParams();
            llParams.gravity = Gravity.CENTER_HORIZONTAL;

        }
    }

    private void TranslateView() {
        DisplayMetrics metrics = PopupDialogActivity.this.getResources().getDisplayMetrics();
        screenHeight = metrics.heightPixels;
        PercentLayoutHelper.PercentLayoutParams layoutParams = (PercentLayoutHelper.PercentLayoutParams) ll.getLayoutParams();
        PercentLayoutHelper.PercentLayoutInfo percentLayoutInfo = layoutParams.getPercentLayoutInfo();
        percentLayoutInfo.topMarginPercent = Y / (float) screenHeight;
    }

   /* private void fadeIn() {
        extraContent.setVisibility(View.VISIBLE);
    }*/

    @Override
    public FragmentManager getContexualFragmentActivity() {

        return this.getSupportFragmentManager();
    }
}

