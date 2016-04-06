package org.keynote.godtools.android;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
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
import android.widget.TextView;
import android.widget.Toast;

import org.ccci.gto.android.common.db.Query;
import org.keynote.godtools.android.broadcast.BroadcastUtil;
import org.keynote.godtools.android.broadcast.Type;
import org.keynote.godtools.android.business.GTLanguage;
import org.keynote.godtools.android.business.GTPackage;
import org.keynote.godtools.android.dao.DBAdapter;
import org.keynote.godtools.android.everystudent.EveryStudent;
import org.keynote.godtools.android.expandableList.ExpandableListAdapter;
import org.keynote.godtools.android.fragments.AccessCodeDialogFragment;
import org.keynote.godtools.android.http.DownloadTask;
import org.keynote.godtools.android.http.GodToolsApiClient;
import org.keynote.godtools.android.http.MetaTask;
import org.keynote.godtools.android.service.UpdatePackageListTask;
import org.keynote.godtools.android.snuffy.SnuffyApplication;
import org.keynote.godtools.android.utils.Device;

import java.util.Iterator;
import java.util.List;

import static org.keynote.godtools.android.dao.DBContract.GTPackageTable.SQL_WHERE_DRAFT_BY_LANGUAGE;
import static org.keynote.godtools.android.utils.Constants.APPLICATION_NAME;
import static org.keynote.godtools.android.utils.Constants.AUTH_DRAFT;
import static org.keynote.godtools.android.utils.Constants.ENGLISH_DEFAULT;
import static org.keynote.godtools.android.utils.Constants.FOUR_LAWS;
import static org.keynote.godtools.android.utils.Constants.KEY_DRAFT;
import static org.keynote.godtools.android.utils.Constants.KEY_DRAFT_PRIMARY;
import static org.keynote.godtools.android.utils.Constants.KEY_PARALLEL;
import static org.keynote.godtools.android.utils.Constants.KEY_PRIMARY;
import static org.keynote.godtools.android.utils.Constants.KGP;
import static org.keynote.godtools.android.utils.Constants.PREFS_NAME;
import static org.keynote.godtools.android.utils.Constants.SATISFIED;
import static org.keynote.godtools.android.utils.Constants.WEB_URL;

public class PreviewModeMainPW extends BaseActionBarActivity implements
        DownloadTask.DownloadTaskHandler,
        MetaTask.MetaTaskHandler,
        View.OnClickListener,
        AccessCodeDialogFragment.AccessCodeDialogListener
{
    private static final String TAG = "PreviewModeMainPW";
    private static final int REQUEST_SETTINGS = 1001;

    private static final int REFERENCE_DEVICE_HEIGHT = 960;    // pixels on iPhone w/retina - including title bar
    private static final int REFERENCE_DEVICE_WIDTH = 640;    // pixels on iPhone w/retina - full width

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
                refreshDrafts();
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

        refreshDrafts();
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
                            intent.putExtra("PackageName", gtPackage.getCode());
                            addPageFrameToIntent(intent);
                            startActivity(intent);
                            return true;
                        }
                        else if (gtPackage.isAvailable())
                        {
                            Intent intent = new Intent(context, SnuffyPWActivity.class);
                            intent.putExtra("PackageName", gtPackage.getCode());
                            intent.putExtra("LanguageCode", gtPackage.getLanguage());
                            intent.putExtra("ConfigFileName", gtPackage.getConfigFileName());
                            intent.putExtra("Status", gtPackage.getStatus());
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

                if (BroadcastUtil.ACTION_DRAFT_START.equals(intent.getAction()))
                {
                    showLoading(getString(R.string.connecting));
                }
                else if (BroadcastUtil.ACTION_STOP.equals(intent.getAction()))
                {

                    Type type = (Type) intent.getSerializableExtra(BroadcastUtil.ACTION_TYPE);

                    if (Type.DISABLE_TRANSLATOR.equals(type))
                    {
                        if (pdLoading != null) pdLoading.hide();

                        Toast.makeText(PreviewModeMainPW.this, getString(R.string.translator_disabled),
                                Toast.LENGTH_LONG).show();

                        finish();
                    }
                    else
                    {
                        refreshDrafts();
                    }
                }

                if (BroadcastUtil.ACTION_FAIL.equals(intent.getAction()))
                {
                    if (pdLoading != null) pdLoading.hide();
                }
            }
        };

        broadcastManager.registerReceiver(broadcastReceiver, BroadcastUtil.startDraftFilter());
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

            default:
                refreshDrafts();
            case RESULT_CHANGED_PRIMARY:
            case RESULT_CHANGED_PARALLEL:
            {
                languagePrimary = settings.getString(GTLanguage.KEY_PRIMARY, "");

                getPackageList();

                break;
            }
        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        SharedPreferences.Editor ed = settings.edit();
        ed.apply();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        getPackageList();
        getScreenSize();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        removeBroadcastReceiver();
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
        top = (rect.height() - height) / 2;

        mPageLeft = left;
        mPageTop = top;
        mPageWidth = width;
        mPageHeight = height;
    }

    private void getPackageList()
    {
        boolean kgpPresent = false;
        boolean satisfiedPresent = false;
        boolean fourlawsPresent = false;

        // only return draft packages with translator mode
        final DBAdapter dao = DBAdapter.getInstance(this);
        List<GTPackage> packageByLanguage = dao.get(Query.select(GTPackage.class).where(
                SQL_WHERE_DRAFT_BY_LANGUAGE.args(languagePrimary)));

        if (ENGLISH_DEFAULT.equals(languagePrimary))
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
        for (; i.hasNext(); )
        {
            if (i.next().getCode().equals(GTPackage.EVERYSTUDENT_PACKAGE_CODE)) i.remove();
        }
    }

    @Override
    public void onClick(View view)
    {
        Log.i(TAG, "View clicked");
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

    private void refreshDrafts()
    {
        if (Device.isConnected(PreviewModeMainPW.this))
        {
            swipeRefreshLayout.setRefreshing(true);

            GodToolsApiClient.getListOfDrafts(settings.getString(AUTH_DRAFT, ""),
                    languagePrimary, KEY_DRAFT, this);

            GodToolsApiClient.downloadDrafts(getApp(),
                    settings.getString(AUTH_DRAFT, ""),
                    languagePrimary,
                    KEY_DRAFT,
                    this);
        }
        else
        {
            Toast.makeText(PreviewModeMainPW.this, getString(R.string.internet_needed),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void doCmdShare()
    {
        String messageBody = getString(R.string.share_general_subject);
        messageBody = messageBody.replace(APPLICATION_NAME, getString(R.string.app_name));
        messageBody = messageBody.replace(WEB_URL, "\n"+getString(R.string.app_share_link_base_link));

        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
        share.putExtra(Intent.EXTRA_TEXT, messageBody);
        startActivity(Intent.createChooser(share, getString(R.string.share_prompt)));
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

    @Override
    public void metaTaskComplete(List<GTLanguage> languageList, String tag)
    {
        UpdatePackageListTask.run(languageList, DBAdapter.getInstance(this));
    }

    @Override
    public void metaTaskFailure(List<GTLanguage> languageList, String tag, int statusCode)
    {
        if (401 == statusCode)
        {
            showAccessCodeDialog();
            Toast.makeText(PreviewModeMainPW.this, getString(R.string.expired_passcode), Toast.LENGTH_LONG).show();
        }
        else
        {
            Toast.makeText(PreviewModeMainPW.this, getString(R.string.failed_update_draft), Toast.LENGTH_SHORT).show();
        }

        if (tag.equalsIgnoreCase(KEY_DRAFT) || tag.equalsIgnoreCase(KEY_DRAFT_PRIMARY))
        {
            getPackageList();
        }

        swipeRefreshLayout.setRefreshing(false);
        Log.i(TAG, "Done refreshing");
    }

    @Override
    public void downloadTaskComplete(String url, String filePath, String langCode, String tag)
    {
        if (tag.equalsIgnoreCase(KEY_DRAFT))
        {
            Toast.makeText(PreviewModeMainPW.this, getString(R.string.drafts_updated), Toast.LENGTH_SHORT).show();
            getPackageList();

            Log.i(TAG, "Done refreshing");
        }
        else if (tag.equalsIgnoreCase(KEY_DRAFT_PRIMARY))
        {
            languagePrimary = langCode;
            getPackageList();
        }

        if (pdLoading != null && pdLoading.isShowing())
        {
            pdLoading.hide();
        }

        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void downloadTaskFailure(String url, String filePath, String langCode, String tag)
    {

        if (tag.equalsIgnoreCase(KEY_DRAFT))
        {
            Toast.makeText(PreviewModeMainPW.this, getString(R.string.failed_update_draft),
                    Toast.LENGTH_SHORT).show();
        }
        else if (tag.equalsIgnoreCase(KEY_DRAFT_PRIMARY))
        {
            getPackageList();
            Toast.makeText(PreviewModeMainPW.this, getString(R.string.failed_download_draft),
                    Toast.LENGTH_SHORT).show();
        }
        else if (tag.equalsIgnoreCase(KEY_PRIMARY) || tag.equalsIgnoreCase(KEY_PARALLEL))
        {
            Toast.makeText(PreviewModeMainPW.this, getString(R.string.failed_download_resources),
                    Toast.LENGTH_SHORT).show();
        }

        if (pdLoading != null && pdLoading.isShowing())
        {
            pdLoading.hide();
        }

        swipeRefreshLayout.setRefreshing(false);
    }
}