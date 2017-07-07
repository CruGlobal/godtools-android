package org.cru.godtools.everystudent;

import android.database.Cursor;

class EveryStudentSearchResultsPersistance
{
    private final Cursor mCursor;
    private final String mCount;
    private EveryStudentSearchResults.MySimpleCursorAdapter mAdapter = null;
    private String mQuery;


    public EveryStudentSearchResultsPersistance(EveryStudentSearchResults.MySimpleCursorAdapter adapter, Cursor cursor, String query, String count)
    {
        mCount = count;
        mAdapter = adapter;
        mCursor = cursor;
        mQuery = query;
    }

    public EveryStudentSearchResults.MySimpleCursorAdapter getmAdapter()
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