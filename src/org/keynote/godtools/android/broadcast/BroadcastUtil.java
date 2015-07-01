package org.keynote.godtools.android.broadcast;

import android.content.Intent;
import android.content.IntentFilter;

import static org.keynote.godtools.android.utils.Constants.STATUS_CODE;

/**
 * Created by matthewfrederick on 2/17/15.
 */
public final class BroadcastUtil
{
    public static final String ACTION_START = BroadcastUtil.class.getName() + ".ACTION_START";
    public static final String ACTION_STOP = BroadcastUtil.class.getName() + ".ACTION_STOP";
    public static final String ACTION_TYPE = BroadcastUtil.class.getName() + ".ACTION_TYPE";
    public static final String ACTION_FAIL = BroadcastUtil.class.getName() + ".ACTION_FAIL";
    private final String TAG = getClass().getSimpleName();

    public static Intent startBroadcast()
    {
        return new Intent(ACTION_START);
    }

    public static Intent stopBroadcast(Type type)
    {
        return stopBroadcast(type, 0);
    }

    public static Intent stopBroadcast(Type type, int code)
    {
        Intent intent = new Intent(ACTION_STOP);
        intent.putExtra(ACTION_TYPE, type);
        intent.putExtra(STATUS_CODE, code);
        return intent;
    }

    public static Intent failBroadcast(Type type)
    {
        Intent intent = new Intent(ACTION_FAIL);
        intent.putExtra(ACTION_TYPE, type);
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
