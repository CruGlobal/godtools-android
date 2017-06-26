package org.keynote.godtools.android;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

import com.crashlytics.android.Crashlytics;
import com.google.common.base.Strings;

import org.cru.godtools.api.GodToolsApi;
import org.cru.godtools.api.model.GTNotificationRegister;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.keynote.godtools.android.business.GSSubscriber;
import org.keynote.godtools.android.business.GTPackage;
import org.keynote.godtools.android.db.GodToolsDao;
import org.keynote.godtools.android.googleAnalytics.EventTracker;
import org.keynote.godtools.android.model.Followup;
import org.keynote.godtools.android.notifications.NotificationInfo;
import org.keynote.godtools.android.snuffy.SnuffyAboutActivity;
import org.keynote.godtools.android.snuffy.SnuffyHelpActivity;
import org.keynote.godtools.android.sync.GodToolsSyncService;
import org.keynote.godtools.renderer.crureader.XMLUtil;
import org.keynote.godtools.renderer.crureader.bo.GDocument.GDocument;
import org.keynote.godtools.renderer.crureader.bo.GDocument.GDocumentPage;
import org.keynote.godtools.renderer.crureader.bo.GPage.Event.GodToolsEvent;
import org.keynote.godtools.renderer.crureader.bo.GPage.Event.OnDismissEvent;
import org.keynote.godtools.renderer.crureader.bo.GPage.GPage;
import org.keynote.godtools.renderer.crureader.bo.GPage.RenderHelpers.RenderSingleton;
import org.keynote.godtools.renderer.crureader.bo.GPage.Util.FileUtils;
import org.keynote.godtools.renderer.crureader.bo.GPage.Views.BottomSheetDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.support.v4.app.FragmentManager.POP_BACK_STACK_INCLUSIVE;
import static org.keynote.godtools.android.utils.Constants.AUTH_CODE;
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
import static org.keynote.godtools.renderer.crureader.bo.GPage.Event.GodToolsEvent.EventID.SUBSCRIBE_EVENT;
import static org.keynote.godtools.renderer.crureader.bo.GPage.GInputField.FIELD_EMAIL;
import static org.keynote.godtools.renderer.crureader.bo.GPage.GInputField.FIELD_FIRST_NAME;
import static org.keynote.godtools.renderer.crureader.bo.GPage.GInputField.FIELD_LAST_NAME;
import static org.keynote.godtools.renderer.crureader.bo.GPage.GInputField.FIELD_NAME;

public class SnuffyPWActivity extends AppCompatActivity {

    private static final String TAG = "SnuffyActivity";
    private static final String TAG_FOLLOWUP_MODAL = "followupModal";


    @BindView(R.id.snuffyRecyclerView)
    RecyclerView mSnuffyRecyclerView;

    private GtPagesPagerAdapter mSnuffyPagerAdapter;
    private PagerSnapHelper mPagerSnapHelper;
    private LinearLayoutManager mLinearLayoutManager;

    private GTPackage mMainPackage, mParallelPackage;
    private final Queue<ActivityReadyAction> mActivityReadyQueue = new LinkedList<>();
    private boolean isUsingPrimaryLanguage;

    private String mRegID;
    private Timer timer;

    private boolean mIsConfigChange;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.snuffy_main);

        ButterKnife.bind(this);

        setupActionBar();
        final String mAppPackage = getIntent().getStringExtra("PackageName");        // "kgp"
        String mAppLanguage = getIntent().getStringExtra("LanguageCode");      // "en"
        if(mAppLanguage == null || mAppLanguage.equalsIgnoreCase(""))
        {
            mAppLanguage = ENGLISH_DEFAULT;
        }

        String mPackageStatus = getIntent().getStringExtra("Status"); // live = draft
        getIntent().putExtra("AllowFlip", false);

        isUsingPrimaryLanguage = true;

        //noinspection WrongThread
        mMainPackage = GodToolsDao.getInstance(this).find(GTPackage.class, mAppLanguage, mPackageStatus, mAppPackage);
        // check if parallel language is set
        doSetup(0, mMainPackage);
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                String langParallel = settings.getString(LANGUAGE_PARALLEL, "");

                // get package if parallel language is set

                if (!langParallel.isEmpty()) {
                    mParallelPackage = GodToolsDao.getInstance(SnuffyPWActivity.this)
                            .find(GTPackage.class, langParallel, GTPackage.STATUS_LIVE, mAppPackage);
                }

                mRegID = settings.getString(PROPERTY_REG_ID, "");

                if (mAppPackage.equalsIgnoreCase(KGP) || mAppPackage.equalsIgnoreCase(FOUR_LAWS)) {
                    startTimer();


                    GTNotificationRegister gtNotificationRegister = new GTNotificationRegister(mRegID, NotificationInfo.AFTER_10_PRESENTATIONS);
                    GodToolsApi.getInstance(SnuffyPWActivity.this).legacy
                            .updateNotification(settings.getString(AUTH_CODE, ""), gtNotificationRegister)
                            .enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            if(response.isSuccessful())
                            {
                                Log.i(NotificationInfo.NOTIFICATION_TAG, "10 Presentation Notification notice sent to API");
                            }
                            else
                            {
                                Log.e(NotificationInfo.NOTIFICATION_TAG, "10 Presentation notification notice failed to send to API");
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            Log.e(NotificationInfo.NOTIFICATION_TAG, "10 Presentation notification notice failed to send to API");
                            t.printStackTrace();
                        }
                    });

                }
                return null;
            }
        }.execute();

    }

//    protected void onResume() {
//        super.onResume();
//
//        if (mSetupRequired) {
//            doSetup(0, mMainPackage);
//            mSetupRequired = false;
//        }
//    }

    @Subscribe
    public void onNavigationEvent(@NonNull final GodToolsEvent event) {

        if (!event.getEventID().equals(GodToolsEvent.EventID.ERROR_EVENT)) {
            EventBus.getDefault().post(new OnDismissEvent());
            dismissFollowupModal();
            if (triggerFollowupModal(event)) {
                // followup modal was displayed
            } else if (triggerLocalPageNavigation(event)) {

            }
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(SnuffyPWActivity.this);
            builder.setMessage(event.getErrorContent())
                    .setCancelable(false)
                    .setPositiveButton(RenderSingleton.getInstance().getAppConfig().getOK(), null);
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    @WorkerThread
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onSubscribeEvent(@NonNull final GodToolsEvent event) {
        if (event.getEventID().equals(SUBSCRIBE_EVENT)) {
            Log.i(TAG, "Subscribe event");
            processSubscriberEvent(event);
        }
    }

    private void setupActionBar() {
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            if (ViewConfigurationCompat.hasPermanentMenuKey(ViewConfiguration.get(this))) {
                actionBar.hide();
            }
        }
    }

    private void initializeAdapter(int location, int size, GPage page) {
        mSnuffyPagerAdapter = new GtPagesPagerAdapter(location, size, page);
        mLinearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mSnuffyRecyclerView.setLayoutManager(mLinearLayoutManager);
        mSnuffyRecyclerView.setOnFlingListener(null);
        mPagerSnapHelper = new PagerSnapHelper();
        mPagerSnapHelper.attachToRecyclerView(mSnuffyRecyclerView);
        mSnuffyRecyclerView.setHasFixedSize(true);
        mSnuffyRecyclerView.setAdapter(mSnuffyPagerAdapter);
        if (location > 0) {
            mLinearLayoutManager.scrollToPosition(location);
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        mIsConfigChange = false;
        Log.i(TAG, "On Post Resume");
        while (true) {
            ActivityReadyAction activityReadyAction = mActivityReadyQueue.poll();
            if (activityReadyAction == null)
                break;
            activityReadyAction.onActivityReady();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mIsConfigChange = true;
    }

    @Override
    public boolean isChangingConfigurations() {
        if (android.os.Build.VERSION.SDK_INT >= 11)
            return super.isChangingConfigurations() || mIsConfigChange;
        else
            return mIsConfigChange;
    }

    @WorkerThread
    private void processSubscriberEvent(@NonNull final GodToolsEvent event) {
        // look up the followup for the active context
        final GodToolsDao dao = GodToolsDao.getInstance(this);
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
                GodToolsSyncService.syncGrowthSpacesSubscribers(this).sync();
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private boolean triggerFollowupModal(@NonNull final GodToolsEvent event) {
        // check for a followup modal on the current page
        Log.i(TAG, "EventBus triggerFollowupModal");
        if (mSnuffyPagerAdapter != null) {
            final int hashCode = event.getEventID().getId().hashCode();
            if (RenderSingleton.getInstance().gPanelHashMap.get(hashCode) != null) {

                if (isChangingConfigurations()) {
                    mActivityReadyQueue.add(new ActivityReadyAction() {
                        @Override
                        public void onActivityReady() {
                            final FragmentManager fm = getSupportFragmentManager();
                            BottomSheetDialog bs = BottomSheetDialog.create(event.getPosition(), hashCode);
                            bs.show(fm.beginTransaction().addToBackStack(TAG_FOLLOWUP_MODAL), TAG_FOLLOWUP_MODAL);
                        }
                    });
                } else {

                    final FragmentManager fm = getSupportFragmentManager();
                    BottomSheetDialog bs = BottomSheetDialog.create(event.getPosition(), hashCode);
                    bs.show(fm.beginTransaction().addToBackStack(TAG_FOLLOWUP_MODAL), TAG_FOLLOWUP_MODAL);
                }
                return true;
            }
        }
        return false;
    }

    private boolean triggerLocalPageNavigation(@NonNull final GodToolsEvent event) {
        if (event.getPosition() >= -1) {
            showPage(event.getPosition());
            return true;
        }

        return false;
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

    private void showPage(final int position) {
        if (mSnuffyRecyclerView != null && mSnuffyPagerAdapter != null && position > GodToolsEvent.INVALID_ID) {
            mSnuffyRecyclerView.smoothScrollToPosition(position);
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

    void doSetup(final int startPosition, GTPackage mMainPackage) {

        // trigger the actual load of pages
        ProcessPackageAsync mProcessPackageAsync = new ProcessPackageAsync(mMainPackage);
        mProcessPackageAsync.execute(startPosition);
    }

    private void doCmdHelp() {
        Intent intent = new Intent(this, SnuffyHelpActivity.class);
        startActivity(intent);
    }

    private void doCmdShare() {
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
        String shareLink = getString(R.string.app_share_link_base_link) + "/" + getCurrentLanguage();

        if (KGP.equalsIgnoreCase(mMainPackage.getCode()) || FOUR_LAWS.equalsIgnoreCase(mMainPackage.getCode())) {
            // http://www.knowgod.com/en/kgp
            shareLink = shareLink + "/" + mMainPackage.getCode();

            messageBody = getString(R.string.share_from_page_message);
        } else if (SATISFIED.equalsIgnoreCase(mMainPackage.getCode())) {
            messageBody = getString(R.string.satisfied_share);
        }

        //TODO: revisit
        final int currItem = mPagerSnapHelper.findTargetSnapPosition(mLinearLayoutManager, 0, 0);
        if (currItem > 0) {
            // http://www.knowgod.com/en/kgp/5
            shareLink = shareLink + "/" + String.valueOf(currItem);
        }

        messageBody = messageBody.replace(SHARE_LINK, shareLink);

        return messageBody;
    }

    private void doCmdInfo() {

        Intent intent = new Intent(this, SnuffyAboutActivity.class);
        startActivity(intent);
    }

    private void doCmdShowPageMenu() {

        Intent intent = new Intent(this, SnuffyPageMenuPWActivity.class);
        intent.putExtra("LanguageCode", getCurrentLanguage());
        intent.putExtra("PackageName", mMainPackage.getCode());
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_CANCELED)
            return;

        switch (requestCode) {
            case 0: // Show Page Menu
            {
                mSnuffyRecyclerView.getLayoutManager().scrollToPosition(resultCode - RESULT_FIRST_USER);
                break;
            }
        }
    }

    private void switchLanguage() {
        int snapPositionWithNoVelocity = mPagerSnapHelper.findTargetSnapPosition(mLinearLayoutManager, 0, 0);

        Log.i(TAG, "Snap Position with No velocity: " + snapPositionWithNoVelocity);
        int mPositionBeforeLanguageSwitch = snapPositionWithNoVelocity;
        if (mParallelPackage != null) {
            if (isUsingPrimaryLanguage) {
                doSetup(snapPositionWithNoVelocity, mParallelPackage);
                isUsingPrimaryLanguage = false;

            } else {
                doSetup(snapPositionWithNoVelocity, mMainPackage);
                isUsingPrimaryLanguage = true;
            }

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu_options, menu);

        // enable this feature if the the parallel language is set
        // and a translation is available for this package
        MenuItem switchItem = menu.findItem(R.id.CMD_SWITCH_LANGUAGE);
        switchItem.setVisible(true);
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        if (KEY_DRAFT.equalsIgnoreCase(getCurrentPackageStatus()) && settings.getBoolean(TRANSLATOR_MODE, false)) {
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
                doCmdInfo();
                break;
            }
            case R.id.CMD_CONTENT: {
                EventTracker.getInstance(this).menuEvent("Content");
                doCmdShowPageMenu();
                break;
            }
            case R.id.CMD_EMAIL: {
                EventTracker.getInstance(this).menuEvent("Share");
                doCmdShare();
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
        //final GtPagesPagerAdapter.ViewHolder currentView = mSnuffyPagerAdapter.getPrimaryItem();
        //final GPage currentPage = currentView != null ? currentView.mPage : null;
        //final String pageUuid = currentPage != null ? currentPage.getModel().getUuid() : null;

        // short-circuit if we don't have a valid UUID
        /*if (pageUuid == null) {
            return;
        }

        showLoading(getString(R.string.update_page));

        final SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        PackageDownloadHelper.downloadDraftPage((SnuffyApplication) getApplication(),
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

    private void startTimer() {
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                Log.i(TAG, "Timer complete");
                SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                GTNotificationRegister gtNotificationRegister = new GTNotificationRegister(mRegID, NotificationInfo.DAY_AFTER_SHARE);
                GodToolsApi.getInstance(SnuffyPWActivity.this).legacy
                        .updateNotification(settings.getString(AUTH_CODE, ""), gtNotificationRegister)
                        .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if(response.isSuccessful())
                        {
                            Log.i(NotificationInfo.NOTIFICATION_TAG, "Day After Share Notification notice sent to API");
                        }
                        else
                        {
                            Log.e(NotificationInfo.NOTIFICATION_TAG, "Day After Share notification notice failed to send to API");
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.e(NotificationInfo.NOTIFICATION_TAG, "Day After Share notification notice failed to send to API");
                    }
                });
            }
        };

        timer = new Timer("1.5ShareTimer");
        timer.schedule(timerTask, 90000); //1.5 minutes
        Log.i(TAG, "Timer scheduled");
    }

    public String getCurrentLanguage() {
        return isUsingPrimaryLanguage ? mMainPackage.getLanguage() : mParallelPackage.getLanguage();
    }

    public String getCurrentPackageStatus()
    {
        return isUsingPrimaryLanguage ? mMainPackage.getStatus() : mParallelPackage.getStatus();
    }

    public interface ActivityReadyAction {
        void onActivityReady();
    }

    static class GtPagesPagerAdapter extends RecyclerView.Adapter<SnuffyPWActivity.ViewHolder> {

        private final List<GPage> mPages;

        public GtPagesPagerAdapter(int location, int capacity, GPage gPage) {
            GPage blankPage = new GPage();
            mPages = new ArrayList<GPage>();
            for (int i = 0; i < capacity; i++) {
                if (i == location) {
                    mPages.add(gPage);
                } else {
                    mPages.add(blankPage);
                }

            }

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

        public void addPages(int location, GPage page) {
            mPages.set(location, page);
            this.notifyItemChanged(location);
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

        @Nullable
        GPage getItemFromPosition(final int position) {
            return mPages.get(position);
        }

        @Override
        public void onBindViewHolder(@NonNull final SnuffyPWActivity.ViewHolder holder, final int position) {

            GPage itemFromPosition = getItemFromPosition(position);

            RenderSingleton.getInstance().addGlobalColor(position, itemFromPosition.getBackgroundColor());
            itemFromPosition.render(LayoutInflater.from(holder.mContentContainer.getContext()),
                    holder.mContentContainer, position);

        }

    }

    static final class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.pageContainer)
        FrameLayout mContentContainer;

        ViewHolder(@NonNull View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    private class ProcessPackageAsync extends AsyncTask<Integer, Integer, List<GPage>> {

        GTPackage mGTPackageToLoad;

        public ProcessPackageAsync(GTPackage mMainPackage) {
            this.mGTPackageToLoad = mMainPackage;
        }

        @Override
        @WorkerThread
        protected List<GPage> doInBackground(Integer... params) {
            // params are not used
            final int loadingStartPosition = params.length > 0 ? params[0] : 0;

            File f = new File(FileUtils.getResourcesDir(), this.mGTPackageToLoad.getConfigFileName());

            try {
                RenderSingleton.getInstance().setGDocument(XMLUtil.parseGDocument(f, this.mGTPackageToLoad.getConfigFileName(), this.mGTPackageToLoad.getCode(), this.mGTPackageToLoad.getLanguage()));

                final GDocument gDocument = RenderSingleton.getInstance().getGDocument();
                new Thread() {
                    public void run() {
                        super.run();
                        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                        for (int i = loadingStartPosition; i >= 0; i--) {
                            GDocumentPage gdp = gDocument.documentPages.get(i);

                            File fileForGDP = new File(FileUtils.getResourcesDir(), gdp.filename);
                            try {
                                if (i == loadingStartPosition) {
                                    final GPage gPage = XMLUtil.parseGPage(fileForGDP);
                                    SnuffyPWActivity.this.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            initializeAdapter(loadingStartPosition, gDocument.documentPages.size(), gPage);
                                            new Thread() {
                                                public void run() {
                                                    /* Lower the thread priority, so it doesn't interfere with loading images from the disk */
                                                    Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND - 1);
                                                    Thread.yield();
                                                    for (int i = loadingStartPosition + 1; i < gDocument.documentPages.size(); i++) {
                                                        GDocumentPage gdp = gDocument.documentPages.get(i);

                                                        File fileForGDP = new File(FileUtils.getResourcesDir(), gdp.filename);
                                                        try {
                                                            final GPage gPage = XMLUtil.parseGPage(fileForGDP);
                                                            final int addIndex = i;
                                                            SnuffyPWActivity.this.runOnUiThread(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    SnuffyPWActivity.this.mSnuffyPagerAdapter.addPages(addIndex, gPage);
                                                                }
                                                            });
                                                        } catch (Exception e) {
                                                            Crashlytics.logException(e);
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                }
                                            }.start();
                                        }
                                    });
                                } else {
                                    /* Lower the thread priority, so it doesn't interfere with loading images from the disk */
                                    Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND - 1);
                                    Thread.yield();
                                    final GPage gPage = XMLUtil.parseGPage(fileForGDP);
                                    final int addIndex = i;
                                    SnuffyPWActivity.this.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            SnuffyPWActivity.this.mSnuffyPagerAdapter.addPages(addIndex, gPage);
                                        }
                                    });
                                }
                            } catch (Exception e) {
                                Crashlytics.logException(e);
                                e.printStackTrace();
                            }
                        }
                    }

                }.start();

            } catch (Exception e) {
                Crashlytics.logException(e);
                e.printStackTrace();
            }
            return null;
        }

    }

}
