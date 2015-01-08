package org.keynote.godtools.android;


import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.keynote.godtools.android.business.GTLanguage;
import org.keynote.godtools.android.business.GTPackage;
import org.keynote.godtools.android.business.GTPackageReader;
import org.keynote.godtools.android.everystudent.EveryStudent;
import org.keynote.godtools.android.fragments.LanguageDialogFragment;
import org.keynote.godtools.android.fragments.PackageListFragment;
import org.keynote.godtools.android.http.DownloadTask;
import org.keynote.godtools.android.http.DraftCreationTask;
import org.keynote.godtools.android.http.DraftPublishTask;
import org.keynote.godtools.android.http.GodToolsApiClient;
import org.keynote.godtools.android.http.MetaTask;
import org.keynote.godtools.android.http.NotificationRegistrationTask;
import org.keynote.godtools.android.http.NotificationUpdateTask;
import org.keynote.godtools.android.notifications.NotificationInfo;
import org.keynote.godtools.android.snuffy.SnuffyApplication;
import org.keynote.godtools.android.utils.Device;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


public class MainPW extends BaseActionBarActivity implements LanguageDialogFragment.OnLanguageChangedListener,
        PackageListFragment.OnPackageSelectedListener,
        DownloadTask.DownloadTaskHandler,
        MetaTask.MetaTaskHandler
{
    private static final String TAG = "MainPW";
    private static final String TAG_LIST = "PackageList";
    private static final int REQUEST_SETTINGS = 1001;
    private static final String PREFS_NAME = "GodTools";
    private static final String JUST_SWITCHED = "justSwitched";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";

    String SENDER_ID = "237513440670";

    public static final int REFERENCE_DEVICE_HEIGHT = 960;    // pixels on iPhone w/retina - including title bar
    public static final int REFERENCE_DEVICE_WIDTH = 640;    // pixels on iPhone w/retina - full width

    private int mPageLeft;
    private int mPageTop;
    private int mPageWidth;
    private int mPageHeight;
    private boolean mSetupNeeded;
    private String languagePrimary;
    private List<GTPackage> packageList;
    PackageListFragment packageFrag;
    FragmentManager fm;
    View vLoading;
    TextView tvTask;
    FrameLayout frameLayout;
    RelativeLayout tableLayout;
    ImageButton refreshButton;
    ImageButton shareButton;
    GoogleCloudMessaging gcm;
    Context context;
    String regid = "";
    Timer timer;
    /**
     * When clicked, dialog to launch a new translation is opened
     */
    ImageButton addButton;
    boolean isDownloading;
    boolean noPackages = false;
    boolean justSwitchedToTranslatorMode;
    SharedPreferences settings;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_pw);

        vLoading = findViewById(R.id.contLoading);
        tvTask = (TextView) findViewById(R.id.tvTask);
        frameLayout = (FrameLayout) findViewById(R.id.contList);
        tableLayout = (RelativeLayout) findViewById(R.id.full_table);

        settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        languagePrimary = settings.getString(GTLanguage.KEY_PRIMARY, "en");
        justSwitchedToTranslatorMode = settings.getBoolean(JUST_SWITCHED, false);

        packageList = getPackageList(); // get the packages for the primary language

        fm = getSupportFragmentManager();
        packageFrag = (PackageListFragment) fm.findFragmentByTag(TAG_LIST);
        if (packageFrag == null)
        {
            packageFrag = PackageListFragment.newInstance(languagePrimary, packageList, isTranslatorModeEnabled());
            FragmentTransaction ft = fm.beginTransaction();
            ft.add(R.id.contList, packageFrag, TAG_LIST);
            ft.commit();
        }

        // Make the Settings button highlight when pressed (without defining a separate image)
        ImageButton button = (ImageButton) findViewById(R.id.homescreen_settings_button);
        button.setOnTouchListener(new OnTouchListener()
        {
            @Override
            public boolean onTouch(View arg0, MotionEvent me)
            {
                ImageButton button = (ImageButton) arg0;
                Drawable d = button.getBackground();
                PorterDuffColorFilter grayFilter =
                        new PorterDuffColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_ATOP);

                if (me.getAction() == MotionEvent.ACTION_DOWN)
                {
                    d.setColorFilter(grayFilter);
                    button.invalidate();
                    return false;
                }
                else if (me.getAction() == MotionEvent.ACTION_UP)
                {
                    d.setColorFilter(null);
                    button.invalidate();
                    return false;
                }
                else
                    return false;
            }
        });


        shareButton = (ImageButton) findViewById(R.id.export_button);
        shareButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                doCmdShare(view);
            }
        });

        addButton = (ImageButton) findViewById(R.id.homescreen_add_button);
        refreshButton = (ImageButton) findViewById(R.id.refresh_button);
        refreshButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                onCmd_refresh(null);
            }
        });

        addButton = (ImageButton) findViewById(R.id.homescreen_add_button);

        if (settings.getBoolean("TranslatorMode", false))
        {
            addButton.setVisibility(View.VISIBLE);
            addButton.setEnabled(true);

            refreshButton.setVisibility(View.VISIBLE);
            refreshButton.setEnabled(true);
            refreshButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    onCmd_refresh(null);
                }
            });

            shareButton.setVisibility(View.INVISIBLE);
            shareButton.setEnabled(false);
        }
        else
        {
            addButton.setVisibility(View.INVISIBLE);
            addButton.setEnabled(false);

            refreshButton.setVisibility(View.INVISIBLE);
            refreshButton.setEnabled(false);

            shareButton.setVisibility(View.VISIBLE);
            shareButton.setEnabled(true);
        }

        mSetupNeeded = true;
        context = getApplicationContext();

        Log.i(TAG, regid);
        // Check device for Play Services APK. If check succeeds, proceed with GCM registration.
        if (checkPlayServices())
        {
            Log.i(TAG, "Registering Device");
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId(context);

            if (regid.isEmpty())
            {
                registerInBackground();
            }

            // send notification update each time app is used for notification type 1
            GodToolsApiClient.updateNotification(settings.getString("Authorization_Generic", ""), 
                    regid, NotificationInfo.NOT_USED_2_WEEKS, new NotificationUpdateTask.NotificationUpdateTaskHandler()
            {
                @Override
                public void registrationComplete(String regId)
                {
                    Log.i(NotificationInfo.NOTIFICATION_TAG, "Used Notification notice sent to API");
                }

                @Override
                public void registrationFailed()
                {
                    Log.e(NotificationInfo.NOTIFICATION_TAG, "Used notification notice failed to send to API");
                }
            });
        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
        }
        Log.i(TAG, regid);
        
        if (!justSwitchedToTranslatorMode) startTimer(); // don't start timer when switching to translator mode
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        final SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        switch (resultCode)
        {
            case RESULT_CHANGED_PRIMARY:
            {
                SnuffyApplication app = (SnuffyApplication) getApplication();
                app.setAppLocale(data.getStringExtra("primaryCode"));

                languagePrimary = data.getStringExtra("primaryCode");
                packageList = getPackageList();
                packageFrag.refreshList(languagePrimary, isTranslatorModeEnabled(), packageList);
                createTheHomeScreen();

                break;
            }
            case RESULT_DOWNLOAD_PRIMARY:
            {
                // start the download
                String code = data.getStringExtra("primaryCode");
                showLoading("Downloading resources...");
                GodToolsApiClient.downloadLanguagePack((SnuffyApplication) getApplication(),
                        code,
                        "primary",
                        settings.getString("Authorization_Generic", ""),
                        this);
                break;
            }
            case RESULT_DOWNLOAD_PARALLEL:
            {
                // refresh the list if the primary language was changed
                String primaryCode = settings.getString(GTLanguage.KEY_PRIMARY, "en");
                if (!languagePrimary.equalsIgnoreCase(primaryCode))
                {
                    languagePrimary = primaryCode;
                    packageList = getPackageList();
                    packageFrag.refreshList(languagePrimary, isTranslatorModeEnabled(), packageList);
                }

                String code = data.getStringExtra("parallelCode");
                showLoading("Downloading resources...");
                GodToolsApiClient.downloadLanguagePack((SnuffyApplication) getApplication(),
                        code,
                        "parallel",
                        settings.getString("Authorization_Generic", ""),
                        this);
                break;
            }
            case RESULT_PREVIEW_MODE_ENABLED:
            {
                // refresh the list
                String primaryCode = settings.getString(GTLanguage.KEY_PRIMARY, "en");

                if (!languagePrimary.equalsIgnoreCase(primaryCode))
                {
                    SnuffyApplication app = (SnuffyApplication) getApplication();
                    app.setAppLocale(primaryCode);
                }

                showLoading("Downloading drafts...");

                GodToolsApiClient.getListOfDrafts(settings.getString("Authorization_Draft", ""), languagePrimary, "draft_primary", this);

                Toast.makeText(MainPW.this, "Translator preview mode is enabled", Toast.LENGTH_LONG).show();
                switchedToTranslatorMode(true);

                finish();
                startActivity(getIntent());

                break;
            }
            case RESULT_PREVIEW_MODE_DISABLED:
            {
                // refresh the list
                String primaryCode = settings.getString(GTLanguage.KEY_PRIMARY, "en");

                if (!languagePrimary.equalsIgnoreCase(primaryCode))
                {
                    SnuffyApplication app = (SnuffyApplication) getApplication();
                    app.setAppLocale(primaryCode);
                }

                Toast.makeText(MainPW.this, "Translator preview mode is disabled", Toast.LENGTH_LONG).show();

                finish();
                startActivity(getIntent());

                break;
            }
        }
    }


    @Override
    public void onStart()
    {
        super.onStart();
        Log.d(TAG, "onStart");
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor ed = settings.edit();
        ed.commit();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        // Setup is only done on first resume,
        // else we resize the screen elements smaller and smaller each time.
        if (mSetupNeeded)
        {
            doSetup(1000);    // 1 second delay required to make sure activity fully created
            // - is there something we can test for that is better than a fixed timeout?
        }
    }

    private void doSetup(int delay)
    {
        new Handler().postDelayed(new Runnable()
        {
            public void run()
            {
                createTheHomeScreen();
                getScreenSize();
                showTheHomeScreen();
                mSetupNeeded = false;
            }
        }, delay);  // delay can be required to make sure activity fully created - is there something we can test for that is better than a fixed timeout?

    }

    private void getScreenSize()
    {
		/*
         * Although these measurements are not used on this screen, they are passed to and used by
		 * the following screens. At some point maybe all layouts can be updated to relative layout.
		 */
        Rect rect = new Rect();
        Window window = getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rect);

        rect.top = 0;
        int width = rect.width();
        int height = rect.height();
        int left = rect.left;
        int top = rect.top;

        double aspectRatioTarget = (double) MainPW.REFERENCE_DEVICE_WIDTH / (double) MainPW.REFERENCE_DEVICE_HEIGHT;
        double aspectRatio = (double) rect.width() / (double) rect.height();

        if (aspectRatio > aspectRatioTarget)
        {
            height = rect.height();
            width = (int) Math.round(height * aspectRatioTarget);
        }
        else
        {
            width = rect.width();
            height = (int) Math.round(width / aspectRatioTarget);
        }

        left = rect.left + (rect.width() - width) / 2;
        top = 0 + (rect.height() - height) / 2;

        mPageLeft = left;
        mPageTop = top;
        mPageWidth = width;
        mPageHeight = height;
    }

    private void createTheHomeScreen()
    {
        /*
         * This method is called each time the UI needs to be refreshed.
         */

        // If no packages are available for a language, the add method is automatically called
        if (packageList.size() < 1)
        {
            GTPackage newPackage = new GTPackage();
            newPackage.setName("No Package");
            packageList.add(newPackage);
            noPackages = true;

            onCmd_add(null);
        }
        else if (justSwitchedToTranslatorMode)
        {
            /*
             * When switching to translator mode, the MainPW activity is restarted. However, the packageList and
             * packageFrag need to be refreshed based on the newly downloaded items. The justSwitchedToTranslatorMode is
             * saved in the settings and when true, this will refresh the packages available.
             */
            packageList = getPackageList();
            packageFrag.refreshList(languagePrimary, isTranslatorModeEnabled(), packageList);
        }

        noPackages = false;

        // resize contList
        try
        {
            int childHeight = packageFrag.getListView().getChildAt(0).getHeight();
            int childWidth = packageFrag.getListView().getChildAt(0).getWidth();
            int totalHeight = childHeight * packageList.size();
            packageFrag.getListView().setLayoutParams(new FrameLayout.LayoutParams(childWidth, totalHeight));

            // Once the resizing is successful the boolean is changed to false the the list is not refreshed continually

            justSwitchedToTranslatorMode = false;
            switchedToTranslatorMode(false);
        } catch (Exception e)
        {
            Log.e("error", e.getMessage(), e);
        }
    }

    private void showTheHomeScreen()
    {
        // Now that it is resized - show it
        ViewGroup container = (ViewGroup) findViewById(R.id.homescreen_container);
        container.setVisibility(View.VISIBLE);
        trackScreenVisit();
    }

    private void showLoading(String msg)
    {
        isDownloading = true;
        supportInvalidateOptionsMenu();
        tvTask.setText(msg);
        vLoading.setVisibility(View.VISIBLE);
        packageFrag.disable();

        if (refreshButton != null)
        {
            refreshButton.setEnabled(false);
        }
    }

    @Override
    public void onLanguageChanged(String name, String code)
    {
        final SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        GTLanguage gtLanguage = GTLanguage.getLanguage(MainPW.this, code);
        if (gtLanguage.isDownloaded())
        {
            languagePrimary = gtLanguage.getLanguageCode();
            packageList = getPackageList();
            packageFrag.refreshList(languagePrimary, isTranslatorModeEnabled(), packageList);

            SharedPreferences.Editor editor = settings.edit();
            editor.putString(GTLanguage.KEY_PRIMARY, code);

            String parallelLanguage = settings.getString(GTLanguage.KEY_PARALLEL, "");
            if (code.equalsIgnoreCase(parallelLanguage))
                editor.putString(GTLanguage.KEY_PARALLEL, "");

            editor.commit();

            SnuffyApplication app = (SnuffyApplication) getApplication();
            app.setAppLocale(code);

        }
        else
        {

            if (Device.isConnected(MainPW.this))
            {
                showLoading("Downloading resources...");
                GodToolsApiClient.downloadLanguagePack((SnuffyApplication) getApplication(),
                        code,
                        "primary",
                        settings.getString("Authorization_Generic", ""),
                        this);
            }
            else
            {
                // TODO: show dialog, Internet connection is required to download the resources
                Toast.makeText(this, "Unable to download resources. Internet connection unavailable.", Toast.LENGTH_LONG).show();
            }

        }
        createTheHomeScreen();
    }

    @Override
    public void downloadTaskComplete(String url, String filePath, String langCode, String tag)
    {

        if (tag.equalsIgnoreCase("primary"))
        {
            languagePrimary = langCode;

            SnuffyApplication app = (SnuffyApplication) getApplication();
            app.setAppLocale(langCode);

            SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString(GTLanguage.KEY_PRIMARY, langCode);
            editor.commit();

            GTLanguage gtl = GTLanguage.getLanguage(MainPW.this, langCode);
            gtl.setDownloaded(true);
            gtl.update(MainPW.this);

            if (isTranslatorModeEnabled())
            {
                // check for draft_primary
                GodToolsApiClient.getListOfDrafts(settings.getString("Authorization_Draft", ""), langCode, "draft_primary", this);

            }
            else
            {
                packageList = getPackageList();
                packageFrag.refreshList(langCode, isTranslatorModeEnabled(), packageList);
                hideLoading();
            }
            createTheHomeScreen();
        }
        else if (tag.equalsIgnoreCase("parallel"))
        {

            SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString(GTLanguage.KEY_PARALLEL, langCode);
            editor.commit();

            GTLanguage gtl = GTLanguage.getLanguage(MainPW.this, langCode);
            gtl.setDownloaded(true);
            gtl.update(MainPW.this);

            if (isTranslatorModeEnabled())
            {
                // check for draft_parallel
                GodToolsApiClient.getListOfDrafts(settings.getString("Authorization_Draft", ""), langCode, "draft_parallel", this);
            }
            else
            {
                hideLoading();
            }
            createTheHomeScreen();
        }
        else if (tag.equalsIgnoreCase("draft"))
        {
            Toast.makeText(MainPW.this, "Drafts have been updated", Toast.LENGTH_SHORT).show();
            packageList = getPackageList();
            packageFrag.refreshList(langCode, isTranslatorModeEnabled(), packageList);
            hideLoading();
            createTheHomeScreen();
        }
        else if (tag.equalsIgnoreCase("draft_primary"))
        {
            languagePrimary = langCode;
            packageList = getPackageList();

            packageFrag.refreshList(langCode, isTranslatorModeEnabled(), packageList);

            hideLoading();

        }
        else if (tag.equalsIgnoreCase("draft_parallel"))
        {
            hideLoading();
            createTheHomeScreen();
        }
    }

    private List<GTPackage> getPackageList()
    {
        if (isTranslatorModeEnabled())
        {
            return GTPackage.getPackageByLanguage(MainPW.this, languagePrimary);
        }
        else
        {
            return GTPackage.getLivePackages(MainPW.this, languagePrimary);
        }
    }

    private boolean isTranslatorModeEnabled()
    {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return settings.getBoolean("TranslatorMode", false);
    }

    private void switchedToTranslatorMode(boolean switched)
    {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(JUST_SWITCHED, switched);
        editor.commit();
    }

    @Override
    public void onPackageSelected(final GTPackage gtPackage)
    {
        if (gtPackage.getCode().equalsIgnoreCase("everystudent"))
        {
            Intent intent = new Intent(this, EveryStudent.class);
            intent.putExtra("PackageName", gtPackage.getCode());
            addPageFrameToIntent(intent);
            startActivity(intent);
            return;
        }

        final SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        if (isTranslatorModeEnabled() && "draft".equalsIgnoreCase(gtPackage.getStatus()))
        {
            presentFinalizeDraftOption(gtPackage, settings);
        }
        else
        {
            Intent intent = new Intent(this, SnuffyPWActivity.class);
            intent.putExtra("PackageName", gtPackage.getCode());
            intent.putExtra("LanguageCode", gtPackage.getLanguage());
            intent.putExtra("ConfigFileName", gtPackage.getConfigFileName());
            intent.putExtra("Status", gtPackage.getStatus());
            addPageFrameToIntent(intent);
            startActivity(intent);
        }
    }

    /**
     * Dialog example taken from:
     * http://stackoverflow.com/questions/2478517/how-to-display-a-yes-no-dialog-box-in-android
     */
    private void presentFinalizeDraftOption(final GTPackage gtPackage, final SharedPreferences settings)
    {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int which)
            {
                switch (which)
                {
                    case DialogInterface.BUTTON_POSITIVE:
                        GodToolsApiClient.publishDraft(settings.getString("Authorization_Draft", ""),
                                gtPackage.getLanguage(),
                                gtPackage.getCode(),
                                new DraftPublishTask.DraftTaskHandler()
                                {
                                    @Override
                                    public void draftTaskComplete()
                                    {
                                        Toast.makeText(getApplicationContext(), "Draft has been published", Toast.LENGTH_SHORT).show();
                                        showLoading("Updating drafts");
                                        GodToolsApiClient.getListOfDrafts(settings.getString("Authorization_Draft", ""), languagePrimary, "draft_primary", MainPW.this);
                                    }

                                    @Override
                                    public void draftTaskFailure()
                                    {
                                        Toast.makeText(getApplicationContext(), "Failed to publish draft", Toast.LENGTH_SHORT).show();
                                    }
                                });

                        startActivity(getIntent());
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        Intent intent = new Intent(MainPW.this, SnuffyPWActivity.class);
                        intent.putExtra("PackageName", gtPackage.getCode());
                        intent.putExtra("LanguageCode", gtPackage.getLanguage());
                        intent.putExtra("ConfigFileName", gtPackage.getConfigFileName());
                        intent.putExtra("Status", gtPackage.getStatus());
                        addPageFrameToIntent(intent);
                        startActivity(intent);
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do you want to publish this draft?")
                .setPositiveButton("Yes, it's ready!", dialogClickListener)
                .setNegativeButton("No, I just need to see it.", dialogClickListener)
                .show();
    }

    @Override
    public void metaTaskComplete(InputStream is, String langCode, String tag)
    {
        // process the input stream
        new UpdateDraftListTask().execute(is, langCode, tag);
    }

    @Override
    public void metaTaskFailure(InputStream is, String langCode, String tag)
    {

        if (tag.equalsIgnoreCase("draft") || tag.equalsIgnoreCase("draft_primary"))
        {
            packageList = getPackageList();
            packageFrag.refreshList(langCode, isTranslatorModeEnabled(), packageList);
        }

        hideLoading();
        Toast.makeText(MainPW.this, "Failed to update drafts", Toast.LENGTH_SHORT).show();


    }

    @Override
    public void downloadTaskFailure(String url, String filePath, String langCode, String tag)
    {

        if (tag.equalsIgnoreCase("draft"))
        {

            Toast.makeText(MainPW.this, "Failed to update drafts", Toast.LENGTH_SHORT).show();

        }
        else if (tag.equalsIgnoreCase("draft_primary"))
        {

            packageList = getPackageList();
            packageFrag.refreshList(langCode, isTranslatorModeEnabled(), packageList);
            Toast.makeText(MainPW.this, "Failed to download drafts", Toast.LENGTH_SHORT).show();

        }
        else if (tag.equalsIgnoreCase("draft_parallel"))
        {

            // do nothing

        }
        else if (tag.equalsIgnoreCase("primary") || tag.equalsIgnoreCase("parallel"))
        {

            Toast.makeText(MainPW.this, "Failed to download resources", Toast.LENGTH_SHORT).show();

        }

        hideLoading();
    }

    private class UpdateDraftListTask extends AsyncTask<Object, Void, Boolean>
    {
        boolean mNewDraftsAvailable;
        String tag, langCode;

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            mNewDraftsAvailable = false;
        }

        @Override
        protected Boolean doInBackground(Object... params)
        {

            InputStream is = (InputStream) params[0];
            langCode = params[1].toString();
            tag = params[2].toString();

            List<GTLanguage> languageList = GTPackageReader.processMetaResponse(is);

            GTLanguage language = languageList.get(0);
            List<GTPackage> packagesDraft = language.getPackages();

            return packagesDraft.size() != 0;
        }

        @Override
        protected void onPostExecute(Boolean shouldDownload)
        {
            super.onPostExecute(shouldDownload);

            final SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

            GodToolsApiClient.downloadDrafts((SnuffyApplication) getApplication(), settings.getString("Authorization_Draft", ""), langCode, tag, MainPW.this);
        }
    }

    private void hideLoading()
    {
        isDownloading = false;
        supportInvalidateOptionsMenu();
        tvTask.setText("");
        vLoading.setVisibility(View.GONE);
        packageFrag.enable();

        if (refreshButton != null)
        {
            refreshButton.setEnabled(true);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_MENU)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(false)
                    .setMessage(R.string.quit_dialog_message)
                    .setPositiveButton(R.string.quit_dialog_confirm, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i)
                        {
                            timer.cancel();
                            finish();
                        }
                    })
                    .setNegativeButton(R.string.quit_dialog_cancel, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i)
                        {
                            dialogInterface.cancel();
                        }
                    });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void addPageFrameToIntent(Intent intent)
    {
        intent.putExtra("PageLeft", mPageLeft);
        intent.putExtra("PageTop", mPageTop);
        intent.putExtra("PageWidth", mPageWidth);
        intent.putExtra("PageHeight", mPageHeight);
    }

    public void onCmd_settings(View view)
    {
        Intent intent = new Intent(this, SettingsPW.class);
        startActivityForResult(intent, REQUEST_SETTINGS);
    }

    public void onCmd_refresh(View view)
    {
        if (Device.isConnected(MainPW.this))
        {
            SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);


            if (isTranslatorModeEnabled())
            {
                showLoading("Updating drafts...");
                GodToolsApiClient.getListOfDrafts(settings.getString("Authorization_Draft", ""), languagePrimary, "draft", this);
            }
            else
            {
                showLoading("Updating resources...");
                GodToolsApiClient.downloadLanguagePack((SnuffyApplication) getApplication(),
                        languagePrimary,
                        "primary",
                        settings.getString("Authorization_Generic", ""),
                        this);
            }


        }
        else
        {
            Toast.makeText(MainPW.this, "Internet connection is required", Toast.LENGTH_SHORT).show();
        }
    }

    public void onCmd_add(View view)
    {
        if (Device.isConnected(MainPW.this))
        {
            final SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

            AlertDialog.Builder b = new AlertDialog.Builder(this);
            b.setTitle("Start a draft for: ");

            final LinkedHashMap<String, String> possiblePackagesForDraft = getPossiblePackagesForDraft();

            final String[] packageNames = new ArrayList<String>(possiblePackagesForDraft.values()).toArray(new String[possiblePackagesForDraft.size()]);

            b.setItems(packageNames, new DialogInterface.OnClickListener()
            {

                @Override
                public void onClick(DialogInterface dialog, int which)
                {

                    dialog.dismiss();

                    int i = 0;
                    String packageCode = null;
                    for (Map.Entry<String, String> entry : possiblePackagesForDraft.entrySet())
                    {
                        if (i == which)
                        {
                            packageCode = entry.getKey();
                            break;
                        }
                        i++;
                    }
                    GodToolsApiClient.createDraft(settings.getString("Authorization_Draft", ""),
                            languagePrimary,
                            packageCode,
                            new DraftCreationTask.DraftTaskHandler()
                            {
                                @Override
                                public void draftTaskComplete()
                                {
                                    Toast.makeText(getApplicationContext(), "Draft has been created", Toast.LENGTH_SHORT);
                                    showLoading("Updating drafts...");
                                    GodToolsApiClient.getListOfDrafts(settings.getString("Authorization_Draft", ""), languagePrimary, "draft", MainPW.this);
                                }

                                @Override
                                public void draftTaskFailure()
                                {
                                    Toast.makeText(getApplicationContext(), "Failed to create a new draft", Toast.LENGTH_SHORT);
                                }
                            });
                }

            });

            b.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialogInterface, int i)
                {
                    if (noPackages) onCmd_settings(null);
                }
            });

            b.show();
        }
        else
        {
            Toast.makeText(MainPW.this, "Internet connection is required", Toast.LENGTH_SHORT).show();
        }
    }

    private LinkedHashMap<String, String> getPossiblePackagesForDraft()
    {
        // start with an ArrayList the length of number of packages. it will never be bigger than that.
        LinkedHashMap<String, String> possiblePackages = new LinkedHashMap<String, String>(packageList.size());

        // start with an list of (unfortuantely) hard coded packages
        possiblePackages.put("kgp", "Knowing God Personally");
        possiblePackages.put("fourlaws", "Four Spiritual Laws");
        possiblePackages.put("satisfied", "Satisfied?");

        // loop through the list of loaded packages, and stick in the name of any existing packages (already translated)
        for (GTPackage gtPackage : packageList)
        {
            if ("live".equalsIgnoreCase(gtPackage.getStatus()) &&
                    possiblePackages.containsKey(gtPackage.getCode()))
            {
                possiblePackages.put(gtPackage.getCode(), gtPackage.getName());
            }
        }

        // loop through the list again and remove any that are already in 'draft' status
        for (GTPackage gtPackage : packageList)
        {
            if ("draft".equalsIgnoreCase(gtPackage.getStatus()) &&
                    possiblePackages.containsKey(gtPackage.getCode()))
            {
                possiblePackages.remove(gtPackage.getCode());
            }
        }

        return possiblePackages;
    }

    public void doCmdShare(View v)
    {
        String msgBody = getString(R.string.app_share_link);

        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_TEXT, msgBody);
        startActivity(Intent.createChooser(share, "Select how you would like to share"));
    }

    private Tracker getGoogleAnalyticsTracker()
    {
        return ((SnuffyApplication) getApplication()).getTracker();
    }

    private void trackScreenVisit()
    {
        Tracker tracker = getGoogleAnalyticsTracker();
        tracker.setScreenName("HomeScreen");
        tracker.send(new HitBuilders.AppViewBuilder()
                .setCustomDimension(1, "HomeScreen")
                .setCustomDimension(2, languagePrimary)
                .build());
    }

    private boolean checkPlayServices()
    {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS)
        {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode))
            {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }
            else
            {
                Log.i(TAG, "This device is not supported.");
            }
            return false;
        }
        return true;
    }

    private String getRegistrationId(Context context)
    {
        String registrationId = settings.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = settings.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion)
        {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regid = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;

                    sendRegistrationIdToBackend();

                    // Persist the regID - no need to register again.
                    storeRegistrationId(context, regid);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg)
            {
                Log.i(TAG, msg);
            }
        }.execute(null, null, null);
    }

    private void storeRegistrationId(Context context, String regId) {
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    private static int getAppVersion(Context context) 
    {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    private void sendRegistrationIdToBackend() 
    {
       GodToolsApiClient.registerDeviceForNotifications(regid, new NotificationRegistrationTask.NotificationTaskHandler()
       {
           @Override
           public void registrationComplete(String status)
           {
               Log.i(NotificationInfo.NOTIFICATION_TAG, "API Registration Complete");
           }

           @Override
           public void registrationFailed()
           {
               Log.i(NotificationInfo.NOTIFICATION_TAG, "API Registration Failed");
           }
       });
    }
    
    private void startTimer()
    {
        TimerTask timerTask = new TimerTask()
        {
            @Override
            public void run()
            {
                Log.i(TAG, "Timer complete");
                
                if (isAppInForeground())
                {
                    Log.i(TAG, "App is in foreground");
                    GodToolsApiClient.updateNotification(settings.getString("Authorization_Generic", ""),
                            regid, NotificationInfo.AFTER_3_USES, new NotificationUpdateTask.NotificationUpdateTaskHandler()
                            {
                                @Override
                                public void registrationComplete(String regId)
                                {
                                    Log.i(NotificationInfo.NOTIFICATION_TAG, "3 Uses Notification notice sent to API");
                                }

                                @Override
                                public void registrationFailed()
                                {
                                    Log.e(NotificationInfo.NOTIFICATION_TAG, "3 Uses notification notice failed to send to API");
                                }
                            });
                }
                else
                {
                    Log.i(TAG, "App not in foreground, canceling timer");
                }
            }
        };
        
        timer = new Timer("1.5MinuteTimer");
        timer.schedule(timerTask, 90000); //1.5 minutes
        Log.i(TAG, "Timer scheduled");
    }
    
    private boolean isAppInForeground()
    {
        ActivityManager activityManager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> services = activityManager.getRunningTasks(1);
        
        return (services.get(0).topActivity.getPackageName()
                .equalsIgnoreCase(getApplicationContext().getPackageName()));
    }
}