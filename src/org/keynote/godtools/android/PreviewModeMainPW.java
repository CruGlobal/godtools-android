package org.keynote.godtools.android;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.keynote.godtools.android.business.GTLanguage;
import org.keynote.godtools.android.business.GTPackage;
import org.keynote.godtools.android.business.GTPackageReader;
import org.keynote.godtools.android.everystudent.EveryStudent;
import org.keynote.godtools.android.fragments.PackageListFragment;
import org.keynote.godtools.android.http.DownloadTask;
import org.keynote.godtools.android.http.DraftCreationTask;
import org.keynote.godtools.android.http.DraftPublishTask;
import org.keynote.godtools.android.http.GodToolsApiClient;
import org.keynote.godtools.android.http.MetaTask;
import org.keynote.godtools.android.snuffy.SnuffyApplication;
import org.keynote.godtools.android.utils.Device;
import org.keynote.godtools.android.expandableList.ExpandableListAdapter;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;


public class PreviewModeMainPW extends BaseActionBarActivity implements PackageListFragment.OnPackageSelectedListener,
        DownloadTask.DownloadTaskHandler,
        MetaTask.MetaTaskHandler, View.OnClickListener
{
    private static final String TAG = "PreviewModeMainPW";
    private static final int REQUEST_SETTINGS = 1001;
    private static final String JUST_SWITCHED = "justSwitched";

    public static final int REFERENCE_DEVICE_HEIGHT = 960;    // pixels on iPhone w/retina - including title bar
    public static final int REFERENCE_DEVICE_WIDTH = 640;    // pixels on iPhone w/retina - full width

    private int mPageLeft;
    private int mPageTop;
    private int mPageWidth;
    private int mPageHeight;
    private String languagePrimary;
    private List<GTPackage> packageList;

    View vLoading;
    TextView tvTask;
    ImageButton refreshButton;
    Context context;
    Timer timer;
    /**
     * When clicked, dialog to launch a new translation is opened
     */
    boolean isDownloading;
    boolean noPackages = false;
    boolean justSwitchedToTranslatorMode;
    SharedPreferences settings;
    
    ExpandableListAdapter listAdapter;
    ExpandableListView listView;
    List<String> listDataHeader;
    List<String> childList;
    HashMap<String, List<String>> listDataChild;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.preview_mode_main_pw);

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(R.layout.titlebar_centered_title);
        TextView titleBar = (TextView) actionBar.getCustomView().findViewById(R.id.titlebar_title);
        titleBar.setText(R.string.preview_mode_title);

        context = getApplicationContext();
        
        // setupLayout();

        settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        languagePrimary = settings.getString(GTLanguage.KEY_PRIMARY, "en");
        justSwitchedToTranslatorMode = settings.getBoolean(JUST_SWITCHED, false);

        packageList = getPackageList(); // get the packages for the primary language
        
        listView = (ExpandableListView) findViewById(R.id.expandable_list);
        listAdapter = new ExpandableListAdapter(this, packageList);
        listView.setAdapter(listAdapter);
        listView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener()
        {
            int lastExpandedPosition = -1;

            @Override
            public void onGroupExpand(int groupPosition)
            {
                if (groupPosition != lastExpandedPosition)
                    listView.collapseGroup(lastExpandedPosition);
                lastExpandedPosition = groupPosition;
            }
        });
        
        listView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener()
        {
            @Override
            public boolean onGroupClick(ExpandableListView expandableListView, View view, int i, long l)
            {
                return false;
            }
        });
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

                Toast.makeText(PreviewModeMainPW.this, "Translator preview mode is enabled", Toast.LENGTH_LONG).show();
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

                Toast.makeText(PreviewModeMainPW.this, "Translator preview mode is disabled", Toast.LENGTH_LONG).show();

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

        double aspectRatioTarget = (double) PreviewModeMainPW.REFERENCE_DEVICE_WIDTH / (double) PreviewModeMainPW.REFERENCE_DEVICE_HEIGHT;
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

        // If no packages are available for a language, then fallback to English
        if (justSwitchedToTranslatorMode)
        {
            /*
             * When switching to translator mode, the MainPW activity is restarted. However, the packageList and
             * packageFrag need to be refreshed based on the newly downloaded items. The justSwitchedToTranslatorMode is
             * saved in the settings and when true, this will refresh the packages available.
             */
            packageList = getPackageList();
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
                packageList = getPackageList();
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
            else
            {
            }
            createTheHomeScreen();
        }
        else if (tag.equalsIgnoreCase("draft"))
        {
            Toast.makeText(PreviewModeMainPW.this, "Drafts have been updated", Toast.LENGTH_SHORT).show();
            packageList = getPackageList();
            createTheHomeScreen();
        }
        else if (tag.equalsIgnoreCase("draft_primary"))
        {
            languagePrimary = langCode;
            packageList = getPackageList();

            createTheHomeScreen();
        }
        else if (tag.equalsIgnoreCase("draft_parallel"))
        {
            createTheHomeScreen();
        }
    }

    private List<GTPackage> getPackageList()
    {
        // only return draft packages with translator mode
        List<GTPackage> packageByLanguage = GTPackage.getDraftPackages(PreviewModeMainPW.this, languagePrimary);
        if("en".equals(languagePrimary))
        {
            removeEveryStudent(packageByLanguage);
        }
        return packageByLanguage;
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
                                        GodToolsApiClient.getListOfDrafts(settings.getString("Authorization_Draft", ""), languagePrimary, "draft_primary", PreviewModeMainPW.this);
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
                        Intent intent = new Intent(PreviewModeMainPW.this, SnuffyPWActivity.class);
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
        }

        Toast.makeText(PreviewModeMainPW.this, "Failed to update drafts", Toast.LENGTH_SHORT).show();


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

            packageList = getPackageList();
            Toast.makeText(PreviewModeMainPW.this, "Failed to download drafts", Toast.LENGTH_SHORT).show();

        }
        else if (tag.equalsIgnoreCase("draft_parallel"))
        {

            // do nothing

        }
        else if (tag.equalsIgnoreCase("primary") || tag.equalsIgnoreCase("parallel"))
        {

            Toast.makeText(PreviewModeMainPW.this, "Failed to download resources", Toast.LENGTH_SHORT).show();

        }

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

    private void onCmd_refresh()
    {
        if (Device.isConnected(PreviewModeMainPW.this))
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
            Toast.makeText(PreviewModeMainPW.this, "Internet connection is required", Toast.LENGTH_SHORT).show();
        }
    }

    private void onCmd_add()
    {
        if (Device.isConnected(PreviewModeMainPW.this))
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
                                    GodToolsApiClient.getListOfDrafts(settings.getString("Authorization_Draft", ""), languagePrimary, "draft", PreviewModeMainPW.this);
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
                    if (noPackages) onCmd_settings();
                }
            });

            b.show();
        }
        else
        {
            Toast.makeText(PreviewModeMainPW.this, "Internet connection is required", Toast.LENGTH_SHORT).show();
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