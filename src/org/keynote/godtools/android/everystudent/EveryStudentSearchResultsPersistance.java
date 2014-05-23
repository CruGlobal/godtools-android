package org.keynote.godtools.android.everystudent;
import org.keynote.godtools.android.everystudent.EveryStudentSearchResults.MySimpleCursorAdapter;

import android.database.Cursor;

class EveryStudentSearchResultsPersistance{
	private MySimpleCursorAdapter mAdapter = null;
	private Cursor mCursor;
	private String mQuery;
	private String mCount;

	
	public MySimpleCursorAdapter getmAdapter() {
		return mAdapter;
	}

	public void setmAdapter(MySimpleCursorAdapter adapter) {
		mAdapter = adapter;
	}
	
	
	public void setCursor(Cursor cursor) {
		mCursor = cursor;
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
	
	public void setCount(String count) {
		mCount = count;
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
	
	public EveryStudentSearchResultsPersistance() {}
}