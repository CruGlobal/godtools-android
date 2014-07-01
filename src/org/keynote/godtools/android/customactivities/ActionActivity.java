package org.keynote.godtools.android.customactivities;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.internal.view.menu.MenuBuilder;
import android.util.Log;
import android.view.Menu;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.keynote.godtools.android.R;
import org.keynote.godtools.android.customviews.CustomActionBar;
import org.keynote.godtools.android.utils.MenuParser;

public class ActionActivity extends FragmentActivity {
    String LOGTAG = "ActionActivity";

    public static final int RESULT_DOWNLOAD_PRIMARY = 2001;
    public static final int RESULT_DOWNLOAD_PARALLEL = 2002;
    public static final int RESULT_CHANGED_PRIMARY = 2003;

    CustomActionBar actionbar;
    MenuParser menuParser;

    String pageTitle = "";
    boolean isBackButtonEnabled = true;
    boolean backButtonStateApplied = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onCreateOptionsMenu(new MenuBuilder(this));
    }

    @Override
    public void setContentView(int layoutResID) {
        /*inflate actionlayout.xml. the layout consists of a LinearLayout and includes the actionbar.
         * next, inflate the layout specified in the setContentView(int layoutResID) parameter then add it
         * to the actionlayout.
         */

        //inflate the custom actionlayout
        ViewGroup actionLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.actionlayout, null);
        //add the activity's layout to the actionlayout
        getLayoutInflater().inflate(layoutResID, actionLayout);

        //set up the actionbar
        actionbar = new CustomActionBar(this, actionLayout);
        actionbar.setPageTitle(pageTitle);

        //if the back button state has not yet been applied, do it now
        if (!backButtonStateApplied)
            actionbar.applyBackButtonState(isBackButtonEnabled);
        //if menuParser was set up first, we get the menu elements and attach it to the actionbar
        if (menuParser != null)
            actionbar.setUpMenu(menuParser.getActionMenuList(), menuParser.getMenuItems());


        //display the whole augmented layout, regardless if actionbar has been setup
        getWindow().setContentView(actionLayout);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(LOGTAG, "super oncreateoptionsmenu");
        return true;
    }

    public void setPageTitle(String title) {
        if (actionbar != null)
            actionbar.setPageTitle(title);
        else
            pageTitle = title;
    }

    public void setBackButtonEnabled(boolean enabled) {
        isBackButtonEnabled = enabled;

        if (actionbar != null) {
            actionbar.applyBackButtonState(isBackButtonEnabled);
            backButtonStateApplied = true;

            return;
        }

        backButtonStateApplied = false;
    }


    public void createMenuItems(int menuResourceId, Menu menu) {
        menuParser = new MenuParser(this);
        menuParser.createMenuItems(menuResourceId, menu);

        //if actionbar was set up first, we attach the menu to it
        if (actionbar != null) {
            actionbar.setUpMenu(menuParser.getActionMenuList(), menuParser.getMenuItems());
        }
    }

}
