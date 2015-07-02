package org.keynote.godtools.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsoluteLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.keynote.godtools.android.business.GTPackage;
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
import org.keynote.godtools.android.utils.LanguagesNotSupportedByDefaultFont;
import org.keynote.godtools.android.utils.Typefaces;

import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import static org.keynote.godtools.android.utils.Constants.AUTH_DRAFT;
import static org.keynote.godtools.android.utils.Constants.AUTH_GENERIC;
import static org.keynote.godtools.android.utils.Constants.CONFIG_FILE_NAME;
import static org.keynote.godtools.android.utils.Constants.COUNT;
import static org.keynote.godtools.android.utils.Constants.DRAFT;
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
import static org.keynote.godtools.android.utils.Constants.REGISTRATION_ID;
import static org.keynote.godtools.android.utils.Constants.STATUS;
import static org.keynote.godtools.android.utils.Constants.TRANSLATOR_MODE;

public class SnuffyPWActivity extends Activity
{
    private static final String TAG = SnuffyPWActivity.class.getSimpleName();
    private static final int DIALOG_PROCESS_PACKAGE_PROGRESS = 1;
    private String mAppPackage;
    private String mConfigFileName;
    private String mAppLanguage = ENGLISH_DEFAULT;
    private Typeface mAlternateTypeface;
    private Vector<SnuffyPage> mPages = new Vector<SnuffyPage>(0);
    private SnuffyPage mAboutView;
    private ViewPager mPager;
    private int mPagerCurrentItem;
    private MyPagerAdapter mPagerAdapter;
    private boolean mSetupRequired = true;
    private int mPageLeft;
    private int mPageTop;
    private int mPageWidth;
    private int mPageHeight;
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
    private ProgressDialog mProgressDialog;

    private String getLanguage()
    {
        return mAppLanguage;
    }

    private void setLanguage(String languageCode)
    {
        mAppLanguage = languageCode;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        mAppPackage = getIntent().getStringExtra(PACKAGE_NAME);        // "kgp"
        mAppLanguage = getIntent().getStringExtra(LANGUAGE_CODE);      // "en"
        mConfigFileName = getIntent().getStringExtra(CONFIG_FILE_NAME);
        mPackageStatus = getIntent().getStringExtra(STATUS); // live = draft
        mPageLeft = getIntent().getIntExtra(PAGE_LEFT, 0);
        mPageTop = getIntent().getIntExtra(PAGE_TOP, 0);
        mPageWidth = getIntent().getIntExtra(PAGE_WIDTH, 320);         // set defaults but they will not be used
        mPageHeight = getIntent().getIntExtra(PAGE_HEIGHT, 480);       // caller will always determine these and pass them in

        getIntent().putExtra("AllowFlip", false);

        setContentView(R.layout.snuffy_main);
        trackScreenActivity(mAppPackage + "-0");

        mConfigPrimary = mConfigFileName;
        isUsingPrimaryLanguage = true;

        // check if parallel language is set
        settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String langParallel = settings.getString("languageParallel", EMPTY_STRING);


        // get package if parallel language is set
        if (!langParallel.isEmpty())
        {
            isParallelLanguageSet = true;
            mParallelPackage = GTPackage.getPackage(this, mAppPackage, langParallel, "live");
        }

        if (mParallelPackage != null)
        {
            mConfigParallel = mParallelPackage.getConfigFileName();
        }

        // Now we are called from GodTools - do not restore current page
        // always start at 0
        mPagerCurrentItem = 0;

        // TODO: when we can display About or other pages, save that state too so we can restore that too.

        handleLanguagesWithAlternateFonts();

        regid = settings.getString(REGISTRATION_ID, EMPTY_STRING);

        if (mAppPackage.equalsIgnoreCase(KGP) || mAppPackage.equalsIgnoreCase(FOUR_LAWS))
        {
            startTimer();

            GodToolsApiClient.updateNotification(settings.getString(AUTH_GENERIC, EMPTY_STRING),
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

    private void handleLanguagesWithAlternateFonts()
    {
        if (LanguagesNotSupportedByDefaultFont.contains(mAppLanguage))
        {
            mAlternateTypeface = Typefaces.get(getApplication(), LanguagesNotSupportedByDefaultFont.getPathToAlternateFont(mAppLanguage));
        }
    }

    protected void onResume()
    {
        super.onResume();

        if (mSetupRequired)
        {
            doSetup(100); // used to be 1 second delay required to make sure activity fully created
            // - is there something we can test for that is better than a fixed timeout?
            // We reduce this now to 100 mil sec since we are not measuring the device size here
            // since that is done in GodTools which calls us and passes the dimensions in.
            mSetupRequired = false;
        }
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

    }

    private void doSetup(int delay)
    {
        new Handler().postDelayed(new Runnable()
        {
            public void run()
            {

                // release the memory from the old package before we start building the new package
                mPages.clear();
                mPages = null;
                mAboutView = null;
                SnuffyApplication app = (SnuffyApplication) getApplication();
                app.mPages = mPages;
                app.mAboutView = mAboutView;
                app.mPackageTitle = mPackageTitle;
                mPages = new Vector<SnuffyPage>(0);

                /** No instance of pager adapter yet, it's only created on completeSetUp()**/
                //mPagerAdapter.notifyDataSetChanged();

                resizeTheActivity();

                mProcessPackageAsync = new ProcessPackageAsync();
                mProcessPackageAsync.execute(EMPTY_STRING);
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

        addClickHandlersToAllPages();    // TODO: could this just be mPager.setOnClickListener?
        addCallingActivityToAllPages();
        mAboutView = mPages.elementAt(0);
        mPages.remove(mAboutView);

        //mPagerAdapter.notifyDataSetChanged();
        mPagerAdapter = new MyPagerAdapter();
        mPager = (ViewPager) findViewById(R.id.snuffyViewPager);
        mPager.setAdapter(mPagerAdapter);


        if (mPagerCurrentItem >= mPages.size()) // if value from prefs (left over from running with different package?) is out-of-range
            mPagerCurrentItem = 0;                // reset to first page.
        mPager.setCurrentItem(mPagerCurrentItem);
        SnuffyApplication app = (SnuffyApplication) getApplication();
        app.mPages = mPages;
        app.mAboutView = mAboutView;
        app.mPackageTitle = mPackageTitle;

        mPager.setOnPageChangeListener(new OnPageChangeListener()
        {

            @Override
            public void onPageSelected(int position)
            {
                Log.d(TAG, "onPageSelected: " + mAppPackage + Integer.toString(position));
                trackScreenActivity(mAppPackage + "-" + Integer.toString(position));

                View oldPage = mPages.elementAt(mPagerCurrentItem);
                if (SnuffyPage.class.isInstance(oldPage))
                {
                    ((SnuffyPage) oldPage).onExitPage();
                }

                mPagerCurrentItem = position;    // keep our own since ViewPager doesn't offer a getCurrentItem method!

                View newPage = mPages.elementAt(mPagerCurrentItem);
                if (SnuffyPage.class.isInstance(newPage))
                {
                    ((SnuffyPage) newPage).onEnterPage();
                }

                // This notification has been updated to only be sent after the app has been opened 3 times
                // The api will only send a notice once, so it can be sent from here multiple times.

                // if the prayer pages are ever moved this will need to be updated.

                if (settings.getInt(COUNT, 0) >= 3)
                {
                    if ((mAppPackage.equalsIgnoreCase(KGP) && position == 7) || (mAppPackage.equalsIgnoreCase(FOUR_LAWS) && position == 6))
                    {
                        Log.i(TAG, "App used 3 times and prayer page reached.");
                        GodToolsApiClient.updateNotification(settings.getString(AUTH_GENERIC, EMPTY_STRING),
                                regid, NotificationInfo.AFTER_1_PRESENTATION, new NotificationUpdateTask.NotificationUpdateTaskHandler()
                                {
                                    @Override
                                    public void registrationComplete(String regId)
                                    {
                                        Log.i(NotificationInfo.NOTIFICATION_TAG, "1 Presentation Notification notice sent to API");
                                    }

                                    @Override
                                    public void registrationFailed()
                                    {
                                        Log.e(NotificationInfo.NOTIFICATION_TAG, "1 Presentation notification notice failed to send to API");
                                    }
                                });
                    }
                }
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2)
            {
                //Log.d(TAG, "onPageScrolled");
            }

            @Override
            public void onPageScrollStateChanged(int state)
            {
            }
        });
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        SharedPreferences.Editor ed = settings.edit();
        ed.putInt("currPage", mPagerCurrentItem);
        ed.putString("currLanguageCode", getLanguage());
        // TODO: when we can display About or other pages, save that state too so we can restore that too.
        ed.apply();


    }

    private void resizeTheActivity()
    {
        // Update layout to set the size we have decided to use instead of FILL_PARENT
        View pager = findViewById(R.id.snuffyViewPager);
        AbsoluteLayout.LayoutParams lp = (AbsoluteLayout.LayoutParams) pager.getLayoutParams();
        lp.width = mPageWidth;
        lp.height = mPageHeight;
        lp.x = mPageLeft;
        lp.y = mPageTop;
        pager.setLayoutParams(lp);
    }

    private void addClickHandlersToAllPages()
    {
        Iterator<SnuffyPage> iterator = mPages.iterator();

        MyGestureDetector = new GestureDetector(getApplicationContext(), new MyGestureListener());


        while (iterator.hasNext())
        {
            iterator.next().setOnTouchListener(new View.OnTouchListener()
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

    public void doCmdGoToFirstPage(View v)
    {
        mPager.setCurrentItem(0);
    }

    private void doCmdHelp()
    {
        Intent intent = new Intent(this, SnuffyHelpActivity.class);
        intent.putExtra("PackageTitle", mPackageTitle);
        startActivity(intent);
    }

    private String getLinkForPackage()
    {
        String link = getString(R.string.app_email_link); // http://www.godtoolsapp.com/?p=%1&l=%2
        link = link.replace("%1", mAppPackage);
        link = link.replace("%2", mAppLanguage);
        return link;
    }

    public void doCmdShare(View v)
    {
        String msgBody = getString(R.string.app_email_body); // "Get the %@1 App by going to the following link:\n%@2";
        msgBody = msgBody.replace("%1", mPackageTitle);
        msgBody = msgBody.replace("%2", getLinkForPackage());

        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_TEXT, msgBody);
        startActivity(Intent.createChooser(share, getString(R.string.select_share)));
    }

    public void doCmdShowPageMenu(View v)
    {
        Intent intent = new Intent(this, SnuffyPageMenuPWActivity.class);
        intent.putExtra(LANGUAGE_CODE, mAppLanguage);
        intent.putExtra(PACKAGE_NAME, mAppPackage);
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

        settings.edit().putString("currLanguageCode", languageCode).apply();

        mPages.clear();
        mPages = null;
        mAboutView = null;
        ((SnuffyApplication) getApplication()).mPages = mPages;
        ((SnuffyApplication) getApplication()).mAboutView = mAboutView;
        mPages = new Vector<SnuffyPage>(0);
        mPagerAdapter.notifyDataSetChanged(); // try to clear cached views (SnuffyPages) in pager, else they will display until we navigate away and back.
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

            mPager.setAdapter(null);
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

    public void doCmdInfo(View v)
    {
        Intent intent = new Intent(this, SnuffyAboutActivity.class);
        startActivity(intent);
    }

    public void doCmdGoToLastPage(View v)
    {
        mPager.setCurrentItem(mPages.size() - 1);
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

        if (DRAFT.equalsIgnoreCase(mPackageStatus) && settings.getBoolean(TRANSLATOR_MODE, false))
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
            case R.id.CMD_FIRST_PAGE:
            {
                trackScreenEvent("First Page");
                doCmdGoToFirstPage(null);
                break;
            }
            case R.id.CMD_LAST_PAGE:
            {
                trackScreenEvent("Last Page");
                doCmdGoToLastPage(null);
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
        SnuffyPage currentPage = mPages.get(mPagerCurrentItem);

        showLoading(getString(R.string.update_page));

        GodToolsApiClient.downloadDraftPage((SnuffyApplication) getApplication(),
                settings.getString(AUTH_DRAFT, EMPTY_STRING),
                mAppLanguage,
                mAppPackage,
                currentPage.getPageId(),
                new DownloadTask.DownloadTaskHandler()
                {
                    @Override
                    public void downloadTaskComplete(String url, String filePath, String langCode, String tag)
                    {
                        Integer result = mProcessPackageAsync.doInBackground();
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
        updatingPage.setText(EMPTY_STRING);

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
                GodToolsApiClient.updateNotification(settings.getString(AUTH_GENERIC, EMPTY_STRING),
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

    private class MyPagerAdapter extends PagerAdapter
    {
        public int getCount()
        {
            return mPages.size();
        }

        public Object instantiateItem(ViewGroup collection, int position)
        {
            View view = mPages.elementAt(position);
            collection.addView(view, 0);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup viewGroup, int position, Object object)
        {
            viewGroup.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1)
        {
            return arg0 == arg1;
        }

        @Override
        public int getItemPosition(Object object)
        {
            // was return POSITION_UNCHANGED; but then showed cached pages from prev language after we rebuilt pages for new language
            return POSITION_NONE; // force view to redisplay
        }

        @Override
        public Parcelable saveState()
        {
            return null;
        }

        @Override
        public void finishUpdate(ViewGroup arg0)
        {
            // TODO Auto-generated method stub
        }

        @Override
        public void restoreState(Parcelable arg0, ClassLoader arg1)
        {
            // TODO Auto-generated method stub
        }

        @Override
        public void startUpdate(ViewGroup arg0)
        {
        }
    }

    private class ProcessPackageAsync
            extends AsyncTask<String, Integer, Integer>
            implements PackageReader.ProgressCallback
    {

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
        protected Integer doInBackground(String... params)
        {
            // params are not used
            Boolean bSuccess = false;
            PackageReader packageReader = new PackageReader();
            try
            {
                bSuccess = packageReader.processPackagePW(
                        (SnuffyApplication) getApplication(),
                        mPageWidth, mPageHeight,
                        mConfigFileName, mPages,
                        ProcessPackageAsync.this,
                        mAlternateTypeface
                );
            } catch (Exception e)
            {
                Log.e(TAG, "processPackage failed: " + e.toString());
                e.printStackTrace();
            }
            if (bSuccess)
                mPackageTitle = packageReader.getPackageTitle();
            return bSuccess ? 1 : 0;  // could not get Boolean return value to work so use Integer instead!
        }

        public void updateProgress(int curr, int max)
        {
            onProgressUpdate(curr, max);
        }

        @Override
        protected void onProgressUpdate(Integer... progress)
        {
            // TODO: How can we get processPackage to call this?
            mProgressDialog.setMax(progress[1]);
            mProgressDialog.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(Integer result)
        {
            dismissDialog(DIALOG_PROCESS_PACKAGE_PROGRESS);
            // TODO: COMPLETE PROCESSING ON MAIN THREAD
            completeSetup(result != 0);
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
}
