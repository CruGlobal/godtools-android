package org.keynote.godtools.android;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AbsoluteLayout;
import android.widget.Toast;

import org.keynote.godtools.android.business.GTPackage;
import org.keynote.godtools.android.snuffy.PackageReader;
import org.keynote.godtools.android.snuffy.SnuffyAboutActivity;
import org.keynote.godtools.android.snuffy.SnuffyApplication;
import org.keynote.godtools.android.snuffy.SnuffyHelpActivity;
import org.keynote.godtools.android.snuffy.SnuffyPage;
import org.keynote.godtools.android.utils.LanguagesNotSupportedByDefaultFont;
import org.keynote.godtools.android.utils.Typefaces;

import java.util.Iterator;
import java.util.Vector;

public class SnuffyPWActivity extends Activity {
    private static final String TAG = "SnuffyActivity";

    private static final String PREFS_NAME = "GodTools";

    private String mAppPackage;
    private String mConfigFileName;
    private String mAppLanguage = "en";
    private String mAppLanguageDefault = "en";
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
    private ProcessPackageAsync mProcessPackageAsync;

    private String mConfigPrimary, mConfigParallel;
    private GTPackage mParallelPackage;
    private boolean isUsingPrimaryLanguage, isParallelLanguageSet;

    public void setLanguage(String languageCode) {
        mAppLanguage = languageCode;
    }

    public String getLanguage() {
        return mAppLanguage;
    }

    public void setLanguageDefault(String languageCode) {
        mAppLanguageDefault = languageCode;
    }

    public String getLanguageDefault() {
        return mAppLanguageDefault;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        mAppPackage = getIntent().getStringExtra("PackageName");        // "kgp"
        mAppLanguage = getIntent().getStringExtra("LanguageCode");      // "en"
        mConfigFileName = getIntent().getStringExtra("ConfigFileName");
        mPageLeft = getIntent().getIntExtra("PageLeft", 0);
        mPageTop = getIntent().getIntExtra("PageTop", 0);
        mPageWidth = getIntent().getIntExtra("PageWidth", 320);         // set defaults but they will not be used
        mPageHeight = getIntent().getIntExtra("PageHeight", 480);       // caller will always determine these and pass them in
        getIntent().putExtra("AllowFlip", false);

        setContentView(R.layout.snuffy_main);

        /** Only set the pager adapter on completeSetUp() **/
        //mPagerAdapter = new MyPagerAdapter();
        //mPager = (ViewPager) findViewById(R.id.snuffyViewPager);
        //mPager.setAdapter(mPagerAdapter);


        mConfigPrimary = mConfigFileName;
        isUsingPrimaryLanguage = true;

        // check if parallel language is set
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String langParallel = settings.getString("languageParallel", "");

        // get package if parallel language is set
        if (!langParallel.isEmpty()) {
            isParallelLanguageSet = true;
            mParallelPackage = GTPackage.getPackage(this, mAppPackage, langParallel);
        }

        if (mParallelPackage != null)
            mConfigParallel = mParallelPackage.getConfigFileName();

        // Now we are called from GodTools - do not restore current page
        // always start at 0
        mPagerCurrentItem = 0;

        // not appropriate from God tools: setLanguage(settings.getString("currLanguageCode", getLanguageDefault()));
        // TODO: when we can display About or other pages, save that state too so we can restore that too.

        handleLanguagesWithAlternateFonts();

        //doSetup(100); // used to be 1 second delay required to make sure activity fully created
        // - is there something we can test for that is better than a fixed timeout?
        // We reduce this now to 100 msec since we are not measuring the device size here
        // since that is done in GodTools which calls us and passes the dimensions in.
    }

    private void handleLanguagesWithAlternateFonts() {
        if (LanguagesNotSupportedByDefaultFont.contains(mAppLanguage)) {
            mAlternateTypeface = Typefaces.get(getApplication(), LanguagesNotSupportedByDefaultFont.getPathToAlternateFont(mAppLanguage));
        }

    }

    private class MyPagerAdapter extends PagerAdapter {
        public int getCount() {
            int n = mPages.size();
            return n;
        }

        public Object instantiateItem(View collection, int position) {
            View view = mPages.elementAt(position);
            ((ViewPager) collection).addView(view, 0);
            return view;
        }

        @Override
        public void destroyItem(View arg0, int arg1, Object arg2) {
            ((ViewPager) arg0).removeView((View) arg2);
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == ((View) arg1);
        }

        @Override
        public int getItemPosition(Object object) {
            // was return POSITION_UNCHANGED; but then showed cached pages from prev language after we rebuilt pages for new language
            return POSITION_NONE; // force view to redisplay
        }

        @Override
        public Parcelable saveState() {
            return null;
        }

        @Override
        public void finishUpdate(View arg0) {
            // TODO Auto-generated method stub
        }

        @Override
        public void restoreState(Parcelable arg0, ClassLoader arg1) {
            // TODO Auto-generated method stub
        }

        @Override
        public void startUpdate(View arg0) {
        }
    }

    protected void onResume() {
        super.onResume();

        if (mSetupRequired) {
            doSetup(100); // used to be 1 second delay required to make sure activity fully created
            // - is there something we can test for that is better than a fixed timeout?
            // We reduce this now to 100 msec since we are not measuring the device size here
            // since that is done in GodTools which calls us and passes the dimensions in.
            mSetupRequired = false;
        }

    }

    private void doSetup(int delay) {
        new Handler().postDelayed(new Runnable() {
            public void run() {

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
                mProcessPackageAsync.execute("");
            }
        }, delay);  // delay can be required to make sure activity fully created - is there something we can test for that is better than a fixed timeout?

    }

    private void completeSetup(boolean bSuccess) {
        if (!bSuccess) { // now testing is done - only show msg on failure
            Toast.makeText(SnuffyPWActivity.this.getApplicationContext(),
                    bSuccess ? "Package processing succeeded" : "Package processing failed",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        addClickHandlersToAllPages();    // TODO: could this just be mPager.setOnClickLIstner?
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

        mPager.setOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                Log.d(TAG, "onPageSelected: " + Integer.toString(position));
                View oldPage = mPages.elementAt(mPagerCurrentItem);
                if (SnuffyPage.class.isInstance(oldPage)) {
                    ((SnuffyPage) oldPage).onExitPage();
                }

                mPagerCurrentItem = position;    // keep our own since ViewPager doesn't offer a getCurrentItem method!

                View newPage = mPages.elementAt(mPagerCurrentItem);
                if (SnuffyPage.class.isInstance(newPage)) {
                    ((SnuffyPage) newPage).onEnterPage();
                }
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
                //Log.d(TAG, "onPageScrolled");
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // arg0 is 0, 1 or 2 and m_currItem contains more info
                //switch (state) {
                //case ViewPager.SCROLL_STATE_IDLE:
                //	Log.d(TAG, "onPageScrollStateChanged: IDLE");
                //	break;
                //case ViewPager.SCROLL_STATE_DRAGGING:
                //	Log.d(TAG, "onPageScrollStateChanged: DRAGGING");
                //	break;
                //case ViewPager.SCROLL_STATE_SETTLING:
                //	Log.d(TAG, "onPageScrollStateChanged: SETTLING");
                //	break;
                //}
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor ed = settings.edit();
        ed.putInt("currPage", mPagerCurrentItem);
        ed.putString("currLanguageCode", getLanguage());
        // TODO: when we can display About or other pages, save that state too so we can restore that too.
        ed.commit();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Save data that is to be preserved across configuration changes.
        // This method is called to retrieve per-instance state from an activity before being killed
        // so that the state can be restored in onCreate(Bundle) or onRestoreInstanceState(Bundle)
        // (the Bundle populated by this method will be passed to both).

        super.onSaveInstanceState(outState);
        //outState.putInt("xx"  , this.mxx);

        // we dont have any of this yet. We use prefs so the curr pos even survives a total restart.
    }


    private void resizeTheActivity() {
        // Update layout to set the size we have decided to use instead of FILL_PARENT
        View pager = findViewById(R.id.snuffyViewPager);
        AbsoluteLayout.LayoutParams lp = (AbsoluteLayout.LayoutParams) pager.getLayoutParams();
        lp.width = mPageWidth;
        lp.height = mPageHeight;
        lp.x = mPageLeft;
        lp.y = mPageTop;
        pager.setLayoutParams(lp);
    }

    private void addClickHandlersToAllPages() {
        Iterator<SnuffyPage> iter = mPages.iterator();

        while (iter.hasNext()) {
            iter.next().setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    openOptionsMenu();
                }
            });
        }
    }

    private void addCallingActivityToAllPages() {
        Iterator<SnuffyPage> iter = mPages.iterator();

        while (iter.hasNext()) {
            iter.next().mCallingActivity = this; // the SnuffyActivity owns most pages except the about page - which will be set explicitly
        }
    }

    private void doCmdFlip() {
        // Note: We have disabled this menu item if this package does not have both
        // of these language codes defined or curr language is not one of them.
        getIntent().putExtra("AllowFlip", true); // allow called intent to show the flip command

        if (mAppLanguage.equalsIgnoreCase("en_heartbeat"))
            switchLanguages("et_heartbeat", false);
        else if (mAppLanguage.equalsIgnoreCase("et_heartbeat"))
            switchLanguages("en_heartbeat", false);
        // no other flip actions defined
    }

    public void doCmdGoToFirstPage(View v) {
        mPager.setCurrentItem(0);
    }

    private void doCmdHelp() {
        Intent intent = new Intent(this, SnuffyHelpActivity.class);
        intent.putExtra("PackageTitle", mPackageTitle);
        startActivity(intent);
    }

    private String getLinkForPackage() {
        String link = getString(R.string.app_email_link); // http://www.godtoolsapp.com/?p=%1&l=%2
        link = link.replace("%1", mAppPackage);
        link = link.replace("%2", mAppLanguage);
        return link;
    }

    public void doCmdShare(View v) {
        // See navToolbarShareSelector in snuffyViewController.m

        String subjectLine = mPackageTitle + " App";
        // stick to plain text - Android cannot reliably send HTML email and anyway
        // most receivers will turn the link into a hyperlink automatically

        String msgBody = getString(R.string.app_email_body); // "Get the %@1 App by going to the following link:\n%@2";
        msgBody = msgBody.replace("%1", mPackageTitle);
        msgBody = msgBody.replace("%2", getLinkForPackage());

        SnuffyApplication app = ((SnuffyApplication) getApplication());
        app.sendEmailWithContent(this, subjectLine, msgBody);
    }

    public void doCmdShowPageMenu(View v) {
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

    private void switchLanguages(String languageCode, boolean bResetToFirstPage) {
        setLanguage(languageCode);

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor ed = settings.edit();
        ed.putString("currLanguageCode", languageCode);
        ed.commit();

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

    private void switchLanguage() {

        if (isUsingPrimaryLanguage) {
            mConfigFileName = mConfigParallel;
            isUsingPrimaryLanguage = false;

        } else {
            mConfigFileName = mConfigPrimary;
            isUsingPrimaryLanguage = true;
        }


        mPager.setAdapter(null);
        doSetup(0);

    }

    public void doCmdInfo(View v) {
        Intent intent = new Intent(this, SnuffyAboutActivity.class);
        startActivity(intent);
    }

    public void doCmdGoToLastPage(View v) {
        mPager.setCurrentItem(mPages.size() - 1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu_options, menu);

        // enable this feature just for one specific situation
        // matches corresponding items in doCmdFlip() below
        MenuItem flipItem = menu.findItem(R.id.CMD_FLIP);
        if (mAppPackage.equalsIgnoreCase("kgp")
                && (mAppLanguage.equalsIgnoreCase("en_heartbeat") || mAppLanguage.equalsIgnoreCase("et_heartbeat")))
            flipItem.setVisible(true);
        else
            flipItem.setVisible(false);

        // enable this feature if the the parallel language is set
        // and a translation is available for this package
        MenuItem switchItem = menu.findItem(R.id.CMD_SWITCH_LANGUAGE);
        if (isParallelLanguageSet && mParallelPackage != null)
            switchItem.setVisible(true);
        else
            switchItem.setVisible(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (super.onOptionsItemSelected(item))
            return true;
        switch (item.getItemId()) {
            case R.id.CMD_ABOUT: {
                doCmdInfo(null);
                break;
            }
            case R.id.CMD_FIRST_PAGE: {
                doCmdGoToFirstPage(null);
                break;
            }
            case R.id.CMD_LAST_PAGE: {
                doCmdGoToLastPage(null);
                break;
            }
            case R.id.CMD_CONTENT: {
                doCmdShowPageMenu(null);
                break;
            }
            case R.id.CMD_EMAIL: {
                doCmdShare(null);
                break;
            }
            case R.id.CMD_HELP: {
                doCmdHelp();
                break;
            }
            case R.id.CMD_FLIP: {
                doCmdFlip();
                break;
            }
            case R.id.CMD_SWITCH_LANGUAGE: {
                switchLanguage();
                break;
            }
            default:
                break;
        }
        return true;
    }

    private ProgressDialog mProgressDialog;
    private static final int DIALOG_PROCESS_PACKAGE_PROGRESS = 1;

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
                // Can't support cancel - would leave app undefined. Processing does not take more than a few seconds so no need.
                //mProgressDialog.setOnCancelListener(new OnCancelListener() {
                //
                //	@Override
                //	public void onCancel(DialogInterface dialog) {
                //		mProcessPackageAsync.cancel(false);
                //	}
                //});
                mProgressDialog.show();
                return mProgressDialog;
            default:
                return null;
        }
    }

    private class ProcessPackageAsync
            extends AsyncTask<String, Integer, Integer>
            implements PackageReader.ProgressCallback {

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
        protected Integer doInBackground(String... params) {
            // params are not used
            Boolean bSuccess = false;
            PackageReader packageReader = new PackageReader();
            try {
                bSuccess = packageReader.processPackagePW(
                        (SnuffyApplication) getApplication(),
                        mPageWidth, mPageHeight,
                        mConfigFileName, mPages,
                        ProcessPackageAsync.this,
                        mAlternateTypeface
                );
            } catch (Exception e) {
                Log.e(TAG, "processPackage failed: " + e.toString());
            }
            if (bSuccess)
                mPackageTitle = packageReader.getPackageTitle();
            return bSuccess ? 1 : 0;  // could not get Boolean return value to work so use Integer instead!
        }

        public void updateProgress(int curr, int max) {
            onProgressUpdate(curr, max);
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            // TODO: How can we get processPackage to call this?
            mProgressDialog.setMax(progress[1]);
            mProgressDialog.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(Integer result) {
            dismissDialog(DIALOG_PROCESS_PACKAGE_PROGRESS);
            // TODO: COMPLETE PROCESSING ON MAIN THREAD
            completeSetup(result != 0);
        }
    }
}
