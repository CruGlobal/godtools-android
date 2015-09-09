package org.keynote.godtools.android.everystudent;

import android.database.Cursor;

import org.keynote.godtools.android.everystudent.EveryStudentSearchResults.MySimpleCursorAdapter;

class EveryStudentSearchResultsPersistance
{
    private final Cursor mCursor;
    private final String mCount;
    private MySimpleCursorAdapter mAdapter = null;
    private String mQuery;


    public EveryStudentSearchResultsPersistance(MySimpleCursorAdapter adapter, Cursor cursor, String query, String count)
    {
        mCount = count;
        mAdapter = adapter;
        mCursor = cursor;
        mQuery = query;
    }

    public MySimpleCursorAdapter getmAdapter()
    {
        return mAdapter;
    }

    public Cursor getCursor()
    {
        return mCursor;
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

}