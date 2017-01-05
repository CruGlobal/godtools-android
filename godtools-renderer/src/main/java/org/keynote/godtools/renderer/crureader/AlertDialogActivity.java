package org.keynote.godtools.renderer.crureader;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.view.Window;

import org.keynote.godtools.renderer.crureader.bo.GPage.Base.GBaseButtonAttributes;
import org.keynote.godtools.renderer.crureader.bo.GPage.RenderHelpers.RenderConstants;
import org.keynote.godtools.renderer.crureader.bo.GPage.RenderHelpers.RenderSingleton;

import static org.keynote.godtools.renderer.crureader.bo.GPage.Base.GBaseButtonAttributes.ButtonMode.allurl;
import static org.keynote.godtools.renderer.crureader.bo.GPage.Base.GBaseButtonAttributes.ButtonMode.url;

/**
 * Created by rmatt on 11/14/2016.
 */
public class AlertDialogActivity extends Activity {

    public static final String TAG = "PopupDialogActivity";
    public static final String CONSTANTS_ALERT_DIALOG_MODE_STRING_EXTRA = "mode";
    public static final String CONSTANTS_ALERT_DIALOG_CONTENT_STRING_EXTRA = "content";

    public GBaseButtonAttributes.ButtonMode mButtonMode;
    public String content;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_CONTENT_TRANSITIONS);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setAllowEnterTransitionOverlap(false);
            getWindow().setAllowReturnTransitionOverlap(false);
        }

        getIntentData();
        bindAlertDialogAndShow();

    }

    private void bindAlertDialogAndShow() {
        AlertDialog.Builder builder = new AlertDialog.Builder(AlertDialogActivity.this)
                .setMessage(mButtonMode == allurl ? RenderSingleton.getInstance().getAppConfig().getAllWebsitesString() : content)
                .setCancelable(true);

        if (mButtonMode == url) {

            builder.setPositiveButton(RenderSingleton.getInstance().getAppConfig().getOpenString(),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            String urlScheme = "http://"; // bUrlMode
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlScheme + content));
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            RenderSingleton.getInstance().getContext().startActivity(intent);

                        }
                    });
        }
        builder.setNeutralButton(RenderSingleton.getInstance().getAppConfig().getEmailString(),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // This code - to send an email with subject and body - is similar
                        // to code in SnuffyActivity: doCmdShare. Can we abstract to a common function?

                        String subjectLine = RenderSingleton.getInstance().getGDocument().packagename.content + (mButtonMode == GBaseButtonAttributes.ButtonMode.allurl ?
                                RenderSingleton.getInstance().getAppConfig().getMultiWebsiteString() :
                                RenderSingleton.getInstance().getAppConfig().getSingleWebsiteAssistString());
                        // stick to plain text - Android cannot reliably send HTML email and anyway
                        // most receivers will turn the link into a hyperlink automatically

                        String msgBody = "http://" + content;
                        RenderConstants.sendEmailWithContent(subjectLine, msgBody);
                    }
                });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            builder.setNegativeButton(RenderSingleton.getInstance().getAppConfig().getCopy(),
                    new DialogInterface.OnClickListener() {
                        @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
                        public void onClick(DialogInterface dialog, int id) {
                            ClipboardManager clipboard = (ClipboardManager) RenderSingleton.getInstance().getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData clip = ClipData.newPlainText(RenderSingleton.getInstance().getAppConfig().getCopy(), "http://" + content);
                            clipboard.setPrimaryClip(clip);
                        }
                    });
        }

        AlertDialog alert = builder.create();
        alert.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                AlertDialogActivity.this.finish();
            }
        });

        alert.show();
    }

    public void getIntentData() {
        mButtonMode = GBaseButtonAttributes.ButtonMode.valueOf(this.getIntent().getExtras().getString(CONSTANTS_ALERT_DIALOG_MODE_STRING_EXTRA));
        content = this.getIntent().getExtras().getString(CONSTANTS_ALERT_DIALOG_CONTENT_STRING_EXTRA);
    }
}

