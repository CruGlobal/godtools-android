package org.keynote.godtools.android;


import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.keynote.godtools.android.broadcast.BroadcastUtil;
import org.keynote.godtools.android.broadcast.Type;
import org.keynote.godtools.android.business.GTLanguage;
import org.keynote.godtools.android.business.GTPackage;
import org.keynote.godtools.android.business.GTPackageReader;
import org.keynote.godtools.android.everystudent.EveryStudent;
import org.keynote.godtools.android.expandableList.ExpandableListAdapter;
import org.keynote.godtools.android.http.DownloadTask;
import org.keynote.godtools.android.http.GodToolsApiClient;
import org.keynote.godtools.android.http.MetaTask;
import org.keynote.godtools.android.service.BackgroundService;
import org.keynote.godtools.android.snuffy.SnuffyApplication;
import org.keynote.godtools.android.utils.Device;

import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import static org.keynote.godtools.android.utils.Constants.PREFS_NAME;
import static org.keynote.godtools.android.utils.Constants.REFERENCE_DEVICE_HEIGHT;
import static org.keynote.godtools.android.utils.Constants.REFERENCE_DEVICE_WIDTH;
import static org.keynote.godtools.android.utils.Constants.REQUEST_SETTINGS;
import static org.keynote.godtools.android.utils.Constants.RESULT_CHANGED_PARALLEL;
import static org.keynote.godtools.android.utils.Constants.RESULT_CHANGED_PRIMARY;
import static org.keynote.godtools.android.utils.Constants.RESULT_DOWNLOAD_PARALLEL;
import static org.keynote.godtools.android.utils.Constants.RESULT_DOWNLOAD_PRIMARY;
import static org.keynote.godtools.android.utils.Constants.RESULT_PREVIEW_MODE_DISABLED;
import static org.keynote.godtools.android.utils.Constants.RESULT_PREVIEW_MODE_ENABLED;


public class PreviewModeMainPW extends ActionBarActivity implements
        DownloadTask.DownloadTaskHandler,
        MetaTask.MetaTaskHandler, View.OnClickListener
{
    private static final String TAG = "PreviewModeMainPW";

    private int mPageLeft;
    private int mPageTop;
    private int mPageWidth;
    private int mPageHeight;
    private String languagePrimary;
    private List<GTPackage> packageList;
    private SwipeRefreshLayout swipeRefreshLayout;
    
    private LocalBroadcastManager broadcastManager;
    private BroadcastReceiver broadcastReceiver;

    Context context;
    
    SharedPreferences settings;
    
    ExpandableListAdapter listAdapter;
    ExpandableListView listView;
    

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.preview_mode_main_pw);
        
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                Log.i(TAG, "Starting refresh");
                onCmd_refresh();
            }
        });

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(R.layout.titlebar_centered_title);
        TextView titleBar = (TextView) actionBar.getCustomView().findViewById(R.id.titlebar_title);
        titleBar.setText(R.string.preview_mode_title);

        context = getApplicationContext();
        setupBroadcastReceiver();

        settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        languagePrimary = settings.getString(GTLanguage.KEY_PRIMARY, "en");

        getPackageList(); // get the packages for the primary language
    }
    
    private void setupExpandableList()
    {
        listView = (ExpandableListView) findViewById(R.id.expandable_list);
        listAdapter = new ExpandableListAdapter(this, packageList, languagePrimary);
        listView.setAdapter(listAdapter);

        listView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener()
        {
            @Override
            public boolean onGroupClick(ExpandableListView expandableListView, View view, int i, long l)
            {
                TextView textView = (TextView) view.findViewById(R.id.tv_trans_view);
                String packageName = (String) textView.getText();
                Log.i(TAG, "Clicked: " + packageName);

                for (GTPackage gtPackage : packageList)
                {
                    if (packageName.equals(gtPackage.getName()))
                    {
                        if (gtPackage.getCode().equalsIgnoreCase("everystudent"))
                        {
                            Intent intent = new Intent(context, EveryStudent.class);
                            intent.putExtra("PackageName", gtPackage.getCode());
                            addPageFrameToIntent(intent);
                            startActivity(intent);
                            return true;
                        }

                        Intent intent = new Intent(context, SnuffyPWActivity.class);
                        intent.putExtra("PackageName", gtPackage.getCode());
                        intent.putExtra("LanguageCode", gtPackage.getLanguage());
                        intent.putExtra("ConfigFileName", gtPackage.getConfigFileName());
                        intent.putExtra("Status", gtPackage.getStatus());
                        addPageFrameToIntent(intent);
                        startActivity(intent);
                    }
                }

                return true;
            }
        });
    }
    
    private void setupBroadcastReceiver()
    {
        broadcastManager = LocalBroadcastManager.getInstance(context);
        
        broadcastReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                if (BroadcastUtil.ACTION_START.equals(intent.getAction())) Log.i(TAG, "Action started");
                else if (BroadcastUtil.ACTION_STOP.equals(intent.getAction()))
                {
                    Log.i(TAG, "Action Done");

                    Type type = (Type) intent.getSerializableExtra(BroadcastUtil.ACTION_TYPE);
                    
                    switch (type)
                    {
                        case AUTH:
                            Log.i(TAG, "Auth Task complete");
                            BackgroundService.getListOfPackages(PreviewModeMainPW.this);
                            break;
                        case DOWNLOAD_TASK:
                            Log.i(TAG, "Download complete");
                            getPackageList();
                            createTheHomeScreen();
                            break;
                        case DRAFT_CREATION_TASK:
                            Log.i(TAG, "Create broadcast received");
                            GodToolsApiClient.getListOfDrafts(settings.getString("Authorization_Draft", ""), languagePrimary, "draft", PreviewModeMainPW.this);
                            break;
                        case DRAFT_PUBLISH_TASK:
                            Log.i(TAG, "Publish broadcast received");
                            GodToolsApiClient.getListOfDrafts(settings.getString("Authorization_Draft", ""), languagePrimary, "draft_primary", PreviewModeMainPW.this);
                            break;
                        case META_TASK:
                            break;
                        case DISABLE_TRANSLATOR:
                            finish();
                            break;
                        case ERROR:
                            Log.i(TAG, "Error");
                            break;
                    }
                }

                if (BroadcastUtil.ACTION_FAIL.equals(intent.getAction()))
                {
                    Log.i(TAG, "Action Failed: " + intent.getSerializableExtra(BroadcastUtil.ACTION_TYPE));
                    getPackageList();
                    createTheHomeScreen();
                }
            }
        };
        
        broadcastManager.registerReceiver(broadcastReceiver, BroadcastUtil.startFilter());
        broadcastManager.registerReceiver(broadcastReceiver, BroadcastUtil.stopFilter());
        broadcastManager.registerReceiver(broadcastReceiver, BroadcastUtil.failedFilter());
    }
    
    private void removeBroadcastReceiver()
    {
        broadcastManager.unregisterReceiver(broadcastReceiver);
        broadcastReceiver = null;        
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
                    getPackageList();
                }

                String code = data.getStringExtra("parallelCode");
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

                GodToolsApiClient.getListOfDrafts(settings.getString("Authorization_Draft", ""), languagePrimary, "draft_primary", this);

                Toast.makeText(PreviewModeMainPW.this, "Translator preview mode is enabled", Toast.LENGTH_LONG).show();

                finish();
                startActivity(getIntent());

                break;
            }
            case RESULT_PREVIEW_MODE_DISABLED:
            {
                // This should not happen but just in case
                
                Intent intent = new Intent(this, MainPW.class);
                startActivity(intent);
                finish();
                
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
        getPackageList();

        if(withFallback && packageList.isEmpty())
        {
            SharedPreferences.Editor editor = settings.edit();
            editor.putString(GTLanguage.KEY_PRIMARY, "en");
            editor.apply();
            languagePrimary = "en";
            getPackageList();
        }
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
        int width;
        int height;
        int left;
        int top;

        double aspectRatioTarget = (double) REFERENCE_DEVICE_WIDTH / (double) REFERENCE_DEVICE_HEIGHT;
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

        getPackageList();

        trackScreenVisit();
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

            GTLanguage gtl = GTLanguage.getLanguage(PreviewModeMainPW.this, langCode);
            gtl.setDownloaded(true);
            gtl.update(PreviewModeMainPW.this);

            if (isTranslatorModeEnabled())
            {
                // check for draft_primary
                GodToolsApiClient.getListOfDrafts(settings.getString("Authorization_Draft", ""), langCode, "draft_primary", this);
            }
            else
            {
                getPackageList();
            }
            createTheHomeScreen();
        }
        else if (tag.equalsIgnoreCase("parallel"))
        {

            SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString(GTLanguage.KEY_PARALLEL, langCode);
            editor.apply();

            GTLanguage gtl = GTLanguage.getLanguage(PreviewModeMainPW.this, langCode);
            gtl.setDownloaded(true);
            gtl.update(PreviewModeMainPW.this);

            if (isTranslatorModeEnabled())
            {
                // check for draft_parallel
                GodToolsApiClient.getListOfDrafts(settings.getString("Authorization_Draft", ""), langCode, "draft_parallel", this);
            }

            createTheHomeScreen();
        }
        else if (tag.equalsIgnoreCase("draft"))
        {
            Toast.makeText(PreviewModeMainPW.this, "Drafts have been updated", Toast.LENGTH_SHORT).show();
            getPackageList();
            createTheHomeScreen();
            
            swipeRefreshLayout.setRefreshing(false);
            Log.i(TAG, "Done refreshing");
        }
        else if (tag.equalsIgnoreCase("draft_primary"))
        {
            languagePrimary = langCode;
            getPackageList();

            createTheHomeScreen();
        }
        else if (tag.equalsIgnoreCase("draft_parallel"))
        {
            createTheHomeScreen();
        }
    }

    private void getPackageList()
    {
        boolean kgpPresent = false;
        boolean satisfiedPresent = false;
        boolean fourlawsPresent = false;
        
        // only return draft packages with translator mode
        List<GTPackage> packageByLanguage = GTPackage.getDraftPackages(PreviewModeMainPW.this, languagePrimary);
        if("en".equals(languagePrimary))
        {
            removeEveryStudent(packageByLanguage);
        }

        Log.i(TAG, "Package size: " + packageByLanguage.size());

        for (GTPackage gtPackage : packageByLanguage)
        {

            if ("kgp".equals(gtPackage.getCode())) kgpPresent = true;
            if ("satisfied".equals(gtPackage.getCode())) satisfiedPresent = true;
            if ("fourlaws".equals(gtPackage.getCode())) fourlawsPresent = true;
        }

        if (!kgpPresent || !satisfiedPresent || !fourlawsPresent)
        {
            
            if (!kgpPresent)
            {
                GTPackage kgpPack = new GTPackage();
                kgpPack.setCode("draftkgp");
                kgpPack.setName("Knowing God Personally");
                packageByLanguage.add(kgpPack);
            }

            if (!satisfiedPresent)
            {
                GTPackage satPack = new GTPackage();
                satPack.setCode("draftsatisfied");
                satPack.setName("Satisfied?");
                packageByLanguage.add(satPack);
            }

            if (!fourlawsPresent)
            {
                GTPackage fourLawPack = new GTPackage();
                fourLawPack.setCode("draftfourlaws");
                fourLawPack.setName("The Four Spiritual Laws");
                packageByLanguage.add(fourLawPack);
            }
        }

        Log.i(TAG, "Package Size v2: " + packageByLanguage.size());
        
        packageList = packageByLanguage;
        
        setupExpandableList();
    }

    private void removeEveryStudent(List<GTPackage> packages)
    {
        Iterator<GTPackage> i = packages.iterator();
        for(; i.hasNext(); )
        {
            if(i.next().getCode().equals(GTPackage.EVERYSTUDENT_PACKAGE_CODE)) i.remove();
        }
    }

    private boolean isTranslatorModeEnabled()
    {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return settings.getBoolean("TranslatorMode", false);
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
            getPackageList();
        }

        Toast.makeText(PreviewModeMainPW.this, "Failed to update drafts", Toast.LENGTH_SHORT).show();
        swipeRefreshLayout.setRefreshing(false);
        Log.i(TAG, "Done refreshing");
    }

    @Override
    public void downloadTaskFailure(String url, String filePath, String langCode, String tag)
    {

        if (tag.equalsIgnoreCase("draft"))
        {
            Toast.makeText(PreviewModeMainPW.this, "Failed to update drafts", Toast.LENGTH_SHORT).show();
        }
        else if (tag.equalsIgnoreCase("draft_primary"))
        {
            getPackageList();
            Toast.makeText(PreviewModeMainPW.this, "Failed to download drafts", Toast.LENGTH_SHORT).show();
        }
        else if (tag.equalsIgnoreCase("primary") || tag.equalsIgnoreCase("parallel"))
        {
            Toast.makeText(PreviewModeMainPW.this, "Failed to download resources", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View view)
    {
        Log.i(TAG, "View clicked");
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

            GodToolsApiClient.downloadDrafts((SnuffyApplication) getApplication(), settings.getString("Authorization_Draft", ""), langCode, tag, PreviewModeMainPW.this);
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

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        removeBroadcastReceiver();
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

    private void onCmd_refresh()
    {
        if (Device.isConnected(PreviewModeMainPW.this))
        {
            SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

            GodToolsApiClient.getListOfDrafts(settings.getString("Authorization_Draft", ""), languagePrimary, "draft", this);

        }
        else
        {
            Toast.makeText(PreviewModeMainPW.this, "Internet connection is required", Toast.LENGTH_SHORT).show();
        }
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
}