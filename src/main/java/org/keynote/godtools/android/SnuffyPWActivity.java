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
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.base.Strings;

import org.ccci.gto.android.common.support.v4.adapter.ViewHolderPagerAdapter;
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
import org.keynote.godtools.android.snuffy.PackageReader;
import org.keynote.godtools.android.snuffy.SnuffyAboutActivity;
import org.keynote.godtools.android.snuffy.SnuffyApplication;
import org.keynote.godtools.android.snuffy.SnuffyHelpActivity;
import org.keynote.godtools.android.snuffy.model.GtFollowupModal;
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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static android.support.v4.app.FragmentManager.POP_BACK_STACK_INCLUSIVE;
import static android.support.v4.view.PagerAdapter.POSITION_NONE;
import static org.ccci.gto.android.common.support.v4.util.IdUtils.convertId;
import static org.keynote.godtools.android.event.GodToolsEvent.EventID.SUBSCRIBE_EVENT;
import static org.keynote.godtools.android.snuffy.model.GtInputField.FIELD_EMAIL;
import static org.keynote.godtools.android.snuffy.model.GtInputField.FIELD_FIRST_NAME;
import static org.keynote.godtools.android.snuffy.model.GtInputField.FIELD_LAST_NAME;
import static org.keynote.godtools.android.snuffy.model.GtInputField.FIELD_NAME;
import static org.keynote.godtools.android.utils.Constants.AUTH_CODE;
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
public class SnuffyPWActivity extends AppCompatActivity {
    private static final String TAG = "SnuffyActivity";
    private static final String TAG_FOLLOWUP_MODAL = "followupModal";
    private static final int DIALOG_PROCESS_PACKAGE_PROGRESS = 1;
    private final Set<String> mVisibleChildPages = new HashSet<>();
    @BindView(R.id.snuffyViewPager)
    ViewPager mPager;
    GtPagesPagerAdapter mPagerAdapter;
    @Nullable
    String mCurrentPageId;
    @NonNull
    private EventTracker mTracker;
    private String mAppPackage;
    private String mConfigFileName;
    private String mAppLanguage = ENGLISH_DEFAULT;
    private Unbinder mButterKnife;
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

    //TODO: RM No BO tracking in activity?   May be memory leak.
    // @Nullable
    //private List<GPage> mPages;
    @Nullable
    private GPage mAboutView;
    private ProgressDialog mProgressDialog;

    /* BEGIN lifecycle */

    private String getLanguage() {
        return mAppLanguage;
    }

    private void setLanguage(String languageCode) {
        mAppLanguage = languageCode;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        mTracker = EventTracker.getInstance(this);

        setContentView(R.layout.snuffy_main);
        mButterKnife = ButterKnife.bind(this);
        setupActionBar();
        setupViewPager();

        Log.i("Activity", "SnuffyPWActivity");

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
        mTracker.activeScreen(mCurrentPageId != null ? mCurrentPageId : mAppPackage + "-0");

        if (mSetupRequired) {
            doSetup();
            mSetupRequired = false;
        }
    }

    /**
     * Event triggered whenever a new set of pages is loaded.
     *
     * @param pages the Pages that were just loaded.
     */
    void onPagesLoaded(@Nullable final List<GPage> pages) {
        updateDisplayedPages(pages);
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

    @WorkerThread
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onSubscribeEvent(@NonNull final GodToolsEvent event) {
        if (event.getEventID().equals(SUBSCRIBE_EVENT)) {
            processSubscriberEvent(event);
        }
    }

    /* END lifecycle */

    /**
     * Event triggered when a child page should be shown
     *
     * @param id
     */
    public void onShowChildPage(@NonNull final String id) {
        mVisibleChildPages.add(id);
        //TODO: figure this out.
        //updateViewPager();
        dismissFollowupModal();
        showPage(id);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cleanupViewPager();
        mButterKnife.unbind();
    }

    private void setupActionBar() {
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            if (ViewConfigurationCompat.hasPermanentMenuKey(ViewConfiguration.get(this))) {
                actionBar.hide();
            }
        }
    }

    private void setupViewPager() {
        if (mPager != null) {
            mPagerAdapter = new GtPagesPagerAdapter();
            mPager.setAdapter(mPagerAdapter);

            // configure page change listener
            mPager.addOnPageChangeListener(new SimpleOnPageChangeListener() {
                @Override
                public void onPageSelected(int position) {

                    //TODO: RM I don't fully understand this.
                    if (mCurrentPageId != null) {
                        final GPage page = mPagerAdapter.getItemFromPosition(position); //TODO: forloops getItem(mCurrentPageId);
                        if (page != null) {
                            // trigger the exit page event

                            //TODO: RM trigger onExitPage();
                            //page.onExitPage();
                        }
                    }

                    final GPage page = mPagerAdapter.getItemFromPosition(position);
                    if (page != null) {
                        //TODO: RM track pages that turned, figure out need for onEnterPage()
                        /*final GtPage model = page.getModel();

                        // track the currently active page
                        mCurrentPageId = page.getModel().getId();
                        trackPageView(model);

                        // trigger the enter page event
                        page.onEnterPage();
                        */
                    }

                    // This notification has been updated to only be sent after the app has been opened 3 times
                    // The api will only send a notice once, so it can be sent from here multiple times.

                    // if the prayer pages are ever moved this will need to be updated.
                    //TODO: RM why is this here, this should be elsewhere.
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

                @Override
                public void onPageScrollStateChanged(final int state) {
                    switch (state) {
                        case ViewPager.SCROLL_STATE_IDLE:
                            clearNotVisibleChildPages();
                            break;
                    }
                }
            });
        }

        // trigger an initial update
        //updateViewPager();
    }

    private void updateViewPager(List<GPage> gPages) {
        //TODO RM: not certain if this is needed still.

        /*if (mPagerAdapter != null) {
            final List<GPage> pages;
            if (mVisibleChildPages.isEmpty() || mPages == null) {
                pages = mPages;
            } else {
                pages = new ArrayList<>();
                for (final GPage page : mPages) {
                    pages.add(page);
                    for (final String name : mVisibleChildPages) {
                        final SnuffyPage child = page.getChildPage(name);
                        if (child != null) {
                            pages.add(child);
                        }
                    }
                }
            }

            mPagerAdapter.setPages(pages);
        }*/
        mPagerAdapter.setPages(gPages);
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

        //TODO: deteremine about vieww
        //app.aboutView = mAboutView;
        //TODO: RM the pages are saved to activity, no need for parent activity.
        //app.setSnuffyPages(mPages);
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
     * @param pages the new set of Pages to display
     */
    void updateDisplayedPages(@Nullable final List<GPage> pages) {
        // replace the about page
        mAboutView = null;
        //TODO: RM looks like the first page is the about page
        if (pages
                != null && pages.size() > 0) {

            updateViewPager(pages);
            updateAppPages();

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

        // create the new followup modal
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
        if (mPager != null && mPagerAdapter != null && id != null) {
            // are we trying to show the currently active page?
            if (TextUtils.equals(id, mCurrentPageId)) {
                //final SnuffyPage page = mPagerAdapter.getItem(mCurrentPageId);
                final GPage page = mPagerAdapter.getItemFromPosition(Integer.parseInt(mCurrentPageId));
                //TODO: RM // FIXME: 12/19/2016
                /*if (page != null) {
                    // hide any active panel modals
                    page.hideAllModals();
                }*/
            }

            final int position = mPagerAdapter.getItemPositionFromId(convertId(id));
            if (position != POSITION_NONE) {
                mPager.setCurrentItem(position);
            }
        }
    }

    void clearNotVisibleChildPages() {
        boolean changed = false;
        final boolean isVisibleChild = mVisibleChildPages.remove(mCurrentPageId);
        if (!mVisibleChildPages.isEmpty()) {
            mVisibleChildPages.clear();
            changed = true;
        }

        if (isVisibleChild) {
            mVisibleChildPages.add(mCurrentPageId);
            changed = true;
        }

        //TODO:
        //what is this?
        if (changed) {
            //updateViewPager();
        }
    }

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
                if (mPager != null) {
                    final int width = mPager.getMeasuredWidth();
                    final int height = mPager.getMeasuredHeight();
                    if (width > 0 && height > 0) {
                        // trigger the actual load of pages
                        mProcessPackageAsync = new ProcessPackageAsync(width, height);
                        mProcessPackageAsync.execute("");
                    } else {
                        doSetup();
                    }
                }
            }
        }, 1000 / 60);
    }

    private void completeSetup(boolean bSuccess) {
        if (!bSuccess) { // now testing is done - only show msg on failure
            Toast.makeText(SnuffyPWActivity.this.getApplicationContext(),
                    getString(R.string.processing_failed),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // track a page view of the most recently loaded page
        final GtPagesPagerAdapter.ViewHolder holder = mPagerAdapter.getPrimaryItem();
        if (holder != null) {
            Diagnostics.StartMethodTracingByKey("Tracker");
            trackPageView(mPagerAdapter.getItemFromPosition(0));
            Diagnostics.StopMethodTracingByKey("Tracker");
        }

        addClickHandlersToAllPages();
        addCallingActivityToAllPages();
    }

    private void addClickHandlersToAllPages() {
        //TODO: what is this for?
        /*Iterator<SnuffyPage> iter = mPages.iterator();

        MyGestureDetector = new GestureDetector(new MyGestureListener());

        while (iter.hasNext()) {
            iter.next().setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    MyGestureDetector.onTouchEvent(event);
                    return true;
                }
            });
        }
        */
    }

    private void addCallingActivityToAllPages() {
        //TODO: i don't think this will be coming back in, not sure though.
        /*for (SnuffyPage mPage : mPages) {
            mPage.mCallingActivity = this; // the SnuffyActivity owns most pages except the about page - which will be set explicitly
        }*/
    }

    private void doCmdHelp() {
        Intent intent = new Intent(this, SnuffyHelpActivity.class);
        intent.putExtra("PackageTitle", mPackageTitle);
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

        final int currItem = mPager.getCurrentItem();
        if (currItem > 0) {
            // http://www.knowgod.com/en/kgp/5
            shareLink = shareLink + "/" + String.valueOf(currItem);
        }

        messageBody = messageBody.replace(SHARE_LINK, shareLink);

        return messageBody;
    }

    private void doCmdShowPageMenu(View v) {
        // store pages before triggering Activity launch
        //TODO: figure out why we store?
        /*Crashlytics.log("Storing SnuffyPages in Application before launching SnuffyPageMenuPWActivity: " +
                (mPages != null ? mPages.size() : "null"));*/
        //TODO: determine how much of SnuffyApp we need to use.
        //getApp().setSnuffyPages(mPages);

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
                mPager.setCurrentItem(resultCode - RESULT_FIRST_USER);
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
                mTracker.menuEvent("About");
                doCmdInfo(null);
                break;
            }
            case R.id.CMD_CONTENT: {
                mTracker.menuEvent("Content");
                doCmdShowPageMenu(null);
                break;
            }
            case R.id.CMD_EMAIL: {
                mTracker.menuEvent("Share");
                doCmdShare(null);
                break;
            }
            case R.id.CMD_HELP: {
                mTracker.menuEvent("Help");
                doCmdHelp();
                break;
            }
            case R.id.CMD_SWITCH_LANGUAGE: {
                mTracker.menuEvent("Switch Language");
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
        final GtPagesPagerAdapter.ViewHolder currentView = mPagerAdapter.getPrimaryItem();
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
                new DownloadTask.DownloadTaskHandler() {
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

    private void showLoading(String msg) {
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

    }

    private SnuffyApplication getApp() {
        return (SnuffyApplication) getApplication();
    }

    void trackPageView(@NonNull final GPage page) {
        //TODO: // FIXME: 12/19/2016
        mTracker.screenView("0", "enUS");
        //mTracker.screenView(page.getId(), page.getManifest().getLanguage());
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

    static class GtPagesPagerAdapter extends ViewHolderPagerAdapter<GtPagesPagerAdapter.ViewHolder> {
        @NonNull
        private List<GPage> mPages = new ArrayList<GPage>(); // ImmutableList.of();

        public GtPagesPagerAdapter() {
            setHasStableIds(true);
        }

        public void setPages(@NonNull final List<GPage> pages) {
            mPages = pages; //pages != null ? ImmutableList.copyOf(pages) : ImmutableList.<GPage>of();
            notifyDataSetChanged();
        }

        @Override
        public long getItemId(final int position) {
            //TODO:// FIXME: 12/19/2016
            return position; //convertId(mPages.get(position).getModel().getId());
        }

        @Override
        protected int getItemPositionFromId(final long id) {
            /*for (int i = 0; i < mPages.size(); i++) {
                if (convertId(mPages.get(i).getModel().getId()) == id) {
                    return i;
                }
            }*/
            return (int) id;
            //return POSITION_NONE;

        }

        /*
            TODO: no for loops
         */
        @Nullable
        GPage getItemFromPosition(final int position) {
            if (position >= 0 && position < mPages.size()) {
                return mPages.get(position);
            }
            return null;
        }

        /*
            TODO: no for loops
         */
        /*@Nullable
        SnuffyPage getItem(@Nullable final String id) {
            for (final GPage page : mPages) {
                if (TextUtils.equals(page.getModel().getId(), id)) {
                    return page;
                }
            }
            return null;
        }*/

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

            /*
                TODO: This is duplicating business object.
             */
            //holder.mPage = mPages.get(position);

            if (holder.mContentContainer != null) {
                // remove any previous page from the content container
                /* This might be costly  */
                holder.mContentContainer.removeAllViews();
                // t
                // attach the current page to the content container;
                GPage itemFromPosition = getItemFromPosition(position);
                //TODO:
                RenderSingleton.getInstance().addGlobalColor(position, itemFromPosition.getBackgroundColor());
                itemFromPosition.render(LayoutInflater.from(holder.mContentContainer.getContext()),
                        holder.mContentContainer, position);
            }
        }

        @Override
        protected void onViewRecycled(@NonNull final ViewHolder holder) {
            super.onViewRecycled(holder);
            //holder.mPage = null;
            //TODO: this will have a bad effect with rotation and possible timing issues with home, backbuttons.  This is possible memory leak.
            if (holder.mContentContainer != null) {
                holder.mContentContainer.removeAllViews();
            }
        }

        static final class ViewHolder extends ViewHolderPagerAdapter.ViewHolder {

            /*
            TODO: Shouldn't have business objects in ViewHolder.
             */
            /*@Nullable
            SnuffyPage mPage;*/

            @Nullable
            @BindView(R.id.pageContainer)
            FrameLayout mContentContainer;

            ViewHolder(@NonNull View view) {
                super(view);
                ButterKnife.bind(this, view);
            }
        }
    }

    private class ProcessPackageAsync extends AsyncTask<String, Integer, List<GPage>>
            implements PackageReader.ProgressCallback {
        private final int mPageWidth;
        private final int mPageHeight;

        public ProcessPackageAsync(int width, int height) {
            mPageWidth = width;
            mPageHeight = height;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (mProgressDialog != null) {
                mProgressDialog.setProgress(0);
                mProgressDialog.setMax(1);
            }
            showDialog(DIALOG_PROCESS_PACKAGE_PROGRESS);
        }

        @Override
        @WorkerThread
        protected List<GPage> doInBackground(String... params) {
            // params are not used
            List<GPage> pages = new ArrayList<GPage>();
            PackageReader packageReader = new PackageReader();
            try {
                File f = new File(FileUtils.getResourcesDir(SnuffyPWActivity.this), mConfigFileName);

                GDocument gDocument

                        = XMLUtil.parseGDocument(SnuffyPWActivity.this.getBaseContext(), f);

                for(GDocumentPage gdp : gDocument.pages)
                {
                    File fileForGDP = new File(FileUtils.getResourcesDir(SnuffyPWActivity.this), gdp.filename);
                    pages.add(XMLUtil.parseGPage(SnuffyPWActivity.this, fileForGDP));
                }

                return pages;
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
            onProgressUpdate(curr, max);
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            mProgressDialog.setMax(progress[1]);
            mProgressDialog.setProgress(progress[0]);
        }

        @Override
        @UiThread
        protected void onPostExecute(List<GPage> result) {
            if (mProgressDialog != null &&
                    mProgressDialog.isShowing()) {
                dismissDialog(DIALOG_PROCESS_PACKAGE_PROGRESS);
            }

            onPagesLoaded(result);
            completeSetup(result != null);
        }
    }

    private class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            openOptionsMenu();
            return super.onSingleTapUp(e);
        }
    }
}
