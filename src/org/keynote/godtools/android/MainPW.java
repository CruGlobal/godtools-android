package org.keynote.godtools.android;


import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import org.keynote.godtools.android.fragments.PackageListFragment;
import org.keynote.godtools.android.http.DownloadTask;
import org.keynote.godtools.android.http.GodToolsApiClient;
import org.keynote.godtools.android.http.MetaTask;
import org.keynote.godtools.android.http.NotificationRegistrationTask;
import org.keynote.godtools.android.http.NotificationUpdateTask;
import org.keynote.godtools.android.model.HomescreenLayout;
import org.keynote.godtools.android.notifications.NotificationInfo;
import org.keynote.godtools.android.snuffy.SnuffyApplication;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class MainPW extends BaseActionBarActivity implements PackageListFragment.OnPackageSelectedListener,
        DownloadTask.DownloadTaskHandler,
        MetaTask.MetaTaskHandler, View.OnClickListener
{
    private static final String TAG = "MainPW";
    private static final int REQUEST_SETTINGS = 1001;
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
    private String languagePrimary;
    private List<GTPackage> packageList;
    
    private List<HomescreenLayout> layouts;

    View vLoading;
    TextView tvTask;
    ImageButton refreshButton;
    GoogleCloudMessaging gcm;
    Context context;
    String regid = "";
    Timer timer;
    /**
     * When clicked, dialog to launch a new translation is opened
     */
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
        overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_pw);

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(R.layout.titlebar_centered_title);
        TextView titleBar = (TextView) actionBar.getCustomView().findViewById(R.id.titlebar_title);
        titleBar.setText(R.string.app_title);

        context = getApplicationContext();
        vLoading = findViewById(R.id.contLoading);
        tvTask = (TextView) findViewById(R.id.tvTask);
        
        setupLayout();

        settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        
        if (settings.getBoolean("TranslatorMode", false))
        {
            Intent intent = new Intent(this, PreviewModeMainPW.class);
            startActivity(intent);
            finish();
        }
        
        languagePrimary = settings.getString(GTLanguage.KEY_PRIMARY, "en");
        justSwitchedToTranslatorMode = settings.getBoolean(JUST_SWITCHED, false);

        packageList = getPackageList(); // get the packages for the primary language
        
        showLayoutsWithPackages();
        
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
    
    private void setupLayout()
    {
        layouts = new ArrayList<HomescreenLayout>();
        
        HomescreenLayout first = new HomescreenLayout();

        first.setLayout((LinearLayout) findViewById(R.id.first_layout));
        first.setTextView((TextView) findViewById(R.id.tv_first));
        first.setImageView((ImageView) findViewById(R.id.iv_first));
        layouts.add(first);

        HomescreenLayout second = new HomescreenLayout();

        second.setLayout((LinearLayout) findViewById(R.id.second_layout));
        second.setTextView((TextView) findViewById(R.id.tv_second));
        second.setImageView((ImageView) findViewById(R.id.iv_second));
        layouts.add(second);

        HomescreenLayout third = new HomescreenLayout();

        third.setLayout((LinearLayout) findViewById(R.id.third_layout));
        third.setTextView((TextView) findViewById(R.id.tv_third));
        third.setImageView((ImageView) findViewById(R.id.iv_third));
        layouts.add(third);

        HomescreenLayout fourth = new HomescreenLayout();

        fourth.setLayout((LinearLayout) findViewById(R.id.fourth_layout));
        fourth.setTextView((TextView) findViewById(R.id.tv_fourth));
        fourth.setImageView((ImageView) findViewById(R.id.iv_fourth));
        layouts.add(fourth);

    }
    
    private void showLayoutsWithPackages()
    {
        // now there will only be four packages shown on the homescreen
        for (int i = 0; i < 4; i++)
        {
            if (packageList.size() > i)
            {
                GTPackage gtPackage = packageList.get(i);
                HomescreenLayout layout = layouts.get(i);
                
                gtPackage.setLayout(layout);

                layout.getLayout().setVisibility(View.VISIBLE);
                layout.getLayout().setClickable(true);
                layout.getLayout().setOnClickListener(this);
                layout.getTextView().setText(gtPackage.getName());
                
                if ("kgp".equals(gtPackage.getCode())) layout.getImageView().setImageResource(R.drawable.gt4_homescreen_kgpicon);
                if ("fourlaws".equals(gtPackage.getCode())) layout.getImageView().setImageResource(R.drawable.gt4_homescreen_4lawsicon);
                if ("satisfied".equals(gtPackage.getCode())) layout.getImageView().setImageResource(R.drawable.gt4_homescreen_satisfiedicon);
                if ("everystudent".equals(gtPackage.getCode())) layout.getImageView().setImageResource(R.drawable.gt4_homescreen_esicon);

            }
            else
            {
                HomescreenLayout layout = layouts.get(i);
                layout.getLayout().setVisibility(View.INVISIBLE);
                layout.getLayout().setClickable(false);
            }
        }           
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.homescreen_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.homescreen_settings:
                onCmd_settings();
                return true;
            case R.id.homescreen_share:
                doCmdShare();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        final SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        switch (resultCode)
        {
            /* It's possible that both primary and parallel languages that were previously downloaded were changed at the same time.
             * If only one or the other were changed, no harm in running this code, but we do need to make sure the main screen updates
             * if the both were changed.  If if both were changed RESULT_CHANGED_PARALLEL were not added here, then the home screen would
             * not reflect the changed primary language*/
            case RESULT_CHANGED_PRIMARY:
            case RESULT_CHANGED_PARALLEL:
            {
                SnuffyApplication app = (SnuffyApplication) getApplication();
                app.setAppLocale(settings.getString(GTLanguage.KEY_PRIMARY, ""));

                refreshPackageList(settings, false);
                createTheHomeScreen();

                break;
            }
            case RESULT_DOWNLOAD_PRIMARY:
            {
                // start the download
                String code = data.getStringExtra("primaryCode");

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
                    showLayoutsWithPackages();
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

                refreshPackageList(settings, true);

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

    /**
     * @param withFallback specifies when true will fallback to English if the primary language code
     *                     has no packages available.  This is true when leaving translator mode in a language with all
     *                     drafts and no published live versions.
     */
    private void refreshPackageList(SharedPreferences settings, boolean withFallback)
    {
        languagePrimary = settings.getString(GTLanguage.KEY_PRIMARY, "");
        packageList = getPackageList();

        if(withFallback && packageList.isEmpty())
        {
            SharedPreferences.Editor editor = settings.edit();
            editor.putString(GTLanguage.KEY_PRIMARY, "en");
            editor.apply();
            languagePrimary = "en";
            packageList = getPackageList();
        }
        showLayoutsWithPackages();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor ed = settings.edit();
        ed.apply();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        doSetup();
    }

    private void doSetup()
    {
        createTheHomeScreen();
        getScreenSize();
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
        top = (rect.height() - height) / 2;

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

        // If no packages are available for a language, then fallback to English
        if (justSwitchedToTranslatorMode)
        {
            /*
             * When switching to translator mode, the MainPW activity is restarted. However, the packageList and
             * packageFrag need to be refreshed based on the newly downloaded items. The justSwitchedToTranslatorMode is
             * saved in the settings and when true, this will refresh the packages available.
             */
            packageList = getPackageList();
            showLayoutsWithPackages();
        }

        noPackages = false;

        justSwitchedToTranslatorMode = false;
        switchedToTranslatorMode(false);
        trackScreenVisit();
    }

    private void showLoading(String msg)
    {
        isDownloading = true;
        supportInvalidateOptionsMenu();
        tvTask.setText(msg);
        vLoading.setVisibility(View.VISIBLE);

        if (refreshButton != null)
        {
            refreshButton.setEnabled(false);
        }
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
            editor.apply();

            GTLanguage gtl = GTLanguage.getLanguage(MainPW.this, langCode);
            gtl.setDownloaded(true);
            gtl.update(MainPW.this);

            packageList = getPackageList();
            showLayoutsWithPackages();
            hideLoading();
            
            createTheHomeScreen();
        }
        else if (tag.equalsIgnoreCase("parallel"))
        {

            SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString(GTLanguage.KEY_PARALLEL, langCode);
            editor.apply();

            GTLanguage gtl = GTLanguage.getLanguage(MainPW.this, langCode);
            gtl.setDownloaded(true);
            gtl.update(MainPW.this);

            hideLoading();
            createTheHomeScreen();
        }
        else if (tag.equalsIgnoreCase("draft"))
        {
            Toast.makeText(MainPW.this, "Drafts have been updated", Toast.LENGTH_SHORT).show();
            packageList = getPackageList();
            showLayoutsWithPackages();
            hideLoading();
            createTheHomeScreen();
        }
        else if (tag.equalsIgnoreCase("draft_primary"))
        {
            languagePrimary = langCode;
            packageList = getPackageList();

            showLayoutsWithPackages();

            hideLoading();
            createTheHomeScreen();
        }
        else if (tag.equalsIgnoreCase("draft_parallel"))
        {
            hideLoading();
            createTheHomeScreen();
        }
    }

    private List<GTPackage> getPackageList()
    {
        return GTPackage.getLivePackages(MainPW.this, languagePrimary);
    }

    private void switchedToTranslatorMode(boolean switched)
    {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(JUST_SWITCHED, switched);
        editor.apply();
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

        Intent intent = new Intent(this, SnuffyPWActivity.class);
        intent.putExtra("PackageName", gtPackage.getCode());
        intent.putExtra("LanguageCode", gtPackage.getLanguage());
        intent.putExtra("ConfigFileName", gtPackage.getConfigFileName());
        intent.putExtra("Status", gtPackage.getStatus());
        addPageFrameToIntent(intent);
        startActivity(intent);
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
            showLayoutsWithPackages();
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
            showLayoutsWithPackages();
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

    @Override
    public void onClick(View view)
    {
        for (GTPackage gtPackage : packageList)
        {
            if (view.getId() == gtPackage.getLayout().getLayout().getId())
            {
                Log.i(TAG, "clicked: " + gtPackage.getCode());
                onPackageSelected(gtPackage);
            }
        }
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

    private void onCmd_settings()
    {
        Intent intent = new Intent(this, SettingsPW.class);
        startActivityForResult(intent, REQUEST_SETTINGS);
    }

    private void doCmdShare()
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
        editor.apply();
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
       String deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
       GodToolsApiClient.registerDeviceForNotifications(regid, deviceId, new NotificationRegistrationTask.NotificationTaskHandler()
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