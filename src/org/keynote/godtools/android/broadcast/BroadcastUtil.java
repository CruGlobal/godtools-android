package org.keynote.godtools.android.broadcast;

import android.content.Intent;
import android.content.IntentFilter;

import static org.keynote.godtools.android.utils.Constants.TYPE;

/**
 * Created by matthewfrederick on 2/17/15.
 */
public final class BroadcastUtil
{
    private final String TAG = getClass().getSimpleName();

    public static final String ACTION_START = BroadcastUtil.class.getName() + ".ACTION_START";
    public static final String ACTION_STOP = BroadcastUtil.class.getName() + ".ACTION_STOP";
    public static final String ACTION_TYPE = BroadcastUtil.class.getName() + ".ACTION_TYPE";
    public static final String ACTION_FAIL = BroadcastUtil.class.getName() + ".ACTION_FAIL";
    
    public static Intent startBroadcast()
    {
        return new Intent(ACTION_START);
    }
    
    public static Intent stopBroadcast(Type type)
    {
        Intent intent = new Intent(ACTION_STOP);
        intent.putExtra(TYPE, type);
        return intent;
    }

    public static Intent failBroadcast(Type type)
    {
        Intent intent = new Intent(ACTION_FAIL);
        intent.putExtra(TYPE, type);
        return intent;
    }

    public static IntentFilter startFilter()
    {
        return new IntentFilter(ACTION_START);
    }

    public static IntentFilter stopFilter()
    {
        return new IntentFilter(ACTION_STOP);
    }

    public static IntentFilter failedFilter()
    {
        return new IntentFilter(ACTION_FAIL);
    }
}
