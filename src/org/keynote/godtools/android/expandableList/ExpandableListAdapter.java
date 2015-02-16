package org.keynote.godtools.android.expandableList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.keynote.godtools.android.R;
import org.keynote.godtools.android.business.GTPackage;

import java.util.ArrayList;
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
    private List<GTPackage> packages;
    private GTPackage currentPackage;
    
    public ExpandableListAdapter(Context context, List<GTPackage> packages)
    {
        this.context = context;
        this.packages = packages;

        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();

        // child list needs one item to show expandable menu
        List<String> childList = new ArrayList<String>(1);
        childList.add("");

        for (GTPackage gtPackage : packages)
        {
            listDataHeader.add(gtPackage.getCode());
            listDataChild.put(gtPackage.getCode(), childList);
        }
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
        String packageCode = (String) getGroup(groupPosition);
        
        for (GTPackage gtPackage : packages)
        {
            if (packageCode.equals(gtPackage.getCode())) currentPackage = gtPackage;
        }
        
        if (convertView == null)
        {
            LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView  = inflater.inflate(R.layout.expandable_group_item, null);
        }

        TextView textView = (TextView) convertView.findViewById(R.id.tv_trans_view);
        textView.setText(currentPackage.getName());

        ImageView icon = (ImageView) convertView.findViewById(R.id.iv_trans_view);

        if ("kgp".equals(currentPackage.getCode())) icon.setImageResource(R.drawable.gt4_homescreen_kgpicon);
        if ("fourlaws".equals(currentPackage.getCode())) icon.setImageResource(R.drawable.gt4_homescreen_4lawsicon);
        if ("satisfied".equals(currentPackage.getCode())) icon.setImageResource(R.drawable.gt4_homescreen_satisfiedicon);
        
        ImageView subMenu = (ImageView) convertView.findViewById(R.id.sub_menu);
        
        if (isExpanded)
        {
            subMenu.setImageResource(R.drawable.gt4_gomescreen_draftgripd);
        }
        else
        {
            subMenu.setImageResource(R.drawable.gt4_homescreen_draftgripc);
        }
        
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
    public boolean isChildSelectable(int groupPosition, int childPosition)
    {
        return false;
    }
}
