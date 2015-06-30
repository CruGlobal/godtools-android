package org.keynote.godtools.android;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
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

import org.keynote.godtools.android.broadcast.BroadcastUtil;
import org.keynote.godtools.android.broadcast.Type;
import org.keynote.godtools.android.business.GTLanguage;
import org.keynote.godtools.android.business.GTPackage;
import org.keynote.godtools.android.business.GTPackageReader;
import org.keynote.godtools.android.everystudent.EveryStudent;
import org.keynote.godtools.android.expandableList.ExpandableListAdapter;
import org.keynote.godtools.android.fragments.AccessCodeDialogFragment;
import org.keynote.godtools.android.googleAnalytics.EventTracker;
import org.keynote.godtools.android.http.DownloadTask;
import org.keynote.godtools.android.http.GodToolsApiClient;
import org.keynote.godtools.android.http.MetaTask;
import org.keynote.godtools.android.service.BackgroundService;
import org.keynote.godtools.android.snuffy.SnuffyApplication;
import org.keynote.godtools.android.utils.Device;

import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import static org.keynote.godtools.android.utils.Constants.AUTH_DRAFT;
import static org.keynote.godtools.android.utils.Constants.CONFIG_FILE_NAME;
import static org.keynote.godtools.android.utils.Constants.EMPTY_STRING;
import static org.keynote.godtools.android.utils.Constants.ENGLISH_DEFAULT;
import static org.keynote.godtools.android.utils.Constants.FOUR_LAWS;
import static org.keynote.godtools.android.utils.Constants.KGP;
import static org.keynote.godtools.android.utils.Constants.LANGUAGE_CODE;
import static org.keynote.godtools.android.utils.Constants.PACKAGE_NAME;
import static org.keynote.godtools.android.utils.Constants.PAGE_HEIGHT;
import static org.keynote.godtools.android.utils.Constants.PAGE_LEFT;
import static org.keynote.godtools.android.utils.Constants.PAGE_TOP;
import static org.keynote.godtools.android.utils.Constants.PAGE_WIDTH;
import static org.keynote.godtools.android.utils.Constants.PREFS_NAME;
import static org.keynote.godtools.android.utils.Constants.REFERENCE_DEVICE_HEIGHT;
import static org.keynote.godtools.android.utils.Constants.REFERENCE_DEVICE_WIDTH;
import static org.keynote.godtools.android.utils.Constants.RESULT_CHANGED_PARALLEL;
import static org.keynote.godtools.android.utils.Constants.RESULT_CHANGED_PRIMARY;
import static org.keynote.godtools.android.utils.Constants.SATISFIED;
import static org.keynote.godtools.android.utils.Constants.STATUS;
import static org.keynote.godtools.android.utils.Constants.STATUS_CODE;


public class PreviewModeMainPW extends ActionBarActivity implements
        DownloadTask.DownloadTaskHandler,
        MetaTask.MetaTaskHandler, View.OnClickListener,
        AccessCodeDialogFragment.AccessCodeDialogListener
{
    private static final String TAG = PreviewModeMainPW.class.getSimpleName();
    private static final int REQUEST_SETTINGS = 1001;

    private int mPageLeft;
    private int mPageTop;
    private int mPageWidth;
    private int mPageHeight;
    private String languagePrimary;
    private List<GTPackage> packageList;
    private SwipeRefreshLayout swipeRefreshLayout;
    
    private LocalBroadcastManager broadcastManager;
    private BroadcastReceiver broadcastReceiver;

    private SharedPreferences settings;

    private Context context;

    private ProgressDialog pdLoading;
    

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
        languagePrimary = settings.getString(GTLanguage.KEY_PRIMARY, ENGLISH_DEFAULT);

        swipeRefreshLayout.setRefreshing(true);
        onCmd_refresh();
    }
    
    private void setupExpandableList()
    {
        ExpandableListView listView = (ExpandableListView) findViewById(R.id.expandable_list);
        ExpandableListAdapter listAdapter = new ExpandableListAdapter(this, packageList, languagePrimary);
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
                            intent.putExtra(PACKAGE_NAME, gtPackage.getCode());
                            addPageFrameToIntent(intent);
                            startActivity(intent);
                            return true;
                        }
                        else if (gtPackage.isAvailable())
                        {
                            Intent intent = new Intent(context, SnuffyPWActivity.class);
                            intent.putExtra(PACKAGE_NAME, gtPackage.getCode());
                            intent.putExtra(LANGUAGE_CODE, gtPackage.getLanguage());
                            intent.putExtra(CONFIG_FILE_NAME, gtPackage.getConfigFileName());
                            intent.putExtra(STATUS, gtPackage.getStatus());
                            addPageFrameToIntent(intent);
                            startActivity(intent);
                        }
                        else
                        {
                            Toast.makeText(context, getString(R.string.package_not_created), Toast.LENGTH_SHORT).show();
                        }
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

                if (pdLoading != null) pdLoading.dismiss();

                if (BroadcastUtil.ACTION_START.equals(intent.getAction())) Log.i(TAG, "Action started");
                else if (BroadcastUtil.ACTION_STOP.equals(intent.getAction()))
                {
                    Type type = (Type) intent.getSerializableExtra(BroadcastUtil.ACTION_TYPE);

                    Log.i(TAG, "Action Done, TYPE: " + type.toString());
                    
                    switch (type)
                    {
                        case AUTH:
                            Log.i(TAG, "Auth Task complete");
                            BackgroundService.getListOfPackages(PreviewModeMainPW.this);
                            break;
                        case DOWNLOAD_TASK:
                            Log.i(TAG, "Download complete");
                            break;
                        case DRAFT_CREATION_TASK:
                            Log.i(TAG, "Create broadcast received");
                            GodToolsApiClient.getListOfDrafts(settings.getString(AUTH_DRAFT, EMPTY_STRING),
                                    languagePrimary, "draft", PreviewModeMainPW.this);
                            break;
                        case DRAFT_PUBLISH_TASK:
                            Log.i(TAG, "Publish broadcast received");
                            GodToolsApiClient.getListOfDrafts(settings.getString(AUTH_DRAFT, EMPTY_STRING),
                                    languagePrimary, "draft_primary", PreviewModeMainPW.this);
                            break;
                        case META_TASK:
                            break;
                        case DISABLE_TRANSLATOR:
                            finish();
                            break;
                        case ENABLE_TRANSLATOR:
                            onCmd_refresh();
                            break;
                        case ERROR:
                            Log.i(TAG, "Error");
                            break;
                    }
                }

                if (BroadcastUtil.ACTION_FAIL.equals(intent.getAction()))
                {
                    Log.i(TAG, "Action Failed: " + intent.getSerializableExtra(BroadcastUtil.ACTION_TYPE));

                    if (intent.getIntExtra(STATUS_CODE, 0) == 401)
                    {
                        Toast.makeText(PreviewModeMainPW.this, getString(R.string.expired_passcode),
                                Toast.LENGTH_LONG).show();
                        showAccessCodeDialog();
                    }
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

        switch (resultCode)
        {
            /* It's possible that both primary and parallel languages that were previously downloaded were changed at the same time.
             * If only one or the other were changed, no harm in running this code, but we do need to make sure the main screen updates
             * if the both were changed.  If if both were changed RESULT_CHANGED_PARALLEL were not added here, then the home screen would
             * not reflect the changed primary language*/
            case RESULT_CHANGED_PRIMARY:
            case RESULT_CHANGED_PARALLEL:
            {
                getApp().setAppLocale(settings.getString(GTLanguage.KEY_PRIMARY, EMPTY_STRING));

                swipeRefreshLayout.setRefreshing(true);
                onCmd_refresh();

                break;
            }
        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        settings.edit().apply();
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
        int width, height, left, top;

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
        getPackageList();
        EventTracker.track(getApp(), "Translator Page", languagePrimary);
    }

    @Override
    public void downloadTaskComplete(String url, String filePath, String langCode, String tag)
    {
        if (tag.equalsIgnoreCase("draft"))
        {
            Toast.makeText(PreviewModeMainPW.this, getString(R.string.drafts_updated),
                    Toast.LENGTH_SHORT).show();
            createTheHomeScreen();

            swipeRefreshLayout.setRefreshing(false);
            Log.i(TAG, "Done refreshing");
        }
        else if (tag.equalsIgnoreCase("draft_primary"))
        {
            languagePrimary = langCode;
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
        if(ENGLISH_DEFAULT.equals(languagePrimary))
        {
            removeEveryStudent(packageByLanguage);
        }

        Log.i(TAG, "Package size: " + packageByLanguage.size());

        for (GTPackage gtPackage : packageByLanguage)
        {

            if (KGP.equals(gtPackage.getCode())) kgpPresent = true;
            if (SATISFIED.equals(gtPackage.getCode())) satisfiedPresent = true;
            if (FOUR_LAWS.equals(gtPackage.getCode())) fourlawsPresent = true;
        }

        if (!kgpPresent || !satisfiedPresent || !fourlawsPresent)
        {
            
            if (!kgpPresent)
            {
                GTPackage kgpPack = new GTPackage();
                kgpPack.setCode("draftkgp");
                kgpPack.setName(getString(R.string.menu_item_kgp));
                kgpPack.setAvailable(false);
                packageByLanguage.add(kgpPack);
            }

            if (!satisfiedPresent)
            {
                GTPackage satPack = new GTPackage();
                satPack.setCode("draftsatisfied");
                satPack.setName(getString(R.string.menu_item_satisfied));
                satPack.setAvailable(false);
                packageByLanguage.add(satPack);
            }

            if (!fourlawsPresent)
            {
                GTPackage fourLawPack = new GTPackage();
                fourLawPack.setCode("draftfourlaws");
                fourLawPack.setName(getString(R.string.menu_item_4laws));
                fourLawPack.setAvailable(false);
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

    @Override
    public void metaTaskComplete(InputStream is, String langCode, String tag)
    {
        // process the input stream
        new UpdateDraftListTask().execute(is, langCode, tag);
    }

    @Override
    public void metaTaskFailure(InputStream is, String langCode, String tag, int statusCode)
    {
        if (401 == statusCode)
        {
            showAccessCodeDialog();
            Toast.makeText(PreviewModeMainPW.this, getString(R.string.expired_passcode),
                    Toast.LENGTH_LONG).show();
        }
        else
        {
            Toast.makeText(PreviewModeMainPW.this, getString(R.string.failed_update_draft),
                    Toast.LENGTH_SHORT).show();
        }

        if (tag.equalsIgnoreCase("draft") || tag.equalsIgnoreCase("draft_primary"))
        {
            getPackageList();
        }

        swipeRefreshLayout.setRefreshing(false);
        Log.i(TAG, "Meta Failed: Done refreshing");
    }

    @Override
    public void downloadTaskFailure(String url, String filePath, String langCode, String tag)
    {

        if (tag.equalsIgnoreCase("draft"))
        {
            Toast.makeText(PreviewModeMainPW.this, getString(R.string.failed_update_draft),
                    Toast.LENGTH_SHORT).show();
        }
        else if (tag.equalsIgnoreCase("draft_primary"))
        {
            getPackageList();
            Toast.makeText(PreviewModeMainPW.this, getString(R.string.failed_download_draft),
                    Toast.LENGTH_SHORT).show();
        }
        else if (tag.equalsIgnoreCase("primary") || tag.equalsIgnoreCase("parallel"))
        {
            Toast.makeText(PreviewModeMainPW.this, getString(R.string.failed_download_resources),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View view)
    {
        Log.i(TAG, "View clicked");
    }

    private class UpdateDraftListTask extends AsyncTask<Object, Void, Boolean>
    {
        String tag, langCode;

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

            GodToolsApiClient.downloadDrafts((SnuffyApplication) getApplication(),
                    settings.getString(AUTH_DRAFT, EMPTY_STRING), langCode, tag, PreviewModeMainPW.this);
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
        intent.putExtra(PAGE_LEFT, mPageLeft);
        intent.putExtra(PAGE_TOP, mPageTop);
        intent.putExtra(PAGE_WIDTH, mPageWidth);
        intent.putExtra(PAGE_HEIGHT, mPageHeight);
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
            GodToolsApiClient.getListOfDrafts(settings.getString(AUTH_DRAFT, EMPTY_STRING),
                    languagePrimary, "draft", this);
        }
        else
        {
            Toast.makeText(PreviewModeMainPW.this, getString(R.string.internet_needed),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void doCmdShare()
    {
        String msgBody = getString(R.string.app_share_link);

        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_TEXT, msgBody);
        startActivity(Intent.createChooser(share, getString(R.string.select_share)));
    }

    private SnuffyApplication getApp()
    {
        return (SnuffyApplication) getApplication();
    }

    private void showAccessCodeDialog()
    {
        FragmentManager fm = getSupportFragmentManager();
        DialogFragment frag = (DialogFragment) fm.findFragmentByTag("access_dialog");
        if (frag == null)
        {
            frag = new AccessCodeDialogFragment();
            frag.setCancelable(false);
            frag.show(fm, "access_dialog");
        }
    }

    @Override
    public void onAccessDialogClick(boolean success)
    {
        if (!success)
        {
            if (pdLoading != null) pdLoading.dismiss();
        }
        else
        {
            showLoading(getString(R.string.authenticate_code));
        }
    }

    private void showLoading(String msg)
    {
        pdLoading = new ProgressDialog(PreviewModeMainPW.this);
        pdLoading.setCancelable(false);
        pdLoading.setMessage(msg);
        pdLoading.show();
    }
}