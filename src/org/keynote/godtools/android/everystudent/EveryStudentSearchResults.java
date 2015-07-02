package org.keynote.godtools.android.everystudent;

import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Spannable;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import org.keynote.godtools.android.R;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EveryStudentSearchResults extends ListActivity
{
    public static final int DIALOG_LOADING = 0;
    public static final String LOGTAG = "EveryStudentSearchResults";
    private static SearcherThread mSearcherThread;
    private static MySimpleCursorAdapter mAdapter;
    private static Cursor mCursor;
    private static String mCount;
    private String mQuery;
    private Pattern mPattern;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);

        Intent intent = getIntent();
        mQuery = intent.getStringExtra(SearchManager.QUERY);


        if (Intent.ACTION_VIEW.equals(intent.getAction()))
        {
            // handles a click on a search suggestion; launches activity to show
            // word
            Intent wordIntent = new Intent(this, EveryStudentView.class);
            wordIntent.setData(intent.getData());
            wordIntent.putExtra(SearchManager.QUERY,
                    intent.getStringExtra(SearchManager.USER_QUERY));
            startActivity(wordIntent);
            finish();
        }
        else if (Intent.ACTION_SEARCH.equals(intent.getAction()))
        {
            // handles a search query

            HashMap<String, String> params = new HashMap<String, String>();
            params.put("Query", mQuery);
//FIXME			FlurryAgent.onEvent(FlurryAPI.FlurryPrefix + LOGTAG, params);
//FIXME			FlurryAgent.onPageView();

            EveryStudentSearchResultsPersistance essrp;
            essrp = (EveryStudentSearchResultsPersistance) getLastNonConfigurationInstance();

            if (essrp != null && essrp.getmAdapter() != null && essrp.getCursor() != null && essrp.getQuery() != null && essrp.getCount() != null)
            {
                mCount = essrp.getCount();
                mQuery = essrp.getQuery();
                mCursor = essrp.getCursor();
                mAdapter = createAdapter(EveryStudentSearchResults.this, mCursor);
                TextView text = (TextView) findViewById(R.id.text);
                text.setText(mCount);
                setListAdapter(mAdapter);
            }
            else if (mSearcherThread != null && mSearcherThread.isAlive())
            {
                mSearcherThread.setHandler(new SearcherHandler());
            }
            else
            {
                mSearcherThread = new SearcherThread();
                mSearcherThread.start();
                showDialog(DIALOG_LOADING);
            }

            this.getListView().setOnItemClickListener(new OnItemClickListener()
            {
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id)
                {
                    Intent intent = new Intent(EveryStudentSearchResults.this, EveryStudentView.class);
                    Uri contentUri = Uri.withAppendedPath(EveryStudentProvider.CONTENT_URI, "content");
                    Uri contentUriRow = Uri.withAppendedPath(contentUri, String.valueOf(id));
                    intent.setData(contentUriRow);
                    intent.putExtra(SearchManager.QUERY, mQuery);
                    startActivity(intent);
                }
            });
        }
    }

    @Override
    public Object onRetainNonConfigurationInstance()
    {
        return new EveryStudentSearchResultsPersistance(mAdapter, mCursor, mQuery, mCount);
    }

    @Override
    public void onStart()
    {
        super.onStart();
    }

    @Override
    public void onStop()
    {
        super.onStop();
//FIXME	   FlurryAgent.onEndSession(this);
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

    private MySimpleCursorAdapter createAdapter(Context context, Cursor c)
    {
        // Specify the columns we want to display in the result
        String[] from = new String[]{SearchManager.SUGGEST_COLUMN_TEXT_1,
                SearchManager.SUGGEST_COLUMN_TEXT_2};

        // Specify the corresponding layout elements where we want the
        // columns to go
        int[] to = new int[]{R.id.title, R.id.snippet};
        return new MySimpleCursorAdapter(EveryStudentSearchResults.this,
                R.layout.search_result, mCursor, from, to);
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

    private class SearcherHandler extends Handler
    {
        @Override
        public void handleMessage(Message msg)
        {
            TextView text = (TextView) findViewById(R.id.text);
            mCount = mSearcherThread.getCount();
            text.setText(mCount);
            mAdapter = mSearcherThread.getAdapter();
            setListAdapter(mAdapter);
            dismissDialog(DIALOG_LOADING);
        }
    }

    class SearcherThread extends Thread
    {
        Handler mHandler = new SearcherHandler();
        MySimpleCursorAdapter adapter;
        Cursor myCursor;
        String myCount;

        public String getCount()
        {
            return myCount;
        }

        public void setHandler(Handler h)
        {
            mHandler = h;
        }

        public MySimpleCursorAdapter getAdapter()
        {
            return adapter;
        }

        public void run()
        {
            Looper.prepare();

            mCursor = managedQuery(EveryStudentProvider.CONTENT_URI, null,
                    null, new String[]{mQuery}, null);

            if (mCursor == null)
            {
                myCount = getString(R.string.search_no_results,
                        mQuery);
            }
            else
            {
                // Display the number of results
                int count = mCursor.getCount();
                myCount = getResources().getQuantityString(
                        R.plurals.search_results, count,
                        count, mQuery);

                adapter = createAdapter(EveryStudentSearchResults.this, mCursor);
            }
            mHandler.sendEmptyMessage(0);
        }
    }

    public class MySimpleCursorAdapter extends SimpleCursorAdapter
    {

        private Cursor c;
        private Context context;
        public MySimpleCursorAdapter(Context context, int layout, Cursor c,
                                     String[] from, int[] to)
        {
            super(context, layout, c, from, to);
            this.c = c;
            this.context = context;


            String[] terms = mQuery.split("[\\s]");
            List<String> termsList = Arrays.asList(terms);
            String pattern = "";

            Iterator<String> itr = termsList.iterator();
            while (itr.hasNext())
            {
                pattern += itr.next().trim() + "[^\\s,\\.\\?:;]*";
                if (itr.hasNext())
                {
                    pattern += "|";
                }
            }
            mPattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE
                    | Pattern.MULTILINE);

        }

        @Override
        public View getView(int pos, View inView, ViewGroup parent)
        {
            View v = inView;
            if (v == null)
            {
                LayoutInflater inflater = (LayoutInflater) context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = inflater.inflate(R.layout.search_result, null);
            }
            this.c.moveToPosition(pos);
            TextView vTitle = (TextView) v.findViewById(R.id.title);
            TextView vSnippet = (TextView) v.findViewById(R.id.snippet);
            String title = this.c.getString(this.c
                    .getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1));
            String snippet = this.c.getString(this.c
                    .getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_2));

            vTitle.setText(title, TextView.BufferType.SPANNABLE);
            vSnippet.setText(snippet, TextView.BufferType.SPANNABLE);

            Spannable titleSpan = (Spannable) vTitle.getText();
            Spannable snippetSpan = (Spannable) vSnippet.getText();

            Matcher titleMatcher = mPattern.matcher(title);
            while (titleMatcher.find())
            {
                titleSpan.setSpan(
                        new StyleSpan(android.graphics.Typeface.BOLD),
                        titleMatcher.start(), titleMatcher.end(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            Matcher snippetMatcher = mPattern.matcher(snippet);
            while (snippetMatcher.find())
            {
                snippetSpan.setSpan(new BackgroundColorSpan(
                                android.graphics.Color.YELLOW), snippetMatcher.start(),
                        snippetMatcher.end(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                snippetSpan.setSpan(new ForegroundColorSpan(
                                android.graphics.Color.BLACK), snippetMatcher.start(),
                        snippetMatcher.end(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            return (v);
        }
    }
}
