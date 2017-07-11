package org.cru.godtools.everystudent;

import android.widget.ExpandableListAdapter;

import java.util.List;
import java.util.Map;

class EveryStudentPersistance {
    private ExpandableListAdapter mAdapter = null;
    private List<List<Map<String, String>>> mTopics = null;
    private List<Map<String, String>> mCategories = null;

    EveryStudentPersistance(ExpandableListAdapter adapter, List<List<Map<String, String>>> topics,
                            List<Map<String, String>> categories) {
        mAdapter = adapter;
        mTopics = topics;
        mCategories = categories;
    }

    public ExpandableListAdapter getmAdapter() {
        return mAdapter;
    }

    public List<List<Map<String, String>>> getmTopics() {
        return mTopics;
    }

    public List<Map<String, String>> getmCategories() {
        return mCategories;
    }
}
