package org.keynote.godtools.android.everystudent;

import android.database.Cursor;

import org.keynote.godtools.android.everystudent.EveryStudentSearchResults.MySimpleCursorAdapter;

class EveryStudentSearchResultsPersistance
{
    private MySimpleCursorAdapter mAdapter = null;
    private Cursor mCursor;
    private String mQuery;
    private String mCount;


    public EveryStudentSearchResultsPersistance(MySimpleCursorAdapter adapter, Cursor cursor, String query, String count)
    {
        mCount = count;
        mAdapter = adapter;
        mCursor = cursor;
        mQuery = query;
    }

    public EveryStudentSearchResultsPersistance()
    {
    }

    public MySimpleCursorAdapter getmAdapter()
    {
        return mAdapter;
    }

    public void setmAdapter(MySimpleCursorAdapter adapter)
    {
        mAdapter = adapter;
    }

    public Cursor getCursor()
    {
        return mCursor;
    }

    public void setCursor(Cursor cursor)
    {
        mCursor = cursor;
    }

    public String getQuery()
    {
        return mQuery;
    }

    public void setQuery(String query)
    {
        mQuery = query;
    }

    public String getCount()
    {
        return mCount;
    }

    public void setCount(String count)
    {
        mCount = count;
    }
}