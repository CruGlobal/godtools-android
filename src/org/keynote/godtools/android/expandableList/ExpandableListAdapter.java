package org.keynote.godtools.android.expandableList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.keynote.godtools.android.R;
import org.keynote.godtools.android.business.GTPackage;
import org.keynote.godtools.android.http.DraftPublishTask;
import org.keynote.godtools.android.http.GodToolsApiClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by matthewfrederick on 2/16/15.
 */
public class ExpandableListAdapter extends BaseExpandableListAdapter implements View.OnClickListener
{
    private final SharedPreferences settings;
    private final String PREFS_NAME = "GodTools";
    private final String TAG = getClass().getSimpleName();    
    
    private Context context;
    private List<String> listDataHeader;
    private HashMap<String, List<String>> listDataChild;
    private List<GTPackage> packages;
    private GTPackage currentPackage;
    private String languagePrimary;

    int lastExpandedPosition = -1;
    
    public ExpandableListAdapter(Context context, List<GTPackage> packages, String languagePrimary)
    {
        this.context = context;
        this.packages = packages;
        this.languagePrimary = languagePrimary;

        settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

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
    public View getGroupView(final int groupPosition, final boolean isExpanded, View convertView, final ViewGroup parent)
    {
        final String packageCode = (String) getGroup(groupPosition);
        
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
        
        subMenu.setOnClickListener(new View.OnClickListener()
        {   
            @Override
            public void onClick(View view)
            {
                if (isExpanded)
                {
                    ((ExpandableListView) parent).collapseGroup(groupPosition);
                }
                else
                {
                    if (groupPosition != lastExpandedPosition)
                    {
                        ((ExpandableListView) parent).collapseGroup(lastExpandedPosition);
                    }
                    ((ExpandableListView) parent).expandGroup(groupPosition);
                    lastExpandedPosition = groupPosition;
                }
                
            }
        });
        
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent)
    {
        final String packageCode = (String) getGroup(groupPosition);

        for (GTPackage gtPackage : packages)
        {
            if (packageCode.equals(gtPackage.getCode())) currentPackage = gtPackage;
        }
        
        if (convertView == null)
        {
            LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.expandable_child_item, null);
        }
        
        ImageView delete = (ImageView) convertView.findViewById(R.id.deleteDraft);
        
        ImageView publish = (ImageView) convertView.findViewById(R.id.publishDraft);
        publish.setOnClickListener(this);
        
        ImageView create = (ImageView) convertView.findViewById(R.id.createDraft);

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition)
    {
        return false;
    }

    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.publishDraft:
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which)
                    {
                        switch (which)
                        {
                            case DialogInterface.BUTTON_POSITIVE:
                                GodToolsApiClient.publishDraft(settings.getString("Authorization_Draft", ""),
                                        currentPackage.getLanguage(),
                                        currentPackage.getCode(),
                                        new DraftPublishTask.DraftTaskHandler()
                                        {
                                            @Override
                                            public void draftTaskComplete()
                                            {
                                                Toast.makeText(context, "Draft has been published", Toast.LENGTH_SHORT).show();
                                            }

                                            @Override
                                            public void draftTaskFailure()
                                            {
                                                Toast.makeText(context, "Failed to publish draft", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("Do you want to publish this draft?")
                        .setPositiveButton("Yes, it's ready!", dialogClickListener)
                        .setNegativeButton("No, I changed my mind.", dialogClickListener)
                        .show();
                break;
        }
    }
}
