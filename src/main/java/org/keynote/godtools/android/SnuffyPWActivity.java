package org.keynote.godtools.android;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.annotation.WorkerThread;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.common.collect.ImmutableList;

import org.ccci.gto.android.common.support.v4.adapter.ViewHolderPagerAdapter;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.keynote.godtools.android.business.GSSubscriber;
import org.keynote.godtools.android.business.GTPackage;
import org.keynote.godtools.android.dao.DBAdapter;
import org.keynote.godtools.android.event.GodToolsEvent;
import org.keynote.godtools.android.event.GodToolsEvent.EventID;
import org.keynote.godtools.android.googleAnalytics.EventTracker;
import org.keynote.godtools.android.http.DownloadTask;
import org.keynote.godtools.android.http.GodToolsApiClient;
import org.keynote.godtools.android.http.NotificationUpdateTask;
import org.keynote.godtools.android.notifications.NotificationInfo;
import org.keynote.godtools.android.snuffy.PackageReader;
import org.keynote.godtools.android.snuffy.SnuffyAboutActivity;
import org.keynote.godtools.android.snuffy.SnuffyApplication;
import org.keynote.godtools.android.snuffy.SnuffyHelpActivity;
import org.keynote.godtools.android.snuffy.SnuffyPage;
import org.keynote.godtools.android.sync.GodToolsSyncService;

import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.Bind;
import butterknife.ButterKnife;

import static org.ccci.gto.android.common.support.v4.util.IdUtils.convertId;
import static org.keynote.godtools.android.event.GodToolsEvent.EventID.SUBSCRIBE_EVENT;
import static org.keynote.godtools.android.utils.Constants.AUTH_CODE;
import static org.keynote.godtools.android.utils.Constants.AUTH_DRAFT;
import static org.keynote.godtools.android.utils.Constants.COUNT;
import static org.keynote.godtools.android.utils.Constants.ENGLISH_DEFAULT;
import static org.keynote.godtools.android.utils.Constants.FOUR_LAWS;
import static org.keynote.godtools.android.utils.Constants.KEY_DRAFT;
import static org.keynote.godtools.android.utils.Constants.KGP;
import static org.keynote.godtools.android.utils.Constants.LANGUAGE_PARALLEL;
import static org.keynote.godtools.android.utils.Constants.PREFS_NAME;
import static org.keynote.godtools.android.utils.Constants.PROPERTY_REG_ID;
import static org.keynote.godtools.android.utils.Constants.SATISFIED;
import static org.keynote.godtools.android.utils.Constants.SHARE_LINK;
import static org.keynote.godtools.android.utils.Constants.TRANSLATOR_MODE;

@SuppressWarnings("deprecation")
public class SnuffyPWActivity extends AppCompatActivity
{
    private static final String TAG = "SnuffyActivity";

    private String mAppPackage;
    private String mConfigFileName;
    private String mAppLanguage = ENGLISH_DEFAULT;
    @Bind(R.id.snuffyViewPager)
    ViewPager mPager;
    private int mPagerCurrentItem = 0;
    private GtPagesPagerAdapter mPagerAdapter;
    private boolean mSetupRequired = true;
    private String mPackageTitle;
    private String mPackageStatus;
    private ProcessPackageAsync mProcessPackageAsync;
    private GestureDetector MyGestureDetector;

    private String mConfigPrimary, mConfigParallel;
    private GTPackage mParallelPackage;
    private boolean isUsingPrimaryLanguage, isParallelLanguageSet;
    
    private SharedPreferences settings;
    private String regid;
    private Timer timer;

    @Nullable
    private List<SnuffyPage> mPages;
    @Nullable
    private SnuffyPage mAboutView;

    private void setLanguage(String languageCode)
    {
        mAppLanguage = languageCode;
    }

    private String getLanguage()
    {
        return mAppLanguage;
    }

    /* BEGIN lifecycle */

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.snuffy_main);
        ButterKnife.bind(this);
        setupViewPager();

        Log.i("Activity", "SnuffyPWActivity");

        mAppPackage = getIntent().getStringExtra("PackageName");        // "kgp"
        mAppLanguage = getIntent().getStringExtra("LanguageCode");      // "en"
        mConfigFileName = getIntent().getStringExtra("ConfigFileName");
        mPackageStatus = getIntent().getStringExtra("Status"); // live = draft
        getIntent().putExtra("AllowFlip", false);

        trackScreenActivity(mAppPackage + "-0");

        mConfigPrimary = mConfigFileName;
        isUsingPrimaryLanguage = true;

        // check if parallel language is set
        settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String langParallel = settings.getString(LANGUAGE_PARALLEL, "");


        // get package if parallel language is set
        if (!langParallel.isEmpty())
        {
            isParallelLanguageSet = true;
            //noinspection WrongThread
            mParallelPackage =
                    DBAdapter.getInstance(this).find(GTPackage.class, langParallel, GTPackage.STATUS_LIVE, mAppPackage);
        }

        if (mParallelPackage != null)
        {
            mConfigParallel = mParallelPackage.getConfigFileName();
        }

        //doSetup(100); // used to be 1 second delay required to make sure activity fully created
        // - is there something we can test for that is better than a fixed timeout?
        // We reduce this now to 100 msec since we are not measuring the device size here
        // since that is done in GodTools which calls us and passes the dimensions in.
        
        regid = settings.getString(PROPERTY_REG_ID, "");
        
        if (mAppPackage.equalsIgnoreCase(KGP) || mAppPackage.equalsIgnoreCase(FOUR_LAWS))
        {
            startTimer();

            GodToolsApiClient.updateNotification(settings.getString(AUTH_CODE, ""),
                    regid, NotificationInfo.AFTER_10_PRESENTATIONS, new NotificationUpdateTask.NotificationUpdateTaskHandler()
                    {
                        @Override
                        public void registrationComplete(String regId)
                        {
                            Log.i(NotificationInfo.NOTIFICATION_TAG, "10 Presentation Notification notice sent to API");
                        }

                        @Override
                        public void registrationFailed()
                        {
                            Log.e(NotificationInfo.NOTIFICATION_TAG, "10 Presentation notification notice failed to send to API");
                        }
                    });


        }
    }

    /**
     * Event triggered whenever a new set of pages is loaded.
     *
     * @param pages the Pages that were just loaded.
     */
    void onPagesLoaded(@Nullable final List<SnuffyPage> pages) {
        updateDisplayedPages(pages);
    }

    @Subscribe
    public boolean onGodToolsEvent(@NonNull final GodToolsEvent event) {
        if (event.getEventID().equals(SUBSCRIBE_EVENT)) {
            return triggerSubscribeEvent(event);
        } else {
            return triggerLocalPageNavigation(event.getEventID());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cleanupViewPager();
        ButterKnife.unbind(this);
    }

    /* END lifecycle */

    private void setupViewPager() {
        if (mPager != null) {
            mPagerAdapter = new GtPagesPagerAdapter();
            mPager.setAdapter(mPagerAdapter);

            // configure page change listener
            mPager.addOnPageChangeListener(new SimpleOnPageChangeListener() {
                @Override
                public void onPageSelected(int position) {
                    Log.d(TAG, "onPageSelected: " + mAppPackage + Integer.toString(position));
                    trackScreenActivity(mAppPackage + "-" + Integer.toString(position));

                    // exit previously active page
                    if (mPages != null) {
                        mPages.get(mPagerCurrentItem).onExitPage();
                    }

                    // keep our own since ViewPager doesn't offer a getCurrentItem method!
                    mPagerCurrentItem = position;

                    // enter currently active page
                    if (mPages != null) {
                        mPages.get(mPagerCurrentItem).onEnterPage();
                    }

                    // This notification has been updated to only be sent after the app has been opened 3 times
                    // The api will only send a notice once, so it can be sent from here multiple times.

                    // if the prayer pages are ever moved this will need to be updated.

                    if (settings.getInt(COUNT, 0) >= 3) {
                        if ((mAppPackage.equalsIgnoreCase(KGP) && position == 7) ||
                                (mAppPackage.equalsIgnoreCase(FOUR_LAWS) && position == 6)) {
                            Log.i(TAG, "App used 3 times and prayer page reached.");
                            GodToolsApiClient.updateNotification(
                                    settings.getString(AUTH_CODE, ""), regid, NotificationInfo.AFTER_1_PRESENTATION,
                                    new NotificationUpdateTask.NotificationUpdateTaskHandler() {
                                        @Override
                                        public void registrationComplete(String regId) {
                                            Log.i(NotificationInfo.NOTIFICATION_TAG,
                                                  "1 Presentation Notification notice sent to API");
                                        }

                                        @Override
                                        public void registrationFailed() {
                                            Log.e(NotificationInfo.NOTIFICATION_TAG,
                                                  "1 Presentation notification notice failed to send to API");
                                        }
                                    });
                        }
                    }
                }
            });
        }

        // trigger an initial update
        updateViewPager();
    }

    private void updateViewPager() {
        if (mPagerAdapter != null) {
            mPagerAdapter.setPages(mPages);
        }
    }

    private void cleanupViewPager() {
        if (mPager != null) {
            mPager.setAdapter(null);
            mPagerAdapter = null;
        }
    }

    private void updateAppPages() {
        final SnuffyApplication app = getApp();
        app.packageTitle = mPackageTitle;
        app.aboutView = mAboutView;
        app.setSnuffyPages(mPages);
    }

    /**
     * This method is responsible for updating the list of pages being displayed and used by the app
     *
     * @param pages the new set of Pages to display
     */
    void updateDisplayedPages(@Nullable final List<SnuffyPage> pages) {
        // replace the about page
        mAboutView = null;
        if (pages != null && pages.size() > 0) {
            mAboutView = pages.get(0);
            pages.remove(mAboutView);
        }

        // store the remaining pages as the actual pages
        mPages = pages;

        // trigger updates on various components
        updateViewPager();
        updateAppPages();
    }

    private boolean triggerLocalPageNavigation(@NonNull final EventID event) {
        if (mPages != null) {
            for (int x = 0; x < mPages.size(); x++) {
                if (mPages.get(x).getModel().getListeners().contains(event)) {
                    mPager.setCurrentItem(x);
                    return true;
                }
            }
        }

        return false;
    }

    private boolean triggerSubscribeEvent(@NonNull final GodToolsEvent event) {
        addGSSubscriberToDB(event);
        GodToolsSyncService.syncGrowthSpacesSubscribers(this);
        return true;
    }

    protected void onResume()
    {
        super.onResume();

        if (mSetupRequired)
        {
            doSetup(100); // used to be 1 second delay required to make sure activity fully created
            // - is there something we can test for that is better than a fixed timeout?
            // We reduce this now to 100 msec since we are not measuring the device size here
            // since that is done in GodTools which calls us and passes the dimensions in.
            mSetupRequired = false;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        Log.i(TAG, "Activity stopped");

        if (timer != null)
        {
            timer.cancel();
            Log.i(NotificationInfo.NOTIFICATION_TAG, "Share Timer stopped");
        }

        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    private void addGSSubscriberToDB(GodToolsEvent event) {
        GSSubscriber gsSubscriber = new GSSubscriber();
        gsSubscriber.setRouteId(event.getData().get("routeId"));
        gsSubscriber.setLanguageCode(event.getData().get("languageCode"));
        gsSubscriber.setFirstName(event.getData().get("firstName"));
        gsSubscriber.setLastName(event.getData().get("lastName"));
        gsSubscriber.setEmail(event.getData().get("email"));

        if(gsSubscriber.isValid()) {
            final DBAdapter dao = DBAdapter.getInstance(this);
            dao.insertAsync(gsSubscriber);
        }
        else {
            Log.e(TAG, "Growth Spaces Subscriber must have route id, language code, and email set.");
        }
    }

    private void doSetup(int delay)
    {
        new Handler().postDelayed(new Runnable()
        {
            public void run()
            {
                // trigger the actual load of pages
                mProcessPackageAsync = new ProcessPackageAsync(mPager.getMeasuredWidth(), mPager.getMeasuredHeight());
                mProcessPackageAsync.execute("");
            }
        }, delay);  // delay can be required to make sure activity fully created - is there something we can test for that is better than a fixed timeout?

    }

    private void completeSetup(boolean bSuccess)
    {
        if (!bSuccess)
        { // now testing is done - only show msg on failure
            Toast.makeText(SnuffyPWActivity.this.getApplicationContext(),
                    getString(R.string.processing_failed),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        addClickHandlersToAllPages();
        addCallingActivityToAllPages();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        SharedPreferences.Editor ed = settings.edit();
        ed.putInt("currPage", mPagerCurrentItem);
        ed.apply();
    }

    private void addClickHandlersToAllPages()
    {
        Iterator<SnuffyPage> iter = mPages.iterator();

        MyGestureDetector = new GestureDetector(new MyGestureListener());


        while (iter.hasNext())
        {
            iter.next().setOnTouchListener(new View.OnTouchListener()
            {
                @Override
                public boolean onTouch(View v, MotionEvent event)
                {
                    MyGestureDetector.onTouchEvent(event);
                    return true;
                }
            });
        }
    }

    private void addCallingActivityToAllPages()
    {

        for (SnuffyPage mPage : mPages)
        {
            mPage.mCallingActivity = this; // the SnuffyActivity owns most pages except the about page - which will be set explicitly
        }
    }

    private void doCmdFlip()
    {
        // Note: We have disabled this menu item if this package does not have both
        // of these language codes defined or curr language is not one of them.
        getIntent().putExtra("AllowFlip", true); // allow called intent to show the flip command

        if (mAppLanguage.equalsIgnoreCase("en_heartbeat"))
            switchLanguages("et_heartbeat", false);
        else if (mAppLanguage.equalsIgnoreCase("et_heartbeat"))
            switchLanguages("en_heartbeat", false);
        // no other flip actions defined
    }

    private void doCmdHelp()
    {
        Intent intent = new Intent(this, SnuffyHelpActivity.class);
        intent.putExtra("PackageTitle", mPackageTitle);
        startActivity(intent);
    }

    private void doCmdShare(View v)
    {
        String messageBody = buildMessageBody();

        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
        share.putExtra(Intent.EXTRA_TEXT, messageBody);
        startActivity(Intent.createChooser(share, getString(R.string.share_prompt)));
    }

    private String buildMessageBody()
    {
        String messageBody = "";

        // http://www.knowgod.com/en
        String shareLink = getString(R.string.app_share_link_base_link) + "/" + mAppLanguage;

        if (KGP.equalsIgnoreCase(mAppPackage) || FOUR_LAWS.equalsIgnoreCase(mAppPackage))
        {
            // http://www.knowgod.com/en/kgp
            shareLink = shareLink + "/" + mAppPackage;

            messageBody = getString(R.string.share_from_page_message);
        }
        else if (SATISFIED.equalsIgnoreCase(mAppPackage))
        {
            messageBody = getString(R.string.satisfied_share);
        }

        final int currItem = mPager.getCurrentItem();
        if (currItem > 0) {
            // http://www.knowgod.com/en/kgp/5
            shareLink = shareLink + "/" + String.valueOf(currItem);
        }

        messageBody = messageBody.replace(SHARE_LINK, shareLink);

        return messageBody;
    }

    private void doCmdShowPageMenu(View v)
    {
        // store pages before triggering Activity launch
        Crashlytics.log("Storing SnuffyPages in Application before launching SnuffyPageMenuPWActivity: " +
                                (mPages != null ? mPages.size() : "null"));
        getApp().setSnuffyPages(mPages);

        Intent intent = new Intent(this, SnuffyPageMenuPWActivity.class);
        intent.putExtra("LanguageCode", mAppLanguage);
        intent.putExtra("PackageName", mAppPackage);
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (resultCode == RESULT_CANCELED)
            return;

        switch (requestCode)
        {
            case 0: // Show Page Menu
            {
                mPager.setCurrentItem(resultCode - RESULT_FIRST_USER);
                break;
            }
        }
    }

    private void switchLanguages(String languageCode, boolean bResetToFirstPage)
    {
        setLanguage(languageCode);

        if (bResetToFirstPage)
            mPagerCurrentItem = 0;

        doSetup(1000); // delay required to allow Pager to show the empty set of pages
    }

    private void switchLanguage()
    {
        if (isParallelLanguageSet && mParallelPackage != null)
        {
            if (isUsingPrimaryLanguage)
            {
                mConfigFileName = mConfigParallel;
                isUsingPrimaryLanguage = false;

            }
            else
            {
                mConfigFileName = mConfigPrimary;
                isUsingPrimaryLanguage = true;
            }

            doSetup(0);
        }
        else
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.alternate_language)
                    .setMessage(R.string.alternate_language_message)
                    .setNeutralButton(R.string.ok, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i)
                        {
                            dialogInterface.dismiss();
                        }
                    })
                    .create();
            builder.show();
        }

    }

    private void doCmdInfo(View v)
    {
        Intent intent = new Intent(this, SnuffyAboutActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu_options, menu);

        // enable this feature just for one specific situation
        // matches corresponding items in doCmdFlip() below
        MenuItem flipItem = menu.findItem(R.id.CMD_FLIP);
        if (mAppPackage.equalsIgnoreCase(KGP)
                && (mAppLanguage.equalsIgnoreCase("en_heartbeat") || mAppLanguage.equalsIgnoreCase("et_heartbeat")))
            flipItem.setVisible(true);
        else
            flipItem.setVisible(false);

        // enable this feature if the the parallel language is set
        // and a translation is available for this package
        MenuItem switchItem = menu.findItem(R.id.CMD_SWITCH_LANGUAGE);
        switchItem.setVisible(true);

        if (KEY_DRAFT.equalsIgnoreCase(mPackageStatus) && settings.getBoolean(TRANSLATOR_MODE, false))
        {
            menu.findItem(R.id.CMD_REFRESH_PAGE).setVisible(true);
        }
        else
        {
            menu.findItem(R.id.CMD_REFRESH_PAGE).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (super.onOptionsItemSelected(item))
            return true;
        switch (item.getItemId())
        {
            case R.id.CMD_ABOUT:
            {
                trackScreenEvent("About");
                doCmdInfo(null);
                break;
            }
            case R.id.CMD_CONTENT:
            {
                trackScreenEvent("Content");
                doCmdShowPageMenu(null);
                break;
            }
            case R.id.CMD_EMAIL:
            {
                trackScreenEvent("Share");
                doCmdShare(null);
                break;
            }
            case R.id.CMD_HELP:
            {
                trackScreenEvent("Help");
                doCmdHelp();
                break;
            }
            case R.id.CMD_FLIP:
            {
                trackScreenEvent("Flip");
                doCmdFlip();
                break;
            }
            case R.id.CMD_SWITCH_LANGUAGE:
            {
                trackScreenEvent("Switch Language");
                switchLanguage();
                break;
            }
            case R.id.CMD_REFRESH_PAGE:
            {
                refreshPage();
                break;
            }
            default:
                break;
        }
        return true;
    }

    private void refreshPage()
    {
        final SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SnuffyPage currentPage = mPagerAdapter.getPrimaryItem().mPage;

        showLoading(getString(R.string.update_page));

        GodToolsApiClient.downloadDraftPage((SnuffyApplication) getApplication(),
                settings.getString(AUTH_DRAFT, ""),
                mAppLanguage,
                mAppPackage,
                currentPage.getPageId(),
                new DownloadTask.DownloadTaskHandler()
                {
                    @Override
                    public void downloadTaskComplete(String url, String filePath, String langCode, String tag) {
                        List<SnuffyPage> result = mProcessPackageAsync.doInBackground();
                        mProcessPackageAsync.onPostExecute(result);
                        Toast.makeText(getApplicationContext(), getString(R.string.success), Toast.LENGTH_SHORT).show();
                        hideLoading();
                    }

                    @Override
                    public void downloadTaskFailure(String url, String filePath, String langCode, String tag)
                    {
                        Toast.makeText(getApplicationContext(), getString(R.string.page_error), Toast.LENGTH_SHORT).show();
                        hideLoading();
                    }
                });
    }

    private ProgressDialog mProgressDialog;
    private static final int DIALOG_PROCESS_PACKAGE_PROGRESS = 1;

    @Override
    protected Dialog onCreateDialog(int id)
    {
        switch (id)
        {
            case DIALOG_PROCESS_PACKAGE_PROGRESS:
                mProgressDialog = new ProgressDialog(this);
                mProgressDialog.setMessage(getApplicationContext().getString(R.string.processing_package));
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialog.setCancelable(false);
                mProgressDialog.setProgress(0);
                mProgressDialog.setMax(1); //harmless values to start with to avoid seeing "Nan"
                mProgressDialog.show();
                return mProgressDialog;
            default:
                return null;
        }
    }

    private class ProcessPackageAsync extends AsyncTask<String, Integer, List<SnuffyPage>>
            implements PackageReader.ProgressCallback {
        private final int mPageWidth;
        private final int mPageHeight;

        public ProcessPackageAsync(int width, int height) {
            mPageWidth = width;
            mPageHeight = height;
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            if (mProgressDialog != null)
            {
                mProgressDialog.setProgress(0);
                mProgressDialog.setMax(1);
            }
            showDialog(DIALOG_PROCESS_PACKAGE_PROGRESS);
        }

        @Override
        @WorkerThread
        protected List<SnuffyPage> doInBackground(String... params) {
            // params are not used
            List<SnuffyPage> pages = null;
            PackageReader packageReader = new PackageReader();
            try {
                pages = packageReader.processPackagePW(
                        (SnuffyApplication) getApplication(),
                        mPageWidth, mPageHeight,
                        mConfigFileName, mPackageStatus,
                        ProcessPackageAsync.this,
                        mAppPackage,
                        // send along the language we are loading. if there is a parallel language configured and we
                        // are not showing the primary language, send the parallel language. Otherwise send the primary
                        // language
                        !isUsingPrimaryLanguage && mParallelPackage != null ? mParallelPackage.getLanguage() : mAppLanguage
                );
            }
            catch (Exception e)
            {
                Log.e(TAG, "processPackage failed: " + e.toString());
                Crashlytics.logException(e);
            }
            if (pages != null)
            {
                mPackageTitle = packageReader.getPackageTitle();
            }
            return pages;
        }

        public void updateProgress(int curr, int max)
        {
            onProgressUpdate(curr, max);
        }

        @Override
        protected void onProgressUpdate(Integer... progress)
        {
            mProgressDialog.setMax(progress[1]);
            mProgressDialog.setProgress(progress[0]);
        }

        @Override
        @UiThread
        protected void onPostExecute(List<SnuffyPage> result) {
            if(mProgressDialog != null &&
                    mProgressDialog.isShowing())
            {
                dismissDialog(DIALOG_PROCESS_PACKAGE_PROGRESS);
            }

            onPagesLoaded(result);
            completeSetup(result != null);
        }
    }

    private class MyGestureListener extends GestureDetector.SimpleOnGestureListener
    {
        @Override
        public boolean onSingleTapUp(MotionEvent e)
        {
            openOptionsMenu();
            return super.onSingleTapUp(e);
        }
    }

    private void showLoading(String msg)
    {
        RelativeLayout updatingDraftLayout = (RelativeLayout) findViewById(R.id.updatingDraft);
        updatingDraftLayout.setVisibility(View.VISIBLE);

        TextView updatingPage = (TextView) findViewById(R.id.updatingPageTextView);
        updatingPage.setText(msg);
    }

    private void hideLoading()
    {
        TextView updatingPage = (TextView) findViewById(R.id.updatingPageTextView);
        updatingPage.setText("");

        RelativeLayout updatingDraftLayout = (RelativeLayout) findViewById(R.id.updatingDraft);
        updatingDraftLayout.setVisibility(View.GONE);

    }

    private SnuffyApplication getApp()
    {
        return (SnuffyApplication) getApplication();
    }

    private void trackScreenEvent(String event)
    {
        EventTracker.track(getApp(), mAppPackage, "Menu Event", event);
    }

    private void trackScreenActivity(String activity)
    {
        EventTracker.track(getApp(), activity, mAppLanguage);
    }

    private void startTimer()
    {
        TimerTask timerTask = new TimerTask()
        {
            @Override
            public void run()
            {
                Log.i(TAG, "Timer complete");
                GodToolsApiClient.updateNotification(settings.getString(AUTH_CODE, ""),
                        regid, NotificationInfo.DAY_AFTER_SHARE, new NotificationUpdateTask.NotificationUpdateTaskHandler()
                        {
                            @Override
                            public void registrationComplete(String regId)
                            {
                                Log.i(NotificationInfo.NOTIFICATION_TAG, "Day After Share Notification notice sent to API");
                            }

                            @Override
                            public void registrationFailed()
                            {
                                Log.e(NotificationInfo.NOTIFICATION_TAG, "Day After Share notification notice failed to send to API");
                            }
                        });
            }
        };

        timer = new Timer("1.5ShareTimer");
        timer.schedule(timerTask, 90000); //1.5 minutes
        Log.i(TAG, "Timer scheduled");
    }

    static class GtPagesPagerAdapter extends ViewHolderPagerAdapter<GtPagesPagerAdapter.ViewHolder> {
        @NonNull
        private List<SnuffyPage> mPages = ImmutableList.of();

        public GtPagesPagerAdapter() {
            setHasStableIds(true);
        }

        public void setPages(@Nullable final List<SnuffyPage> pages) {
            mPages = pages != null ? ImmutableList.copyOf(pages) : ImmutableList.<SnuffyPage>of();
            notifyDataSetChanged();
        }

        @Override
        public long getItemId(final int position) {
            return convertId(mPages.get(position).getModel().getId());
        }

        @Override
        protected int getItemPositionFromId(final long id) {
            for (int i = 0; i < mPages.size(); i++) {
                if (convertId(mPages.get(i).getModel().getId()) == id) {
                    return i;
                }
            }
            return POSITION_NONE;
        }

        @Override
        public int getCount() {
            return mPages.size();
        }

        @NonNull
        @Override
        protected ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent) {
            return new ViewHolder(LayoutInflater.from(parent.getContext())
                                          .inflate(R.layout.page_gt_page_frame, parent, false));
        }

        @Override
        protected void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
            super.onBindViewHolder(holder, position);
            holder.mPage = mPages.get(position);

            if (holder.mContentContainer != null) {
                // remove any previous page from the content container
                holder.mContentContainer.removeAllViews();

                // attach the current page to the content container
                holder.mContentContainer.addView(holder.mPage);
            }
        }

        @Override
        protected void onViewRecycled(@NonNull final ViewHolder holder) {
            super.onViewRecycled(holder);
            holder.mPage = null;
            if (holder.mContentContainer != null) {
                holder.mContentContainer.removeAllViews();
            }
        }

        static final class ViewHolder extends ViewHolderPagerAdapter.ViewHolder {
            @Nullable
            SnuffyPage mPage;

            @Nullable
            @Bind(R.id.pageContainer)
            FrameLayout mContentContainer;

            ViewHolder(@NonNull View view) {
                super(view);
                ButterKnife.bind(this, view);
            }
        }
    }
}
