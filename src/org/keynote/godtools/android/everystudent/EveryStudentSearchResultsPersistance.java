package org.keynote.godtools.android.everystudent;
import android.database.Cursor;

import org.keynote.godtools.android.everystudent.EveryStudentSearchResults.MySimpleCursorAdapter;

class EveryStudentSearchResultsPersistance{
	private MySimpleCursorAdapter mAdapter = null;
	private final Cursor mCursor;
	private String mQuery;
	private final String mCount;

	
	public MySimpleCursorAdapter getmAdapter() {
		return mAdapter;
	}


	public Cursor getCursor() {
		return mCursor;
	}
	
	public void setQuery(String query) {
		mQuery = query;
	}
	
	public String getQuery() {
		return mQuery;
	}

	public String getCount() {
		return mCount;
	}


	public EveryStudentSearchResultsPersistance(MySimpleCursorAdapter adapter, Cursor cursor, String query, String count) {
		mCount = count;
		mAdapter = adapter;
		mCursor = cursor;
		mQuery = query;
	}

}