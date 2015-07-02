package org.keynote.godtools.android.everystudent;

import android.app.Dialog;
import android.app.ExpandableListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.SimpleExpandableListAdapter;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.keynote.godtools.android.R;
import org.keynote.godtools.android.snuffy.SnuffyApplication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EveryStudent extends ExpandableListActivity
{
    public static final String NAME = "NAME";
    public static final String CONTENT = "CONTENT";
    public static final String ROWID = "ROWID";

    public static final int DIALOG_LOADING = 0;
    private static ExpandableListAdapter mAdapter;
    private static ParserThread mParserThread;
    private List<List<Map<String, String>>> mTopics;
    private List<Map<String, String>> mCategories;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);

        EveryStudentPersistance esp;
        esp = (EveryStudentPersistance) getLastNonConfigurationInstance();

        if (esp != null && esp.getmAdapter() != null && esp.getmTopics() != null && esp.getmCategories() != null)
        {
            mAdapter = esp.getmAdapter();
            mTopics = esp.getmTopics();
            mCategories = esp.getmCategories();
            setListAdapter(mAdapter);
        }
        else if (mParserThread != null && mParserThread.isAlive())
        {
            mParserThread.setHandler(new ParserHandler());
        }
        else
        {
            mParserThread = new ParserThread();
            mParserThread.start();
            showDialog(DIALOG_LOADING);
        }

        Drawable groupIndicator = getResources().getDrawable(R.drawable.expander_group);
        ExpandableListView view = getExpandableListView();
        view.setGroupIndicator(groupIndicator);
        view.setOnChildClickListener(new OnChildClickListener()
        {
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id)
            {
                Intent intent = new Intent(EveryStudent.this, EveryStudentView.class);
                Uri contentUri = Uri.withAppendedPath(EveryStudentProvider.CONTENT_URI, "content");
                Uri contentUriRow = Uri.withAppendedPath(contentUri, mTopics.get(groupPosition).get(childPosition).get(ROWID));
                intent.setData(contentUriRow);
                startActivity(intent);
                return false;
            }
        });

        trackScreenActivity();

//FIXME		FlurryAgent.onEvent(FlurryAPI.FlurryPrefix + LOGTAG);
//FIXME		FlurryAgent.onPageView();
    }

    @Override
    public void onStart()
    {
        super.onStart();
//       FlurryAPI.onStartSession(this);
    }

    @Override
    public void onStop()
    {
        super.onStop();
//FIXME       FlurryAgent.onEndSession(this);
    }

    @Override
    protected Dialog onCreateDialog(int id)
    {
        switch (id)
        {
            case DIALOG_LOADING:
                ProgressDialog pdlg = new ProgressDialog(this);
                pdlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                pdlg.setMessage("Loading. Please wait...");
                pdlg.setIndeterminate(true);
                return pdlg;
        }
        return null;
    }

    public Object onRetainNonConfigurationInstance()
    {
        return new EveryStudentPersistance(mAdapter, mTopics, mCategories);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.everystudent, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.search:
                onSearchRequested();
                return true;
            default:
                return false;
        }
    }

    private Tracker getGoogleAnalyticsTracker()
    {
        return ((SnuffyApplication) getApplication()).getTracker();
    }

    private void trackScreenActivity()
    {
        Tracker tracker = getGoogleAnalyticsTracker();
        tracker.setScreenName("EveryStudent");
        tracker.send(new HitBuilders.AppViewBuilder()
                .setCustomDimension(1, "EveryStudent")
                .build());
    }

    private class ParserHandler extends Handler
    {
        @Override
        public void handleMessage(Message msg)
        {
            mAdapter = mParserThread.getAdapter();
            mCategories = mParserThread.getCategories();
            mTopics = mParserThread.getTopics();
            setListAdapter(mAdapter);
            dismissDialog(DIALOG_LOADING);
        }
    }

    class ParserThread extends Thread
    {
        Handler mHandler = new ParserHandler();
        ExpandableListAdapter adapter;
        List<List<Map<String, String>>> topics = new ArrayList<List<Map<String, String>>>();
        List<Map<String, String>> categories = new ArrayList<Map<String, String>>();

        public void setHandler(Handler h)
        {
            mHandler = h;
        }

        public ExpandableListAdapter getAdapter()
        {
            return adapter;
        }

        public List<List<Map<String, String>>> getTopics()
        {
            return topics;
        }

        public List<Map<String, String>> getCategories()
        {
            return categories;
        }

        public void run()
        {
            Uri base = Uri.withAppendedPath(EveryStudentProvider.CONTENT_URI, "base");
            Cursor cur = managedQuery(base, null, null, null, null);

            HashMap<String, List<Map<String, String>>> tempmap = new HashMap<String, List<Map<String, String>>>();
            ArrayList<String> cats = new ArrayList<String>();

            if (cur.moveToFirst())
            {
                int rowid;
                String category;
                String title;
                do
                {
                    rowid = cur.getInt(0);
                    category = cur.getString(1);
                    title = cur.getString(2);

                    if (!cats.contains(category))
                    {
                        cats.add(category);
                    }

                    if (!tempmap.containsKey(category))
                    {
                        tempmap.put(category, new ArrayList<Map<String, String>>());
                    }
                    HashMap<String, String> map = new HashMap<String, String>();
                    map.put(NAME, title);
                    map.put(ROWID, String.valueOf(rowid));
                    tempmap.get(category).add(map);
                } while (cur.moveToNext());
            }

            for (String cat : cats)
            {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put(NAME, cat);
                categories.add(map);
                topics.add(tempmap.get(cat));
            }

            adapter = new SimpleExpandableListAdapter(EveryStudent.this,
                    categories,
                    R.layout.simple_expandable_list_item_1,
                    new String[]{NAME}, new int[]{android.R.id.text1},
                    topics, R.layout.simple_expandable_list_item_2,
                    new String[]{NAME}, new int[]{android.R.id.text1});

            mHandler.sendEmptyMessage(0);
        }
    }
}