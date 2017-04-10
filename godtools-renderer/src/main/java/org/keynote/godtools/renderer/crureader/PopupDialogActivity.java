package org.keynote.godtools.renderer.crureader;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.keynote.godtools.renderer.crureader.bo.GPage.Base.GCoordinator;
import org.keynote.godtools.renderer.crureader.bo.GPage.Event.OnDismissEvent;
import org.keynote.godtools.renderer.crureader.bo.GPage.IDO.IContexual;
import org.keynote.godtools.renderer.crureader.bo.GPage.RenderHelpers.ImageAsyncTask;
import org.keynote.godtools.renderer.crureader.bo.GPage.RenderHelpers.RenderConstants;
import org.keynote.godtools.renderer.crureader.bo.GPage.RenderHelpers.RenderSingleton;
import org.keynote.godtools.renderer.crureader.bo.GPage.Views.AutoScaleButtonView;
import org.keynote.godtools.renderer.crureader.bo.GPage.Views.AutoScaleTextView;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.view.View.MeasureSpec.makeMeasureSpec;

public class PopupDialogActivity extends FragmentActivity implements IContexual {
    private static final String TAG = "PopupDialogActivity";
    public static final String CONSTANTS_PANEL_HASH_KEY_INT_EXTRA = "panelhash";
    public static final String CONSTANTS_PANEL_TITLE_STRING_EXTRA = "title";
    public static final String CONSTANTS_Y_FROM_TOP_FLOAT_EXTRA = "Y";
    public static final String CONSTANTS_IMAGE_WIDTH_INT_EXTRA = "ImageWidth";
    public static final String CONSTANTS_IMAGE_HEIGHT_INT_EXTRA = "ImageHeight";
    public static final String CONSTANTS_POSITION_INT_EXTRA = "position";
    public static final String CONSTANTS_IMAGE_LOCATION = "imageLocation";

    GCoordinator gPanel;

    @BindView(R2.id.extra_wrapper_fl)
    PercentRelativeLayout extraContent;
    @Nullable
    @BindView(R2.id.popin_button_tv)
    AutoScaleTextView tv;
    @BindView(R2.id.popup_imageView)
    ImageView iv;
    @BindView(R2.id.popup_innerLinearLayout)
    LinearLayout ll;

    private float Y = 0;
    private String title;
    private String mImageLocation;
    private int mImageWidth = 0;
    private int mImageHeight = 0;
    private int mPosition = 0;

    /* BEGIN lifecycle */

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            requestWindowFeature(Window.FEATURE_CONTENT_TRANSITIONS);
            getWindow().setAllowEnterTransitionOverlap(false);
            getWindow().setAllowReturnTransitionOverlap(false);
        }
        readExtras();
        setContentView(R.layout.activity_popupdialog);
        setUpDismissAction();
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        ButterKnife.bind(this);
        loadPanelContent();
        setupHeader();
        setUpImageView();
        setupPanel();
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Subscribe
    public void onDismissEvent(@NonNull final OnDismissEvent event) {
        Log.i(TAG, "On Dismiss event");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (!this.isChangingConfigurations()) {
                finish();
            }
        } else {
            if (!this.isFinishing()) {
                finish();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    /* END lifecycle */

    private void setUpDismissAction() {
        getWindow().getDecorView().setOnTouchListener(new View.OnTouchListener() {
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
    }

    private void readExtras() {
        final Intent intent = getIntent();

        gPanel = RenderSingleton.getInstance().gPanelHashMap
                .get(intent.getIntExtra(CONSTANTS_PANEL_HASH_KEY_INT_EXTRA, 0));
        Y = intent.getFloatExtra(CONSTANTS_Y_FROM_TOP_FLOAT_EXTRA, Y);
        title = intent.getStringExtra(CONSTANTS_PANEL_TITLE_STRING_EXTRA);

        mImageLocation = intent.getStringExtra(CONSTANTS_IMAGE_LOCATION);
        mImageWidth = intent.getIntExtra(CONSTANTS_IMAGE_WIDTH_INT_EXTRA, mImageWidth);
        mImageHeight = intent.getIntExtra(CONSTANTS_IMAGE_HEIGHT_INT_EXTRA, mImageHeight);
        mPosition = intent.getIntExtra(CONSTANTS_POSITION_INT_EXTRA, mPosition);
    }

    private void setupHeader() {
        if (tv != null) {
            tv.setText(title);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 100.0f);
            tv.setTextColor(Color.parseColor(RenderConstants.DEFAULT_TEXT_COLOR));
        }
    }

    private void setupPanel() {
        if (ll != null) {
            ll.setBackgroundColor(RenderSingleton.getInstance().getPositionGlobalColorAsInt(mPosition));

            // update the content stack layout position
            final ViewGroup.LayoutParams lp = ll.getLayoutParams();
            if (lp instanceof RelativeLayout.LayoutParams) {
                // measure the content stack
                final DisplayMetrics metrics = getResources().getDisplayMetrics();
                ll.measure(makeMeasureSpec(metrics.widthPixels, View.MeasureSpec.EXACTLY),
                           makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

                // update the top margin
                ((RelativeLayout.LayoutParams) lp).topMargin =
                        Math.min(Math.max(metrics.heightPixels - ll.getMeasuredHeight(), 0), (int) Y);
                ll.setLayoutParams(lp);
            }
        }
    }

    private void setUpImageView() {
        if (hasImageExtraFromBigButton()) {
            iv.getLayoutParams().height = mImageHeight;
            iv.getLayoutParams().width = mImageWidth;
            ImageAsyncTask.setImageView(mImageLocation, iv);
            if (tv != null) {
                tv.setGravity(Gravity.CENTER_HORIZONTAL);
            }
        }
    }

    private boolean hasImageExtraFromBigButton() {
        return mImageLocation != null && !mImageLocation.equalsIgnoreCase("");
    }

    private void loadPanelContent() {
        gPanel.render(getLayoutInflater(), extraContent, mPosition);
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

    @Override
    public FragmentManager getContexualFragmentActivity() {
        return this.getSupportFragmentManager();
    }
}
