package org.keynote.godtools.android.expandableList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.keynote.godtools.android.R;
import org.keynote.godtools.android.broadcast.Type;
import org.keynote.godtools.android.business.GTPackage;
import org.keynote.godtools.android.http.DraftCreationTask;
import org.keynote.godtools.android.http.DraftPublishTask;
import org.keynote.godtools.android.http.GodToolsApiClient;
import org.keynote.godtools.android.utils.Device;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.keynote.godtools.android.broadcast.BroadcastUtil.draftBroadcast;
import static org.keynote.godtools.android.broadcast.BroadcastUtil.stopBroadcast;
import static org.keynote.godtools.android.utils.Constants.AUTH_DRAFT;
import static org.keynote.godtools.android.utils.Constants.FOUR_LAWS;
import static org.keynote.godtools.android.utils.Constants.KGP;
import static org.keynote.godtools.android.utils.Constants.PREFS_NAME;
import static org.keynote.godtools.android.utils.Constants.SATISFIED;

/**
 * Created by matthewfrederick on 2/16/15.
 */
public class ExpandableListAdapter extends BaseExpandableListAdapter implements View.OnClickListener
{
    private final SharedPreferences settings;
    private final String TAG = getClass().getSimpleName();
    
    private Context context;
    private List<String> listDataHeader;
    private HashMap<String, List<String>> listDataChild;
    private List<GTPackage> packages;
    private GTPackage currentPackage;
    private String languagePrimary;
    private LocalBroadcastManager broadcastManager;

    int lastExpandedPosition = -1;
    
    public ExpandableListAdapter(Context context, List<GTPackage> packages, String languagePrimary)
    {
        this.context = context;
        this.packages = packages;
        this.languagePrimary = languagePrimary;

        settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();
        broadcastManager = LocalBroadcastManager.getInstance(context);

        // child list needs one item to show expandable menu
        List<String> childList = new ArrayList<String>(1);
        childList.add("");
        
        for (GTPackage gtPackage : this.packages)
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
        
        GTPackage localPackage = null;
        
        for (GTPackage gtPackage : packages)
        {
            if (packageCode.equals(gtPackage.getCode())) localPackage = gtPackage;
        }
        
        if (convertView == null)
        {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView  = inflater.inflate(R.layout.expandable_group_item, null);
        }

        TextView textView = (TextView) convertView.findViewById(R.id.tv_trans_view);
        textView.setText(localPackage.getName());

        ImageView icon = (ImageView) convertView.findViewById(R.id.iv_trans_view);

        LinearLayout layout = (LinearLayout) convertView.findViewById(R.id.group_main);

        if (KGP.equals(localPackage.getCode())) icon.setImageResource(R.drawable.gt4_homescreen_kgpicon);
        if (FOUR_LAWS.equals(localPackage.getCode())) icon.setImageResource(R.drawable.gt4_homescreen_4lawsicon);
        if (SATISFIED.equals(localPackage.getCode())) icon.setImageResource(R.drawable.gt4_homescreen_satisfiedicon);
        
        if (localPackage.isAvailable())
        {
            textView.setTextColor(context.getResources().getColor(android.R.color.black));
            layout.setBackgroundColor(context.getResources().getColor(R.color.current_draft_opacity));
            convertView.findViewById(R.id.icon_line).setVisibility(View.INVISIBLE);
        }
        else
        {
            textView.setTextColor(context.getResources().getColor(android.R.color.white));
            icon.setImageResource(android.R.color.transparent);
            layout.setBackgroundColor(context.getResources().getColor(R.color.new_draft_opacity));
            convertView.findViewById(R.id.icon_line).setVisibility(View.VISIBLE);
        }

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
        setCurrentPackage(groupPosition);
        
        if (convertView == null)
        {
            LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.expandable_child_item, null);
        }
        
        ImageView publish = (ImageView) convertView.findViewById(R.id.publishDraft);
        publish.setOnClickListener(this);
        
        ImageView create = (ImageView) convertView.findViewById(R.id.createDraft);
        create.setOnClickListener(this);

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition)
    {
        Log.i(TAG, "Child clicked: " + groupPosition);
        return false;
    }

    @Override
    public void onClick(View view)
    {

        Log.i(TAG, "Clicked: " + currentPackage.getCode());
        
        if (!Device.isConnected(context))
        {
            Toast.makeText(context.getApplicationContext(), context.getString(R.string.internet_needed), Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        
        switch (view.getId())
        {
            case R.id.publishDraft:
                DialogInterface.OnClickListener publishClickListener = new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which)
                    {
                        switch (which)
                        {
                            case DialogInterface.BUTTON_POSITIVE:
                                
                                broadcastManager.sendBroadcast(draftBroadcast());
                                
                                GodToolsApiClient.publishDraft(settings.getString(AUTH_DRAFT, ""),
                                        currentPackage.getLanguage(),
                                        currentPackage.getCode(),
                                        new DraftPublishTask.DraftTaskHandler()
                                        {
                                            @Override
                                            public void draftTaskComplete()
                                            {
                                                Toast.makeText(context, context.getString(R.string.draft_published), Toast.LENGTH_SHORT).show();
                                                broadcastManager.sendBroadcast(stopBroadcast(Type.DRAFT_PUBLISH_TASK));
                                            }

                                            @Override
                                            public void draftTaskFailure(int statusCode)
                                            {
                                                Toast.makeText(context, context.getString(R.string.draft_publish_fail), Toast.LENGTH_SHORT).show();
                                                broadcastManager.sendBroadcast(stopBroadcast(Type.ERROR, statusCode));
                                            }
                                        });
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                dialogInterface.cancel();
                                break;
                        }
                    }
                };

                builder.setMessage(context.getString(R.string.draft_publish_message))
                        .setPositiveButton(context.getString(R.string.draft_publish_confirm), publishClickListener)
                        .setNegativeButton(context.getString(R.string.draft_publish_negative), publishClickListener)
                        .show();
                break;
            
            case R.id.createDraft:
                DialogInterface.OnClickListener createClickListener = new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which)
                    {
                        switch (which)
                        {
                            case DialogInterface.BUTTON_POSITIVE:
                                
                                broadcastManager.sendBroadcast(draftBroadcast());
                                Log.i(TAG, "Creating Draft");
                                
                                GodToolsApiClient.createDraft(settings.getString(AUTH_DRAFT, ""),
                                        languagePrimary,
                                        currentPackage.getCode(),
                                        new DraftCreationTask.DraftTaskHandler()
                                        {
                                            @Override
                                            public void draftTaskComplete()
                                            {
                                                Toast.makeText(context.getApplicationContext(), context.getString(R.string.draft_created), Toast.LENGTH_SHORT).show();
                                                broadcastManager.sendBroadcast(stopBroadcast(Type.DRAFT_CREATION_TASK));
                                            }

                                            @Override
                                            public void draftTaskFailure(int code)
                                            {
                                                Toast.makeText(context.getApplicationContext(), context.getString(R.string.draft_create_failed), Toast.LENGTH_SHORT).show();

                                                broadcastManager.sendBroadcast(stopBroadcast(Type.ERROR, code));
                                            }
                                        });
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                dialogInterface.cancel();
                                break;
                        }
                    }
                };
                
                builder.setTitle(context.getString(R.string.draft_start_message) + currentPackage.getName())
                        .setPositiveButton(R.string.yes, createClickListener)
                        .setNegativeButton(R.string.no, createClickListener)
                        .show();
        }
    }
    
    private void setCurrentPackage(int groupPosition)
    {
        final String packageCode = (String) getGroup(groupPosition);

        for (GTPackage gtPackage : packages)
        {
            if (packageCode.equals(gtPackage.getCode()))
            {
                Log.i(TAG, "Current Package: " + gtPackage.getCode());
                currentPackage = gtPackage;
            }
        }            
    }
}
