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
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewConfigurationCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;

import com.google.common.base.Strings;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.keynote.godtools.android.business.GSSubscriber;
import org.keynote.godtools.android.business.GTPackage;
import org.keynote.godtools.android.dao.DBAdapter;
import org.keynote.godtools.android.event.GodToolsEvent;
import org.keynote.godtools.android.event.GodToolsEvent.EventID;
import org.keynote.godtools.android.googleAnalytics.EventTracker;
import org.keynote.godtools.android.http.GodToolsApiClient;
import org.keynote.godtools.android.http.NotificationUpdateTask;
import org.keynote.godtools.android.model.Followup;
import org.keynote.godtools.android.notifications.NotificationInfo;
import org.keynote.godtools.android.snuffy.SnuffyAboutActivity;
import org.keynote.godtools.android.snuffy.SnuffyHelpActivity;
import org.keynote.godtools.android.snuffy.model.GtFollowupModal;
import org.keynote.godtools.android.snuffy.newnew.ProgressCallback;
import org.keynote.godtools.android.support.v4.fragment.GtFollowupModalDialogFragment;
import org.keynote.godtools.android.sync.GodToolsSyncService;
import org.keynote.godtools.android.utils.FileUtils;
import org.keynote.godtools.renderer.crureader.XMLUtil;
import org.keynote.godtools.renderer.crureader.bo.GDocument.GDocument;
import org.keynote.godtools.renderer.crureader.bo.GDocument.GDocumentPage;
import org.keynote.godtools.renderer.crureader.bo.GPage.GPage;
import org.keynote.godtools.renderer.crureader.bo.GPage.RenderHelpers.Diagnostics;
import org.keynote.godtools.renderer.crureader.bo.GPage.RenderHelpers.RenderSingleton;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.support.v4.app.FragmentManager.POP_BACK_STACK_INCLUSIVE;
import static org.keynote.godtools.android.event.GodToolsEvent.EventID.SUBSCRIBE_EVENT;
import static org.keynote.godtools.android.snuffy.model.GtInputField.FIELD_EMAIL;
import static org.keynote.godtools.android.snuffy.model.GtInputField.FIELD_FIRST_NAME;
import static org.keynote.godtools.android.snuffy.model.GtInputField.FIELD_LAST_NAME;
import static org.keynote.godtools.android.snuffy.model.GtInputField.FIELD_NAME;
import static org.keynote.godtools.android.utils.Constants.AUTH_CODE;
import static org.keynote.godtools.android.utils.Constants.ENGLISH_DEFAULT;
import static org.keynote.godtools.android.utils.Constants.FOUR_LAWS;
import static org.keynote.godtools.android.utils.Constants.KEY_DRAFT;
import static org.keynote.godtools.android.utils.Constants.KGP;
import static org.keynote.godtools.android.utils.Constants.LANGUAGE_PARALLEL;
import static org.keynote.godtools.android.utils.Constants.PREFS_NAME;
import static org.keynote.godtools.android.utils.Constants.PROPERTY_REG_ID;
import static org.keynote.godtools.android.utils.Constants.SATISFIED;
import static org.keynote.godtools.android.utils.Constants.TRANSLATOR_MODE;

@SuppressWarnings("deprecation")
public class SnuffyPWActivity extends AppCompatActivity {
    private static final String TAG = "SnuffyActivity";
    private static final String TAG_FOLLOWUP_MODAL = "followupModal";
    private static final int DIALOG_PROCESS_PACKAGE_PROGRESS = 1;

    @BindView(R.id.snuffyRecyclerView)
    public RecyclerView snuffyRecyclerView;
    GtPagesPagerAdapter mPagerAdapter;
    @Nullable
    String mCurrentPageId;
    private String mAppPackage;
    private String mConfigFileName;
    private String mAppLanguage = ENGLISH_DEFAULT;
    private boolean mSetupRequired = true;
    private String mPackageStatus;
    private ProcessPackageAsync mProcessPackageAsync;
    private String mConfigPrimary, mConfigParallel;
    private GTPackage mParallelPackage;
    private boolean isUsingPrimaryLanguage, isParallelLanguageSet;
    private SharedPreferences settings;
    private String regid;
    private Timer timer;
    /* BEGIN lifecycle */
    private ProgressDialog mProgressDialog;

    private String getLanguage() {
        return mAppLanguage;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.snuffy_main);
        ButterKnife.bind(this);

        setupActionBar();
        mAppPackage = getIntent().getStringExtra("PackageName");        // "kgp"
        mAppLanguage = getIntent().getStringExtra("LanguageCode");      // "en"
        mConfigFileName = getIntent().getStringExtra("ConfigFileName");
        mPackageStatus = getIntent().getStringExtra("Status"); // live = draft
        getIntent().putExtra("AllowFlip", false);

        mConfigPrimary = mConfigFileName;
        isUsingPrimaryLanguage = true;

        // check if parallel language is set
        settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String langParallel = settings.getString(LANGUAGE_PARALLEL, "");

        Diagnostics.StartMethodTracingByKey("langPara");
        // get package if parallel language is set

        if (!langParallel.isEmpty()) {
            isParallelLanguageSet = true;
            //noinspection WrongThread
            mParallelPackage =
                    DBAdapter.getInstance(this).find(GTPackage.class, langParallel, GTPackage.STATUS_LIVE, mAppPackage);
        }
        Diagnostics.StopMethodTracingByKey("langPara");

        if (mParallelPackage != null) {
            mConfigParallel = mParallelPackage.getConfigFileName();
        }

        //doSetup(100); // used to be 1 second delay required to make sure activity fully created
        // - is there something we can test for that is better than a fixed timeout?
        // We reduce this now to 100 msec since we are not measuring the device size here
        // since that is done in GodTools which calls us and passes the dimensions in.

        regid = settings.getString(PROPERTY_REG_ID, "");

        if (mAppPackage.equalsIgnoreCase(KGP) || mAppPackage.equalsIgnoreCase(FOUR_LAWS)) {
            startTimer();

            GodToolsApiClient.updateNotification(settings.getString(AUTH_CODE, ""),
                    regid, NotificationInfo.AFTER_10_PRESENTATIONS, new NotificationUpdateTask.NotificationUpdateTaskHandler() {
                        @Override
                        public void registrationComplete(String regId) {
                            Log.i(NotificationInfo.NOTIFICATION_TAG, "10 Presentation Notification notice sent to API");
                        }

                        @Override
                        public void registrationFailed() {
                            Log.e(NotificationInfo.NOTIFICATION_TAG, "10 Presentation notification notice failed to send to API");
                        }
                    });

        }
    }

    protected void onResume() {
        super.onResume();
        EventTracker.getInstance(this).activeScreen(mCurrentPageId != null ? mCurrentPageId : mAppPackage + "-0");

        if (mSetupRequired) {
            doSetup();
            mSetupRequired = false;
        }
    }


    @Subscribe
    public void onNavigationEvent(@NonNull final GodToolsEvent event) {
        // only process events for our local namespace
        if (event.getEventID().inNamespace(mAppPackage)) {
            if (triggerFollowupModal(event.getEventID())) {
                // followup modal was displayed
            } else if (triggerLocalPageNavigation(event.getEventID())) {
                dismissFollowupModal();
            }
        }
    }

    /* END lifecycle */

    @WorkerThread
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onSubscribeEvent(@NonNull final GodToolsEvent event) {
        if (event.getEventID().equals(SUBSCRIBE_EVENT)) {
            processSubscriberEvent(event);
        }
    }

    /**
     * Event triggered when a child page should be shown
     *
     * @param id
     */
    public void onShowChildPage(@NonNull final String id) {
        //mVisibleChildPages.add(id);
        //TODO: figure this out.
        //updateViewPager();
        dismissFollowupModal();
        showPage(id);
    }

    private void setupActionBar() {
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            if (ViewConfigurationCompat.hasPermanentMenuKey(ViewConfiguration.get(this))) {
                actionBar.hide();
            }
        }
    }

    private void initializeAdapter(GPage page)
    {
        mPagerAdapter = new GtPagesPagerAdapter(page);
        LinearLayoutManager layout = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        snuffyRecyclerView.setLayoutManager(layout);
        SnapHelper helper = new PagerSnapHelper();
        helper.attachToRecyclerView(snuffyRecyclerView);
        snuffyRecyclerView.setHasFixedSize(true);
        snuffyRecyclerView.setAdapter(mPagerAdapter);

    }

    private void setupViewPager() {
        if (snuffyRecyclerView != null) {

            // configure page change listener
//            snuffyRecyclerView.addOnPageChangedListener(new RecyclerViewPager.OnPageChangedListener() {
//                @Override
//                public void OnPageChanged(int oldPosition, int newPosition) {
//
//                    //TODO: RM I don't fully understand this.
//                    /*if (mCurrentPageId != null) {
//                        final GPage page = mPagerAdapter.getItemFromPosition(newPosition); //TODO: forloops getItem(mCurrentPageId);
//                        if (page != null) {
//                            // trigger the exit page event
//
//                            //TODO: RM trigger onExitPage();
//                            //page.onExitPage();
//                        }
//                    }*/
//
//                    /*final GPage page = mPagerAdapter.getItemFromPosition(newPosition);
//                    if (page != null) {*/
//                    //TODO: RM track pages that turned, figure out need for onEnterPage()
//                        /*final GtPage model = page.getModel();
//
//                        // track the currently active page
//                        mCurrentPageId = page.getModel().getId();
//                        trackPageView(model);
//
//                        // trigger the enter page event
//                        page.onEnterPage();
//                        */
//                    /*}*/
//
//                    // This notification has been updated to only be sent after the app has been opened 3 times
//                    // The api will only send a notice once, so it can be sent from here multiple times.
//
//                    // if the prayer pages are ever moved this will need to be updated.
//                    //TODO: RM why is this here, this should be elsewhere.
//
//
//                    /* if (settings.getInt(COUNT, 0) >= 3) {
//                        if ((mAppPackage.equalsIgnoreCase(KGP) && newPosition == 7) ||
//                                (mAppPackage.equalsIgnoreCase(FOUR_LAWS) && newPosition == 6)) {
//                            Log.i(TAG, "App used 3 times and prayer page reached.");
//                            GodToolsApiClient.updateNotification(
//                                    settings.getString(AUTH_CODE, ""), regid, NotificationInfo.AFTER_1_PRESENTATION,
//                                    new NotificationUpdateTask.NotificationUpdateTaskHandler() {
//                                        @Override
//                                        public void registrationComplete(String regId) {
//                                            Log.i(NotificationInfo.NOTIFICATION_TAG,
//                                                    "1 Presentation Notification notice sent to API");
//                                        }
//
//                                        @Override
//                                        public void registrationFailed() {
//                                            Log.e(NotificationInfo.NOTIFICATION_TAG,
//                                                    "1 Presentation notification notice failed to send to API");
//                                        }
//                                    });
//                        }
//                    }*/
//
//                }
//            });
        }

    }

    @WorkerThread
    private void processSubscriberEvent(@NonNull final GodToolsEvent event) {
        // look up the followup for the active context
        final DBAdapter dao = DBAdapter.getInstance(this);
        final Followup followup = dao.find(Followup.class, event.getFollowUpId(), Followup.DEFAULT_CONTEXT);
        if (followup != null) {
            // generate subscriber record
            final GSSubscriber subscriber = new GSSubscriber();
            subscriber.setRouteId(followup.getGrowthSpacesRouteId());
            subscriber.setLanguageCode(event.getLanguage());

            // process any fields
            for (final Map.Entry<String, String> field : event.getFields().entrySet()) {
                switch (Strings.nullToEmpty(field.getKey()).toLowerCase(Locale.US)) {
                    case FIELD_EMAIL:
                        subscriber.setEmail(field.getValue());
                        break;
                    case FIELD_FIRST_NAME:
                        subscriber.setFirstName(field.getValue());
                        break;
                    case FIELD_LAST_NAME:
                        subscriber.setLastName(field.getValue());
                        break;
                    case FIELD_NAME:
                        // XXX: This is a best attempt at doing the right thing with a full name when the API expects
                        // XXX: separate first and last names. This handles edge cases by putting all names beyond the
                        // XXX: first one in the last name field.
                        final String[] parts = field.getValue().split(" ", 2);
                        subscriber.setFirstName(parts[0]);
                        if (parts.length > 1) {
                            subscriber.setLastName(parts[1]);
                        }
                        break;
                }
            }

            // store subscriber if it's valid & trigger background sync
            if (subscriber.isValid()) {
                dao.insert(subscriber);
                GodToolsSyncService.syncGrowthSpacesSubscribers(this);
            }
        }
    }

    /**
     * This method is responsible for updating the list of pages being displayed and used by the app
     *
     * @param pages the newnew set of Pages to display
     */
    void updateDisplayedPages(@Nullable final List<GPage> pages) {
        // replace the about page

        //TODO: RM looks like the first page is the about page
        if (pages
                != null && pages.size() > 0) {
            mPagerAdapter.setPages(pages);

            //updateAppPages();

        }

        // store the remaining pages as the actual pages
        //mPages = pages;

        // trigger updates on various components

    }

    private boolean triggerFollowupModal(@NonNull final EventID event) {
        // check for a followup modal on the current page
        //TODO: RM rework this
        /*if (mPagerAdapter != null) {
            final GtPagesPagerAdapter.ViewHolder holder = mPagerAdapter.getPrimaryItem();
            if (holder != null) {
                for (final GtFollowupModal followup : holder.mPage.getModel().getFollowupModals()) {
                    if (followup != null && followup.getListeners().contains(event)) {
                        showFollowupModal(followup);
                        return true;
                    }
                }
            }
        }*/

        return false;
    }

    private boolean triggerLocalPageNavigation(@NonNull final EventID event) {
        //TODO: RM events
        /*if (mPages != null) {
            for (final GPage page : mPages) {
                final GtPage model = page.getModel();
                if (model.getListeners().contains(event)) {
                    showPage(model.getId());
                    return true;
                }
            }
        }
        */

        return false;
    }

    private void showFollowupModal(@NonNull final GtFollowupModal modal) {
        // dismiss any previous followup modal
        dismissFollowupModal();

        // create the newnew followup modal
        final FragmentManager fm = getSupportFragmentManager();
        GtFollowupModalDialogFragment.newInstance(mAppPackage, mAppLanguage, mPackageStatus, modal.getId())
                .show(fm.beginTransaction().addToBackStack(TAG_FOLLOWUP_MODAL), TAG_FOLLOWUP_MODAL);
    }

    private void dismissFollowupModal() {
        final FragmentManager fm = getSupportFragmentManager();
        final Fragment fragment = fm.findFragmentByTag(TAG_FOLLOWUP_MODAL);
        if (fragment instanceof DialogFragment) {
            ((DialogFragment) fragment).dismiss();
        } else if (fragment != null) {
            fm.popBackStack(TAG_FOLLOWUP_MODAL, POP_BACK_STACK_INCLUSIVE);
        }
    }

    private void showPage(@Nullable final String id) {
        /*if (snuffyRecyclerView != null && mPagerAdapter != null && id != null) {
            // are we trying to show the currently active page?
//            if (TextUtils.equals(id, mCurrentPageId)) {
//                final GPage page = mPagerAdapter.getItemFromPosition(Integer.parseInt(mCurrentPageId));
//            }

            final int position = mPagerAdapter.getItemPositionFromId(convertId(id));
            if (position != POSITION_NONE) {
                RecyclerView.LayoutManager layoutManager = snuffyRecyclerView.getLayoutManager();
                layoutManager.scrollToPosition(position);
                //setCurrentItem(position);
            }
        }
        */
    }

//    void clearNotVisibleChildPages() {
//        boolean changed = false;
//        final boolean isVisibleChild = mVisibleChildPages.remove(mCurrentPageId);
//        if (!mVisibleChildPages.isEmpty()) {
//            mVisibleChildPages.clear();
//            changed = true;
//        }
//
//        if (isVisibleChild) {
//            mVisibleChildPages.add(mCurrentPageId);
//            changed = true;
//        }
//
//        //TODO:
//        //what is this?
//        if (changed) {
//            //updateViewPager();
//        }
//    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "Activity stopped");

        if (timer != null) {
            timer.cancel();
            Log.i(NotificationInfo.NOTIFICATION_TAG, "Share Timer stopped");
        }

        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    void doSetup() {
        new Handler().postDelayed(new Runnable() {
            public void run() {
                if (snuffyRecyclerView != null) {

                    // trigger the actual load of pages
                    mProcessPackageAsync = new ProcessPackageAsync();
                    mProcessPackageAsync.execute("");
                }

            }
        }, 1000 / 60);
    }

    private void completeSetup(boolean bSuccess) {
        /*if (!bSuccess) { // now testing is done - only show msg on failure
            Toast.makeText(SnuffyPWActivity.this.getApplicationContext(),
                    getString(R.string.processing_failed),
                    Toast.LENGTH_SHORT).show();
            return;
        }*/

        // track a page view of the most recently loaded page
//        final GtPagesPagerAdapter.ViewHolder holder = mPagerAdapter.getPrimaryItem();
//        if (holder != null) {
//            Diagnostics.StartMethodTracingByKey("Tracker");
//            trackPageView(mPagerAdapter.getItemFromPosition(0));
//            Diagnostics.StopMethodTracingByKey("Tracker");
//        }

        addClickHandlersToAllPages();
    }

    private void addClickHandlersToAllPages() {
        //TODO: what is this for?
        /*Iterator<SnuffyPage> iter = mPages.iterator();

        MyGestureDetector = newnew GestureDetector(newnew MyGestureListener());

        while (iter.hasNext()) {
            iter.next().setOnTouchListener(newnew View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    MyGestureDetector.onTouchEvent(event);
                    return true;
                }
            });
        }
        */
    }

    private void doCmdHelp() {
        Intent intent = new Intent(this, SnuffyHelpActivity.class);
        startActivity(intent);
    }

    private void doCmdShare(View v) {
        String messageBody = buildMessageBody();

        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
        share.putExtra(Intent.EXTRA_TEXT, messageBody);
        startActivity(Intent.createChooser(share, getString(R.string.share_prompt)));
    }

    private String buildMessageBody() {
        String messageBody = "";

        // http://www.knowgod.com/en
        String shareLink = getString(R.string.app_share_link_base_link) + "/" + mAppLanguage;

        if (KGP.equalsIgnoreCase(mAppPackage) || FOUR_LAWS.equalsIgnoreCase(mAppPackage)) {
            // http://www.knowgod.com/en/kgp
            shareLink = shareLink + "/" + mAppPackage;

            messageBody = getString(R.string.share_from_page_message);
        } else if (SATISFIED.equalsIgnoreCase(mAppPackage)) {
            messageBody = getString(R.string.satisfied_share);
        }

        //TODO: revisit
        //final int currItem = snuffyRecyclerView.getLayoutManager().getItem
//        if (currItem > 0) {
//            // http://www.knowgod.com/en/kgp/5
//            shareLink = shareLink + "/" + String.valueOf(currItem);
//        }
//
//        messageBody = messageBody.replace(SHARE_LINK, shareLink);

        return messageBody;
    }

    private void doCmdShowPageMenu(View v) {
        // store pages before triggering Activity launch
        //TODO: figure out why we store?
        /*Crashlytics.log("Storing SnuffyPages in Application before launching SnuffyPageMenuPWActivity: " +
                (mPages != null ? mPages.size() : "null"));*/
        //TODO: determine how much of SnuffyApp we need to use.

        Intent intent = new Intent(this, SnuffyPageMenuPWActivity.class);
        intent.putExtra("LanguageCode", mAppLanguage);
        intent.putExtra("PackageName", mAppPackage);
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_CANCELED)
            return;

        switch (requestCode) {
            case 0: // Show Page Menu
            {
                snuffyRecyclerView.getLayoutManager().scrollToPosition(resultCode - RESULT_FIRST_USER);
                //setCurrentItem(resultCode - RESULT_FIRST_USER);
                break;
            }
        }
    }

    private void switchLanguage() {
        if (isParallelLanguageSet && mParallelPackage != null) {
            if (isUsingPrimaryLanguage) {
                mConfigFileName = mConfigParallel;
                isUsingPrimaryLanguage = false;

            } else {
                mConfigFileName = mConfigPrimary;
                isUsingPrimaryLanguage = true;
            }

            doSetup();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.alternate_language)
                    .setMessage(R.string.alternate_language_message)
                    .setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .create();
            builder.show();
        }

    }

    private void doCmdInfo(View v) {

        Intent intent = new Intent(this, SnuffyAboutActivity.class);

        startActivity(intent);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu_options, menu);

        // enable this feature if the the parallel language is set
        // and a translation is available for this package
        MenuItem switchItem = menu.findItem(R.id.CMD_SWITCH_LANGUAGE);
        switchItem.setVisible(true);

        if (KEY_DRAFT.equalsIgnoreCase(mPackageStatus) && settings.getBoolean(TRANSLATOR_MODE, false)) {
            menu.findItem(R.id.CMD_REFRESH_PAGE).setVisible(true);
        } else {
            menu.findItem(R.id.CMD_REFRESH_PAGE).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (super.onOptionsItemSelected(item))
            return true;
        switch (item.getItemId()) {
            case R.id.CMD_ABOUT: {
                EventTracker.getInstance(this).menuEvent("About");
                doCmdInfo(null);
                break;
            }
            case R.id.CMD_CONTENT: {
                EventTracker.getInstance(this).menuEvent("Content");
                doCmdShowPageMenu(null);
                break;
            }
            case R.id.CMD_EMAIL: {
                EventTracker.getInstance(this).menuEvent("Share");
                doCmdShare(null);
                break;
            }
            case R.id.CMD_HELP: {
                EventTracker.getInstance(this).menuEvent("Help");
                doCmdHelp();
                break;
            }
            case R.id.CMD_SWITCH_LANGUAGE: {
                EventTracker.getInstance(this).menuEvent("Switch Language");
                switchLanguage();
                break;
            }
            case R.id.CMD_REFRESH_PAGE: {
                refreshPage();
                break;
            }
            default:
                break;
        }
        return true;
    }

    //TODO: RM need to figure out the functions of this.n
    private void refreshPage() {
        //final GtPagesPagerAdapter.ViewHolder currentView = mPagerAdapter.getPrimaryItem();
        //final GPage currentPage = currentView != null ? currentView.mPage : null;
        //final String pageUuid = currentPage != null ? currentPage.getModel().getUuid() : null;

        // short-circuit if we don't have a valid UUID
        /*if (pageUuid == null) {
            return;
        }

        showLoading(getString(R.string.update_page));

        final SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        GodToolsApiClient.downloadDraftPage((SnuffyApplication) getApplication(),
                settings.getString(AUTH_DRAFT, ""),
                mAppLanguage,
                mAppPackage,
                pageUuid,
                newnew DownloadTask.DownloadTaskHandler() {
                    @Override
                    public void downloadTaskComplete(String url, String filePath, String langCode, String tag) {
                        List<SnuffyPage> result = mProcessPackageAsync.doInBackground();
                        mProcessPackageAsync.onPostExecute(result);
                        Toast.makeText(getApplicationContext(), getString(R.string.success), Toast.LENGTH_SHORT).show();
                        hideLoading();
                    }

                    @Override
                    public void downloadTaskFailure(String url, String filePath, String langCode, String tag) {
                        Toast.makeText(getApplicationContext(), getString(R.string.page_error), Toast.LENGTH_SHORT).show();
                        hideLoading();
                    }
                });*/
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
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

    /*private void showLoading(String msg) {
        RelativeLayout updatingDraftLayout = (RelativeLayout) findViewById(R.id.updatingDraft);
        updatingDraftLayout.setVisibility(View.VISIBLE);

        TextView updatingPage = (TextView) findViewById(R.id.updatingPageTextView);
        updatingPage.setText(msg);
    }

    private void hideLoading() {
        TextView updatingPage = (TextView) findViewById(R.id.updatingPageTextView);
        updatingPage.setText("");

        RelativeLayout updatingDraftLayout = (RelativeLayout) findViewById(R.id.updatingDraft);
        updatingDraftLayout.setVisibility(View.GONE);

    }*/

    /*private SnuffyApplication getApp() {
        return (SnuffyApplication) getApplication();
    }*/

    void trackPageView(@NonNull final GPage page) {
        EventTracker.getInstance(this).screenView(page.gtapiTrxId, getLanguage());

    }

    private void startTimer() {
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                Log.i(TAG, "Timer complete");
                GodToolsApiClient.updateNotification(settings.getString(AUTH_CODE, ""),
                        regid, NotificationInfo.DAY_AFTER_SHARE, new NotificationUpdateTask.NotificationUpdateTaskHandler() {
                            @Override
                            public void registrationComplete(String regId) {
                                Log.i(NotificationInfo.NOTIFICATION_TAG, "Day After Share Notification notice sent to API");
                            }

                            @Override
                            public void registrationFailed() {
                                Log.e(NotificationInfo.NOTIFICATION_TAG, "Day After Share notification notice failed to send to API");
                            }
                        });
            }
        };

        timer = new Timer("1.5ShareTimer");
        timer.schedule(timerTask, 90000); //1.5 minutes
        Log.i(TAG, "Timer scheduled");
    }

    static class GtPagesPagerAdapter extends RecyclerView.Adapter<SnuffyPWActivity.ViewHolder> {

        private List<GPage> mPages = new ArrayList<>();

        public GtPagesPagerAdapter(GPage gPage) {
            mPages.add(gPage);
            setHasStableIds(true);
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.page_gt_page_frame, parent, false));
        }

//        public void addItem(int position) {
//            final int id = mCurrentItemId++;
//            mItems.add(position, id);
//            notifyItemInserted(position);
//        }
//
//        public void removeItem(int position) {
//            mItems.remove(position);
//            notifyItemRemoved(position);
//        }

        public void addPages(GPage page) {
            mPages.add(page);
            this.notifyItemInserted(mPages.size() - 1);
        }

        public void setPages(@NonNull final List<GPage> pages) {
            mPages = pages; //pages != null ? ImmutableList.copyOf(pages) : ImmutableList.<GPage>of();
            notifyDataSetChanged();
        }

        @Override
        public long getItemId(final int position) {
            //TODO:// FIXME: 12/19/2016
            return position;
        }

        @Override
        public int getItemCount() {
            return mPages.size();
        }

//        @Override
//        protected int getItemPositionFromId(final long id) {
//            /*for (int i = 0; i < mPages.size(); i++) {
//                if (convertId(mPages.get(i).getModel().getId()) == id) {
//                    return i;
//                }
//            }*/
//            return (int) id;
//            //return POSITION_NONE;
//
//        }

        /*
            TODO: no for loops
         */
        @Nullable
        GPage getItemFromPosition(final int position) {
            return mPages.get(position);
        }

        @Override
        public void onBindViewHolder(@NonNull final SnuffyPWActivity.ViewHolder holder, final int position) {


            /*
                TODO: This is duplicating business object.
             */
            //holder.mPage = mPages.get(position);

            //if (holder.mContentContainer != null) {
            // remove any previous page from the content container
                /* This might be costly  */
            //holder.mContentContainer.removeAllViews();
            // t
            // attach the current page to the content container;
            GPage itemFromPosition = getItemFromPosition(position);
            //TODO:

            RenderSingleton.getInstance().addGlobalColor(position, itemFromPosition.getBackgroundColor());
            itemFromPosition.render(LayoutInflater.from(holder.mContentContainer.getContext()),
                    holder.mContentContainer, position);
            //}
        }

//        @Override
//        protected void onViewRecycled(@NonNull final ViewHolder holder) {
//            super.onViewRecycled(holder);
//            //holder.mPage = null;
//            //TODO: this will have a bad effect with rotation and possible timing issues with home, backbuttons.  This is possible memory leak.
//            if (holder.mContentContainer != null) {
//                holder.mContentContainer.removeAllViews();
//
//            }
//        }

    }

    static final class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.pageContainer)
        FrameLayout mContentContainer;

        ViewHolder(@NonNull View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

//    class FragmentsAdapter extends FragmentStatePagerAdapter  {
//        LinkedHashMap<Integer, Fragment> mFragmentCache = new LinkedHashMap<>();
//
//        public FragmentsAdapter(FragmentManager fm) {
//            super(fm);
//            setHasStableIds(false);
//            RenderSingleton.getInstance().setPages(new ArrayList<GPage>());
//        }
//
//        public FragmentsAdapter(List<GPage> mPages, FragmentManager fm) {
//            super(fm);
//            RenderSingleton.getInstance().setPages(mPages);
//
//        }
//
//        public void addPages(GPage page) {
//            RenderSingleton.getInstance().getPages().add(page);
//            this.notifyItemInserted(RenderSingleton.getInstance().getPages().size() - 1);
//        }
//
//        public void setPages(@NonNull final List<GPage> pages) {
//            RenderSingleton.getInstance().setPages(pages);
//            notifyDataSetChanged();
//        }
//
//        GPage getItemFromPosition(final int position) {
//            return RenderSingleton.getInstance().getPages(position);
//        }
//
//
//        @Override
//        public long getItemId(final int position) {
//            //TODO:// FIXME: 12/19/2016
//            return position;
//        }
//
//        @Override
//        public Fragment getItem(int position, Fragment.SavedState savedState) {
//            Fragment f = mFragmentCache.containsKey(position) ? mFragmentCache.get(position)
//
//                    : SlidePageFragment.create(snuffyRecyclerView.getCurrentPosition());
//            Log.e("test", "getItem:" + position + " from cache" + mFragmentCache.containsKey
//                    (position));
////            if (savedState == null || f.getArguments() == null) {
////                Bundle bundle = new Bundle();
////                bundle.putInt("index", position);
////                f.setArguments(bundle);
////                Log.e("test", "setArguments:" + position);
////            } else if (!mFragmentCache.containsKey(position)) {
////                f.setInitialSavedState(savedState);
////                Log.e("test", "setInitialSavedState:" + position);
////            }
//            mFragmentCache.put(position, f);
//            return f;
//        }
//
//        @Override
//        public void onDestroyItem(int position, Fragment fragment) {
//            // onDestroyItem
//            while (mFragmentCache.size() > 5) {
//                Object[] keys = mFragmentCache.keySet().toArray();
//                mFragmentCache.remove(keys[0]);
//            }
//        }
//
////        @Override
////        public String getPageTitle(int position) {
////            return "item-" + position;
////        }
//
//        @Override
//        public int getItemCount() {
//            return RenderSingleton.getInstance().getPages().size();
//        }
//    }



    private class ProcessPackageAsync extends AsyncTask<String, Integer, List<GPage>>
            implements ProgressCallback {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
           /* if (mProgressDialog != null) {
                mProgressDialog.setProgress(0);
                mProgressDialog.setMax(1);
            }
            showDialog(DIALOG_PROCESS_PACKAGE_PROGRESS);*/
        }

        @Override
        @WorkerThread
        protected List<GPage> doInBackground(String... params) {
            // params are not used

            Diagnostics.StartMethodTracingByKey("snuffy");

            try {
                File f = new File(FileUtils.getResourcesDir(SnuffyPWActivity.this), mConfigFileName);

                RenderSingleton.getInstance().setGDocument(XMLUtil.parseGDocument(f));

                GDocument gDocument = RenderSingleton.getInstance().getGDocument();
                for (int i = 0; i < gDocument.pages.size(); i++) {
                    GDocumentPage gdp = gDocument.pages.get(i);

                    File fileForGDP = new File(FileUtils.getResourcesDir(SnuffyPWActivity.this), gdp.filename);

                    if (i == 0) {
                        final GPage gPage = XMLUtil.parseGPage(fileForGDP);
                        SnuffyPWActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                initializeAdapter(gPage);
                            }
                        });
                    } else {
                        final GPage gPage = XMLUtil.parseGPage(fileForGDP);
                        SnuffyPWActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                SnuffyPWActivity.this.mPagerAdapter.addPages(gPage);
                            }
                        });
                    }

                }
                Diagnostics.StopMethodTracingByKey("snuffy");
                return null;
            } catch (Exception e) {
                e.printStackTrace();
            }

            //TODO: understand all items passsed here.

            /*
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
            } catch (Exception e) {
                Log.e(TAG, "processPackage failed: " + e.toString());
                Crashlytics.logException(e);
            }
            if (pages != null) {
                mPackageTitle = packageReader.getPackageTitle();
            }
            */
            return null;
        }

        public void updateProgress(int curr, int max) {
            //onProgressUpdate(curr, max);
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            /*  mProgressDialog.setMax(progress[1]);
            mProgressDialog.setProgress(progress[0]);*/
        }

        @Override
        @UiThread
        protected void onPostExecute(List<GPage> result) {
            /*if (mProgressDialog != null &&
                    mProgressDialog.isShowing()) {
                dismissDialog(DIALOG_PROCESS_PACKAGE_PROGRESS);
            }*/

            //onPagesLoaded(result);
            completeSetup(result != null);
        }
    }

}
