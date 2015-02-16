package org.keynote.godtools.android.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import org.keynote.godtools.android.R;

import java.util.HashMap;
import java.util.List;

/**
 * Created by matthewfrederick on 2/16/15.
 */
public class ExpandableListAdapter extends BaseExpandableListAdapter
{
    private Context context;
    private List<String> listDataHeader;
    private HashMap<String, List<String>> listDataChild;
    
    public ExpandableListAdapter(Context context, List<String> listDataHeader, HashMap<String, List<String>> listDataChild)
    {
        this.context = context;
        this.listDataHeader = listDataHeader;
        this.listDataChild = listDataChild;
    }
    
    @Override
    public int getGroupCount()
    {
        return this.listDataHeader.size();
    }

    @Override
    public int getChildrenCount(int groupPosition)
    {
        return this.listDataChild.get(this.listDataHeader.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition)
    {
        return this.listDataHeader.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition)
    {
        return this.listDataChild.get(this.listDataHeader.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition)
    {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition)
    {
        return childPosition;
    }

    @Override
    public boolean hasStableIds()
    {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent)
    {
        String headerTitle = (String) getGroup(groupPosition);
        if (convertView == null)
        {
            LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView  = inflater.inflate(R.layout.expandable_group_item, null);
        }
        TextView textView = (TextView) convertView.findViewById(R.id.tv_trans_view);
        textView.setText(headerTitle);
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent)
    {
        if (convertView == null)
        {
            LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.expandable_child_item, null);
        }

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int i, int i2)
    {
        return false;
    }
}
