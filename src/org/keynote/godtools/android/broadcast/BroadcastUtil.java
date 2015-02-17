package org.keynote.godtools.android.broadcast;

import android.content.Intent;
import android.content.IntentFilter;

import org.keynote.godtools.android.http.DownloadTask;
import org.keynote.godtools.android.http.DraftCreationTask;
import org.keynote.godtools.android.http.DraftPublishTask;
import org.keynote.godtools.android.http.MetaTask;

/**
 * Created by matthewfrederick on 2/17/15.
 */
public final class BroadcastUtil
{
    private final String TAG = getClass().getSimpleName();

    public static final String ACTION_START = BroadcastUtil.class.getName() + ".ACTION_START";
    public static final String ACTION_RUNNING = BroadcastUtil.class.getName() + ".ACTION_RUNNING";
    public static final String ACTION_STOP = BroadcastUtil.class.getName() + ".ACTION_STOP";
    public static final String ACTION_TYPE = BroadcastUtil.class.getName() + ".ACTION_TYPE";
    
    public static final String DOWNLOAD_COMPLETE = DownloadTask.class.getName() + ".DOWNLOAD_COMPLETE";
    public static final String DRAFT_CREATION_COMPLETE = DraftCreationTask.class.getName() + ".DRAFT_CREATION_COMPLETE";
    public static final String DRAFT_PUBLISH_COMPLETE = DraftPublishTask.class.getName() + ".DRAFT_PUBLISH_COMPLETE";
    public static final String META_COMPLETE = MetaTask.class.getName() + ".META_COMPLETE";
    
    public static Intent startBroadcast()
    {
        return new Intent(ACTION_START);
    }
    
    public static Intent runningBroadcast()
    {
        return new Intent(ACTION_RUNNING);        
    }
    
    public static Intent stopBroadcast(Type type)
    {
        Intent intent = new Intent(ACTION_STOP);
        intent.putExtra(ACTION_TYPE, type);
        return intent;
    }

    public static IntentFilter startFilter()
    {
        return new IntentFilter(ACTION_START);
    }

    public static IntentFilter runningFilter()
    {
        return new IntentFilter(ACTION_RUNNING);
    }

    public static IntentFilter stopFilter()
    {
        return new IntentFilter(ACTION_STOP);
    }
    
    public static Intent downloadTaskBroadcast()
    {
        return new Intent(DOWNLOAD_COMPLETE);        
    }
    
    public static Intent draftCreationBroadcast()
    {
        return new Intent(DRAFT_CREATION_COMPLETE);        
    }
    
    public static Intent draftPublishBroadcast()
    {
        return new Intent(DRAFT_PUBLISH_COMPLETE);        
    }
    
    public static Intent metaBroadcast()
    {
        return new Intent(META_COMPLETE);
    }
}
