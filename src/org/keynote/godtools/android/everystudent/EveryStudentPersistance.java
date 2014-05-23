package org.keynote.godtools.android.everystudent;

import java.util.List;
import java.util.Map;
import android.widget.ExpandableListAdapter;

class EveryStudentPersistance{
	static private ExpandableListAdapter mAdapter = null;
	static private List<List<Map<String, String>>> mTopics = null;
	static private List<Map<String, String>> mCategories = null;
	
	public ExpandableListAdapter getmAdapter() {
		return mAdapter;
	}

	public void setmAdapter(ExpandableListAdapter mAdapter) {
		EveryStudentPersistance.mAdapter = mAdapter;
	}

	public List<List<Map<String, String>>> getmTopics() {
		return mTopics;
	}

	public void setmTopics(List<List<Map<String, String>>> mTopics) {
		EveryStudentPersistance.mTopics = mTopics;
	}

	public List<Map<String, String>> getmCategories() {
		return mCategories;
	}

	public void setmCategories(List<Map<String, String>> mCategories) {
		EveryStudentPersistance.mCategories = mCategories;
	}

	public EveryStudentPersistance(ExpandableListAdapter adapter, List<List<Map<String, String>>> topics, List<Map<String, String>> categories) {
		mAdapter = adapter;
		mTopics = topics;
		mCategories = categories;
	}
	
	public EveryStudentPersistance() {}
}