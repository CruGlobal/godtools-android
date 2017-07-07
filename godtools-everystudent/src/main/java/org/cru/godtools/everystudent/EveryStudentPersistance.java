package org.cru.godtools.everystudent;

import android.widget.ExpandableListAdapter;

import java.util.List;
import java.util.Map;

class EveryStudentPersistance
{
    static private ExpandableListAdapter mAdapter = null;
    static private List<List<Map<String, String>>> mTopics = null;
    static private List<Map<String, String>> mCategories = null;

    public EveryStudentPersistance(ExpandableListAdapter adapter, List<List<Map<String, String>>> topics, List<Map<String, String>> categories)
    {
        mAdapter = adapter;
        mTopics = topics;
        mCategories = categories;
    }

    public ExpandableListAdapter getmAdapter()
    {
        return mAdapter;
    }

    public List<List<Map<String, String>>> getmTopics()
    {
        return mTopics;
    }

    public List<Map<String, String>> getmCategories()
    {
        return mCategories;
    }

}